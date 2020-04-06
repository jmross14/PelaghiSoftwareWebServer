package com.pelaghisoftware.data.dao.base;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.testkit.javadsl.TestKit;
import com.pelaghisoftware.data.dao.impl.base.BaseDaoImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the BaseDaoImpl abstract class.
 */
public class BaseDaoImplTest
{
    private static final Logger logger = LoggerFactory.getLogger(BaseDaoImplTest.class);

    private static ActorSystem system;
    private static Materializer materializer;
    private static BaseDaoImpl baseDao;

    //Section for queries that will be used during testing
    private static final String INSERT_TEST1 = "CREATE TABLE IF NOT EXISTS TEST1 " +
        "(id SERIAL PRIMARY KEY, userName VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL);";
    private static final String INSERT_TEST2 = "CREATE TABLE IF NOT EXISTS TEST2 " +
        "(id SERIAL PRIMARY KEY, userName VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL);";
    private static final String INSERT_TEST3 = "CREATE TABLE IF NOT EXISTS TEST3 " +
        "(id SERIAL PRIMARY KEY, userName VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL);";
    private static final String INSERT_FAIL = "CREATE TEST1 " +
        "(id SERIAL PRIMARY KEY, userName VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL);";

    //Section for database clean up statements
    private static final String CLEAN_UP_TEST1 = "DELETE FROM TEST1";
    private static final String DROP_TEST1 = "DROP TABLE TEST1;";
    private static final String CLEAN_UP_TEST2 = "DELETE FROM TEST2";
    private static final String DROP_TEST2 = "DROP TABLE TEST2;";
    private static final String CLEAN_UP_TEST3 = "DELETE FROM TEST3";
    private static final String DROP_TEST3 = "DROP TABLE TEST3;";

    //Sets the clean up statements in an array to maximize code reuse
    //during clean up
    private static final String[] cleanupStatements = new String[]
    {
        CLEAN_UP_TEST1, DROP_TEST1,
        CLEAN_UP_TEST2, DROP_TEST2,
        CLEAN_UP_TEST3, DROP_TEST3
    };

    /**
     * Setup the testing environment
     */
    @BeforeAll
    public static void setup()
    {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
        baseDao = Mockito.mock(BaseDaoImpl.class, Mockito.CALLS_REAL_METHODS);
    }

    /**
     * Clean up after testing is done. Removes the Test Table from the Database
     * and stops the Actor System.
     */
    @AfterAll
    public static void teardown()
    {
        SlickSession session = baseDao.createSession();
        system.registerOnTermination(session::close);

        //Remove all test tables from the database.
        for(String statement : cleanupStatements)
        {
            baseDao.executeStatement(statement, session, materializer);
        }

        TestKit.shutdownActorSystem(system);
    }

    /**
     * Test createSession
     */
    @Test
    public void testCreateSession()
    {
        SlickSession session = baseDao.createSession();
        system.registerOnTermination(session::close);

        assertTrue(session instanceof SlickSession);
    }

    /**
     * Test initTables success
     */
    @Test
    public void testInitTablesSuccess()
    {
        String[] testTablesSuccess = new String[]{INSERT_TEST1, INSERT_TEST2};

        SlickSession session = baseDao.createSession();
        system.registerOnTermination(session::close);

        try
        {
            assertTrue(baseDao.initTables(testTablesSuccess, session, materializer));
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage());
        }
    }

    /**
     * Test initTables failure
     */
    @Test
    public void testInitTablesFail()
    {
        String[] testTablesFailure = new String[]{INSERT_FAIL};

        SlickSession session = baseDao.createSession();
        system.registerOnTermination(session::close);

        //assert that a SQLException is thrown
        assertThrows(SQLException.class, () -> baseDao.initTables(testTablesFailure, session, materializer));
    }

    /**
     * Test executeStatement success
     */
    @Test
    public void testExecuteStatementSuccess()
    {
        SlickSession session = baseDao.createSession();
        system.registerOnTermination(session::close);

        assertTrue(baseDao.executeStatement(INSERT_TEST3, session, materializer));
    }

    /**
     * Test executeStatement failure
     */
    @Test void testExecuteStatementFail()
    {
        SlickSession session = baseDao.createSession();
        system.registerOnTermination(session::close);

        assertFalse(baseDao.executeStatement(INSERT_FAIL, session, materializer));
    }
}
