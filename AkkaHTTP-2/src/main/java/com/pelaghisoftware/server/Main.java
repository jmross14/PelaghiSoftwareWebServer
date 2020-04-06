package com.pelaghisoftware.server;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.pelaghisoftware.data.TableInitConstants;
import com.pelaghisoftware.data.dao.Dao;
import com.pelaghisoftware.data.dao.impl.UserDao;
import com.pelaghisoftware.data.dao.impl.base.BaseDaoImpl;
import com.pelaghisoftware.data.entity.response.message.ErrorMessage;
import com.pelaghisoftware.data.entity.User;

import akka.stream.alpakka.slick.javadsl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.*;

public class Main extends AllDirectives
{
    final static Logger logger = LoggerFactory.getLogger(Main.class);
    /**
     * Main Method
     * @param args Arguments passed in when starting the program
     * @throws Exception Shutsdown the system if an Exception is thrown in the
     *  main method.
     */
    public static void main(String[] args) throws Exception
    {
        //Create the parent actor system that will be used to process everything.
        ActorSystem system = ActorSystem.create("routes");

        //Server instance
        final Http http = Http.get(system);

        //Used to process the requests into responses
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //Set up the database session
        SlickSession session = BaseDaoImpl.createSession();
        system.registerOnTermination(session::close);

        //Initialize Tables
        BaseDaoImpl.initTables(TableInitConstants.TABLE_INIT_ARRAY, session, materializer);
        logger.info("Initializing Database Tables");

        //In order to access all directives we need an instance where the routes are defined. I used Main as my class
        //name. Use whatever you name your class.
        Main app = new Main();

        //Maps the routes into the system allowing the HttpRequest to be used and output a HttpRequest back to the User.
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute(materializer, session).flow(system, materializer);

        //Binds the server to a port to start accepting requests
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8099), materializer);

        logger.info("Server online at http://localhost:8099/");
        logger.info("Press Return to stop...");

        System.in.read(); //let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) //trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    private Route createRoute(ActorMaterializer materializer, SlickSession session)
    {
        return concat(
            get(() ->
                path(segment("user"), () ->
                {
                    //Create the database access object
                    Dao<User> userDao = new UserDao(session, materializer);

                    return complete(
                        StatusCodes.OK,
                        userDao.getAll(),
                        Jackson.marshaller()
                    );
                })
            ),
            get(() ->
                path(segment("user").slash().concat(segment()), (String userName) ->
                {
                    //Create the database access object
                    Dao<User> userDao = new UserDao(session, materializer);

                    User user = userDao.get(userName).get();

                    if(user.getUserName() == null)
                    {
                        return complete(
                            StatusCodes.NOT_FOUND,
                            ErrorMessage.resourceNotFoundMessage(),
                            Jackson.marshaller()
                        );
                    }
                    else
                    {
                        return complete(
                            StatusCodes.OK,
                            userDao.get(userName).get(),
                            Jackson.marshaller()
                        );
                    }
                })
            ),
            post(() ->
                path(segment("user").slash().concat("add"), () ->
                {
                    //Create the database access object
                    Dao<User> userDao = new UserDao(session, materializer);

                    return entity(Jackson.unmarshaller(User.class), user ->
                    {
                        User userCheck = userDao.get(user.getUserName()).get();

                        if(userCheck.getUserName() == null)
                        {
                            boolean status = userDao.insert(user);

                            if(!status)
                            {
                                return complete(
                                    StatusCodes.BAD_REQUEST,
                                    ErrorMessage.badRequestMessage(),
                                    Jackson.marshaller()
                                );
                            }
                            else
                            {
                                return complete(StatusCodes.NO_CONTENT);
                            }
                        }
                        else
                        {
                            return complete(
                                StatusCodes.BAD_REQUEST,
                                ErrorMessage.userAlreadyExistsMessage(),
                                Jackson.marshaller()
                            );
                        }
                    });
                })
            ),
            put(() ->
                path(segment("user").slash().concat("update"), () ->
                {
                    //Create the database access object
                    Dao<User> userDao = new UserDao(session, materializer);

                    return entity(Jackson.unmarshaller(User.class), user ->
                    {
                        User userCheck = userDao.get(user.getUserName()).get();

                        if(userCheck.getUserName() == null)
                        {
                            return complete(
                                StatusCodes.NOT_FOUND,
                                ErrorMessage.userDoesNotExistMessage(),
                                Jackson.marshaller()
                            );
                        }
                        else
                        {
                            boolean status = userDao.update(user);

                            if(!status)
                            {
                                return complete(
                                    StatusCodes.BAD_REQUEST,
                                    ErrorMessage.badRequestMessage(),
                                    Jackson.marshaller()
                                );
                            }
                            else
                            {
                                return complete(StatusCodes.NO_CONTENT);
                            }
                        }
                    });
                })
            ),
            delete(() ->
                path(segment("user").slash().concat("delete"), () ->
                {
                    //Create the database access object
                    Dao<User> userDao = new UserDao(session, materializer);

                    return entity(Jackson.unmarshaller(User.class), user ->
                    {
                        User userCheck = userDao.get(user.getUserName()).get();

                        if(userCheck.getUserName() == null)
                        {
                            return complete(
                                StatusCodes.NOT_FOUND,
                                ErrorMessage.userDoesNotExistMessage(),
                                Jackson.marshaller()
                            );
                        }
                        else
                        {
                            boolean status = userDao.delete(user);

                            if(!status)
                            {
                                return complete(
                                        StatusCodes.BAD_REQUEST,
                                        ErrorMessage.badRequestMessage(),
                                        Jackson.marshaller()
                                );
                            }
                            else
                            {
                                return complete(StatusCodes.NO_CONTENT);
                            }
                        }
                    });
                })
            )
        );
    }
}
