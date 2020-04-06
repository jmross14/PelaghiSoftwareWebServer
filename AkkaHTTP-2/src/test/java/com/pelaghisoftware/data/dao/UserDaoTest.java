package com.pelaghisoftware.data.dao;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.testkit.javadsl.TestKit;
import com.pelaghisoftware.data.dao.impl.UserDao;
import com.pelaghisoftware.data.dao.impl.base.BaseDaoImpl;
import com.pelaghisoftware.data.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDaoTest
{
    private static final Logger logger = LoggerFactory.getLogger(UserDaoTest.class);

    private static ActorSystem system;
    private static Materializer materializer;
    private static SlickSession session;

    //Sets up a list of Test Users to be inserted in the database
    private static final List<User> users =
            IntStream.range(0,40)
                .boxed()
                .map((i) -> new User("Name" + i, "Password" + i))
                .collect(Collectors.toList());

    private static UserDao userDao;

    //Queries to Add/Remove the Test Table from the database
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS TEST_USERS " +
            "(id SERIAL PRIMARY KEY, userName VARCHAR(100) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL);";
    private static final String CLEAN_UP_TABLE = "DELETE FROM TEST_USERS";
    private static final String DROP_TABLE = "DROP TABLE TEST_USERS;";

    /**
     * Setup the testing environment
     */
    @BeforeAll
    public static void setup()
    {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
        session = BaseDaoImpl.createSession();
        system.registerOnTermination(session::close);

        userDao = new UserDao("TEST_USERS", session, materializer);

        String [] userTables = new String[] {CREATE_TABLE};

        try
        {
            //Create the test table in the Database
            BaseDaoImpl.initTables(userTables, session, materializer);
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage());
        }

        //Insert all of the users in the database
        for(User user : users)
        {
            BaseDaoImpl.executeStatement(
                    "INSERT INTO TEST_USERS (userName, password) VALUES ('" +
                            user.getUserName() + "', '" + user.getPassword() + "');",
                    session,
                    materializer);
        }
    }

    /**
     * Clean up after testing is done. Removes the Test Table from the Database
     * and stops the Actor System.
     */
    @AfterAll
    public static void teardown()
    {
        BaseDaoImpl.executeStatement(CLEAN_UP_TABLE, session, materializer);
        BaseDaoImpl.executeStatement(DROP_TABLE, session, materializer);

        TestKit.shutdownActorSystem(system);
    }

    /**
     * Tests the get method
     */
    @Test
    public void testGet() throws InterruptedException, ExecutionException, TimeoutException
    {
        assertEquals(
                users.get(0).getPassword(), userDao.get("Name0").get().getPassword());
    }

    /**
     * Tests the getAll method
     */
    @Test
    public void testGetAll()
    {
        assertEquals(users.get(0).getUserName(), userDao.getAll().get(0).getUserName());
    }

    /**
     * Tests the insert method
     */
    @Test
    public void testInsert()
    {
        User testUser = new User("test", "test");

        assertTrue(userDao.insert(testUser));
    }

    /**
     * Tests the update method
     */
    @Test
    public void testUpdate()
    {
        User testUser = new User("Name1", "changedPassword");

        assertTrue(userDao.update(testUser));
    }

    /**
     * Tests the delete method
     */
    @Test
    public void testDelete()
    {
        User testUser = new User("Name2", "Password2");

        assertTrue(userDao.delete(testUser));
    }
}
