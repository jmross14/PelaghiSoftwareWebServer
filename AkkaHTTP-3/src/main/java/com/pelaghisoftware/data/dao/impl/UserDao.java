package com.pelaghisoftware.data.dao.impl;
import com.pelaghisoftware.data.dao.Dao;
import com.pelaghisoftware.data.DatabaseCommonOps;
import com.pelaghisoftware.data.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * DAO to perform crud operations for Users.
 */
public class UserDao implements Dao<User>
{
    private final static Logger logger =
            LoggerFactory.getLogger(UserDao.class);

    protected SessionFactory sessionFactory;

    /**
     * Creates an UserDao object that will query the
     * SITE_USERS table
     */
    public UserDao(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Gets the specified user from the Database
     * @param id The userName for the specified user
     * @return Optional. Will return a blank User if no user was
     *         found.
     */
    @Override
    public Optional<User> get(String id)
    {
        Session session = sessionFactory.openSession();

        User user = session.get(User.class, id);

        session.close();

        if(user == null)
        {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /**
     * Gets all users from the Database
     * @return List with all Users in the Database
     */
    @Override
    public List<User> getAll()
    {
        Session session = sessionFactory.openSession();

        List<User> users =
                DatabaseCommonOps.loadAllData(User.class, session);

        session.close();

        return users;
    }

    /**
     * Inserts a new user into the database
     * @param user The user to insert into the database.
     * @return True if the insertion was successful.
     *         False otherwise
     */
    @Override
    public boolean insert(User user)
    {
        Session session = sessionFactory.openSession();

        Transaction tx = null;
        try
        {
            tx = session.beginTransaction();

            session.save(user);

            tx.commit();

            session.close();
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            session.close();
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Updates the specified user's information.
     * @param user The user to update
     * @return True if the update was successful.
     *         False otherwise
     */
    @Override
    public boolean update(User user)
    {
        Session session = sessionFactory.openSession();

        Transaction tx = null;
        try
        {
            tx = session.beginTransaction();

            session.update(user);

            tx.commit();

            session.close();
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            session.close();
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete's the specified user's information
     * @param user The user to delete
     * @return True if the deletion was successful.
     *         False otherwise
     */
    @Override
    public boolean delete(User user)
    {
        Session session = sessionFactory.openSession();

        Transaction tx = null;
        try
        {
            tx = session.beginTransaction();

            session.delete(user);

            tx.commit();

            session.close();
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            session.close();
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }
}
