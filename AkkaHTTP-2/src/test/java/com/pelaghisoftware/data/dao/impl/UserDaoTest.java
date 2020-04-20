package com.pelaghisoftware.data.dao.impl;

import com.pelaghisoftware.data.DatabaseCommonOps;
import com.pelaghisoftware.data.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDaoTest
{
    private static final Logger logger =
            LoggerFactory.getLogger(UserDaoTest.class);

    private static SessionFactory sessionFactory;

    //Sets up a list of Test Users to be inserted in the database
    private static final List<User> users =
            IntStream.range(0,40)
                .boxed()
                .map((i) -> new User("Name" + i,
                                     "Password" + i))
                .collect(Collectors.toList());

    /**
     * Setup the testing environment
     */
    @BeforeAll
    public static void setup()
    {
        sessionFactory =
                DatabaseCommonOps.createSessionFactory().get();
    }

    /**
     * Clean up after testing is done. Removes the Test Table
     * from the Database and stops the Actor System.
     */
    @AfterAll
    public static void teardown()
    {
        cleanUserTable();
        sessionFactory.close();
    }

    /**
     * Initializes the tables in the database before each
     * test is run
     */
    @BeforeEach
    public void initDb()
    {
        initUserTable();
    }

    /**
     * Clears out the data in the table after each
     * test is run
     */
    @AfterEach
    public void cleanDb()
    {
        cleanUserTable();
    }

    /**
     * Tests the get method
     */
    @Test
    public void testGet()
    {
        UserDao userDao = new UserDao(sessionFactory);

        User user = userDao.get("Name0").get();
        assertEquals("Name0", user.getUserName());
        assertEquals("Password0", user.getPassword());
    }

    /**
     * Tests the getAll method
     */
    @Test
    public void testGetAll()
    {
        UserDao userDao = new UserDao(sessionFactory);

        List<User> currentUser = userDao.getAll();
        assertEquals(40, currentUser.size());
    }

    /**
     * Tests the insert method
     */
    @Test
    public void testInsert()
    {
        UserDao userDao = new UserDao(sessionFactory);

        User user = new User("Test", "Test");
        userDao.insert(user);

        User newUser = userDao.get("Test").get();

        assertEquals(user.getUserName(), newUser.getUserName());
        assertEquals(user.getPassword(), newUser.getPassword());
    }

    /**
     * Tests the update method
     */
    @Test
    public void testUpdate()
    {
        UserDao userDao = new UserDao(sessionFactory);

        User user = new User("Name0", "1234");
        userDao.update(user);

        User newUser = userDao.get("Name0").get();

        assertEquals(user.getUserName(), newUser.getUserName());
        assertEquals(user.getPassword(), newUser.getPassword());
    }

    /**
     * Tests the delete method
     */
    @Test
    public void testDelete()
    {
        UserDao userDao = new UserDao(sessionFactory);

        User user = new User("Name0", "Password0");
        userDao.delete(user);

        Optional<User> newUser = userDao.get("Name0");

        assertTrue(newUser.isEmpty());
    }

    /**
     * Initializes the SITE_USERS Table in the h2 database
     */
    private static void initUserTable()
    {
        for(User user : users)
        {
            Session session = sessionFactory.openSession();

            Transaction tx = null;
            try
            {
                tx = session.beginTransaction();

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
     * Removes all users from the SITE_USERS h2 database
     */
    private static void cleanUserTable()
    {
        Session session = sessionFactory.openSession();
        List<User> currentUsers =
                DatabaseCommonOps.loadAllData(User.class, session);
        session.close();

        for(User user : currentUsers)
        {
            session = sessionFactory.openSession();

            Transaction tx = null;
            try
            {
                tx = session.beginTransaction();

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
