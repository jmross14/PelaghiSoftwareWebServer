package com.pelaghisoftware.data.dao;

import com.pelaghisoftware.data.DatabaseCommonOps;
import com.pelaghisoftware.data.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the BaseDaoImpl abstract class.
 */
public class DatabaseCommonOpsTest
{
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCommonOpsTest.class);

    private static final List<User> users =
            IntStream.range(0,40)
                    .boxed()
                    .map((i) -> new User("Name" + i, "Password" + i))
                    .collect(Collectors.toList());

    /**
     * Setup the testing environment
     */
    @BeforeAll
    public static void setup()
    {

    }

    /**
     * Clean up after testing is done. Removes the Test Table from the Database
     * and stops the Actor System.
     */
    @AfterAll
    public static void teardown()
    {

    }

    /**
     * Test createSession
     */
    @Test
    public void testCreateSession()
    {
        Optional<SessionFactory> sessionFactory = DatabaseCommonOps.createSessionFactory();

        assertTrue(sessionFactory.isPresent());
        assertTrue(sessionFactory.get() instanceof SessionFactory);

        sessionFactory.get().close();
    }

    /**
     * Test initTables success
     */
    @Test
    public void testLoadAllData()
    {
        SessionFactory sessionFactory = DatabaseCommonOps.createSessionFactory().get();

        initUserTable(sessionFactory);

        Session session = sessionFactory.openSession();
        List<User> users = DatabaseCommonOps.loadAllData(User.class, session);

        assertEquals(40, users.size());

        cleanUserTable(sessionFactory);
        sessionFactory.close();
    }

    /**
     * Initializes the SITE_USERS table in the h2 database
     * @param sessionFactory
     */
    private static void initUserTable(SessionFactory sessionFactory)
    {
        for(User user : users)
        {
            Session session = sessionFactory.openSession();

            Transaction tx = null;
            try
            {
                tx = session.beginTransaction();;

                session.save(user);

                tx.commit();
            }
            catch (Exception e)
            {
                if (tx != null)
                {
                    tx.rollback();
                }
                logger.error(e.getMessage());
            }
            finally
            {
                session.close();
            }

        }
    }

    /**
     * Clears the SITE_USERS table in the h2 database
     * @param sessionFactory
     */
    private static void cleanUserTable(SessionFactory sessionFactory)
    {
        Session session = sessionFactory.openSession();
        List<User> currentUsers = DatabaseCommonOps.loadAllData(User.class, session);
        session.close();

        for(User user : currentUsers)
        {
            session = sessionFactory.openSession();

            Transaction tx = null;
            try
            {
                tx = session.beginTransaction();;

                session.delete(user);

                tx.commit();
            }
            catch (Exception e)
            {
                if (tx != null)
                {
                    tx.rollback();
                }
                logger.error(e.getMessage());
            }
            finally
            {
                session.close();
            }
        }
    }
}
