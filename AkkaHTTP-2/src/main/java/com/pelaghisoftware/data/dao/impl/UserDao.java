package com.pelaghisoftware.data.dao.impl;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.alpakka.slick.javadsl.Slick;
import akka.stream.alpakka.slick.javadsl.SlickRow;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.pelaghisoftware.data.TableInitConstants;
import com.pelaghisoftware.data.dao.Dao;
import com.pelaghisoftware.data.dao.impl.base.BaseDaoImpl;
import com.pelaghisoftware.data.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * DAO to perform crud operations for Users.
 */
public class UserDao extends BaseDaoImpl implements Dao<User>
{
    private final static Logger logger = LoggerFactory.getLogger(UserDao.class);

    private String tableName;

    private SlickSession session;
    private Materializer materializer;

    /**
     * Creates an UserDao object that will query the SITE_USERS table
     * @param session Current SlickSession
     * @param materializer Current ActorMaterializer
     */
    public UserDao(SlickSession session, Materializer materializer)
    {
        this(TableInitConstants.SITE_USERS, session, materializer);
    }

    /**
     * Creates an UserDAO object that will query the specified table
     * @param tableName Table to be queried
     * @param session Current SlickSession
     * @param materializer Current ActorMaterializer
     */
    public UserDao(String tableName, SlickSession session, Materializer materializer)
    {
        this.session = session;
        this.materializer = materializer;
        this.tableName = tableName;
    }

    /**
     * Gets the specified user from the Database
     * @param id The userName for the specified user
     * @return Optional. Will return a blank User if no user was found.
     */
    @Override
    public Optional<User> get(String id)
    {
        String SELECT_USER = "SELECT userName, password FROM " + tableName + " WHERE userName = '" +
                id + "';";

        List<User> users = getUsers(SELECT_USER);

        User user = new User();

        //Only return the user if 1 user was found. Should not be an issue as userName is UNIQUE.
        if(users.size() == 1)
        {
            user = users.get(0);
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
        String SELECT_USERS = "SELECT userName, password FROM " + tableName + ";";

        List<User> users = getUsers(SELECT_USERS);

        return users;
    }

    /**
     * Inserts a new user into the database
     * @param user The user to insert into the database.
     * @return True if the insertion was successful. False otherwise
     */
    @Override
    public boolean insert(User user)
    {
        String INSERT_USER = "INSERT INTO " + tableName + " (userName, password) VALUES ('" +
                user.getUserName() + "', '" + user.getPassword() + "');";
        return executeStatement(
                INSERT_USER,
                session,
                materializer);
    }

    /**
     * Updates the specified user's information.
     * @param user The user to update
     * @return True if the update was successful. False otherwise
     */
    @Override
    public boolean update(User user)
    {
        String UPDATE_USER = "UPDATE " + tableName + " SET userName = '" + user.getUserName() + "', password = '" +
                user.getPassword() + "' WHERE userName = '" + user.getUserName() + "';";
        return executeStatement(
                UPDATE_USER,
                session,
                materializer);
    }

    /**
     * Delete's the specified user's information
     * @param user The user to delete
     * @return True if the deletion was successful. False otherwise
     */
    @Override
    public boolean delete(User user)
    {
        String DELETE_USER = "DELETE FROM " + tableName + " WHERE userName = '" + user.getUserName() + "';";
        return executeStatement(
                DELETE_USER,
                session,
                materializer);
    }

    /**
     * Gets a list of users based on the input query
     * @param query The query to get a list of users
     * @return List. All users that are returned from the query
     */
    private List<User> getUsers(String query)
    {
        //Create a source to store the users in
        Source<User, NotUsed> source = Slick.source(
                session,
                query,
                (SlickRow row) -> new User(row.nextString(), row.nextString()));

        //Runs the query and stores in a CompletionStage
        CompletionStage<List<User>> foundUserFuture = source.runWith(
                Sink.seq(),
                materializer);

        //Change the CompletionStage to a List of Users. If any exceptions occur
        //return an empty List.
        try
        {
            return foundUserFuture.toCompletableFuture().get(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            return new ArrayList<>();
        }
    }
}
