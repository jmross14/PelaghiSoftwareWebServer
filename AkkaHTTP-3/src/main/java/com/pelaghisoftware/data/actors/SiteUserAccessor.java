package com.pelaghisoftware.data.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.pelaghisoftware.data.actors.operations.DBOperations;
import com.pelaghisoftware.data.dao.Dao;
import com.pelaghisoftware.data.dao.impl.UserDao;
import com.pelaghisoftware.data.entity.User;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Actor to access the database information for a user entity
 */
public class SiteUserAccessor extends AbstractActor
{
    private final Logger logger = LoggerFactory.getLogger(SiteUserAccessor.class);

    //Used to encrypt passwords and validate passwords
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final Dao<User> userDao;

    /**
     * Creates props for a new SiteUserAccessor
     * @param sessionFactory Session Factory to create new sessions with the database
     * @return
     */
    public static Props props(SessionFactory sessionFactory)
    {
        return Props.create(SiteUserAccessor.class, () -> new SiteUserAccessor(sessionFactory));
    }

    /**
     * Constructor
     * @param sessionFactory Session Factory to create new sessions with the database
     */
    public SiteUserAccessor(SessionFactory sessionFactory)
    {
        userDao = new UserDao(sessionFactory);
    }

    /**
     * Runs when receiving a message
     * @return
     */
    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(DBOperations.GetEntity.class, value ->
                    //Send the return message to the sender
                    getSender().tell(userDao.get(value.id), self())
                )
                .match(DBOperations.GetAllEntities.class, value ->
                    //Send the return message to the sender
                    getSender().tell(userDao.getAll(), self())
                )
                .match(DBOperations.InsertEntity.class, value ->
                {
                    boolean completed = false;
                    Optional<User> responseUser = Optional.empty();

                    //Check to make sure value received isn't empty.
                    if(value.entity.isPresent())
                    {
                        User user = (User)value.entity.get();

                        Optional<User> userCheck = userDao.get(user.getUserName());

                        //Check to see if user is in the database
                        if(userCheck.isPresent())
                        {
                            responseUser = userCheck;
                            completed = false;
                        }
                        //Check the incoming user to see if the password is null
                        else if(user.getEncryptedPassword() == null)
                        {
                            completed = false;
                        }
                        //Incoming user is good to go. Insert the new user in the database.
                        else
                        {
                            String encodedPassword = encoder.encode(user.getEncryptedPassword());
                            user.setEncryptedPassword(encodedPassword);
                            completed = userDao.insert(user);
                        }
                    }

                    //Send the return message to the sender
                    getSender().tell(new DBOperations.InsertEntity(responseUser, completed), self());
                })
                .match(DBOperations.UpdateEntity.class, value ->
                {
                    boolean completed = false;
                    boolean notFound = false;

                    //Check to make sure value received isn't empty.
                    if(value.entity.isPresent())
                    {
                        User user = (User)value.entity.get();

                        Optional<User> userCheck = userDao.get(user.getUserName());

                        //Check to make sure the user exists in the database
                        if(userCheck.isPresent())
                        {
                            //If the incoming value does not have a password set the password to the old one
                            if(user.getEncryptedPassword() == null)
                            {
                                user.setEncryptedPassword(userCheck.get().getEncryptedPassword());
                            }
                            else
                            {
                                String encodedPassword = encoder.encode(user.getEncryptedPassword());
                                user.setEncryptedPassword(encodedPassword);
                            }

                            //Update the user in the database
                            completed = userDao.update(user);
                        }
                        //If the incoming user is not found in the database
                        else
                        {
                            notFound = true;
                        }
                    }

                    //Send the return message to the sender
                    getSender().tell(new DBOperations.UpdateEntity(completed, notFound), self());
                })
                .match(DBOperations.DeleteEntity.class, value ->
                {
                    boolean completed = false;
                    boolean notFound = false;

                    //Check to make sure value received isn't empty.
                    if(value.entity.isPresent())
                    {
                        User user = (User)value.entity.get();

                        Optional<User> userCheck = userDao.get(user.getUserName());

                        //Check to make sure the incoming user exists in the database
                        if(userCheck.isPresent())
                        {
                            completed = userDao.delete(user);
                        }
                        else
                        {
                            notFound = true;
                        }
                    }

                    //Send the return message to the sender
                    getSender().tell(new DBOperations.DeleteEntity(completed, notFound), self());
                })
                .build();

    }
}
