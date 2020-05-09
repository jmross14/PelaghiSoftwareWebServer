package com.pelaghisoftware.server;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import static akka.pattern.Patterns.ask;

import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.pelaghisoftware.data.actors.SiteUserAccessor;
import com.pelaghisoftware.data.DatabaseCommonOps;
import com.pelaghisoftware.data.entity.User;

import com.pelaghisoftware.server.actors.AuthResolver;
import com.pelaghisoftware.server.actors.ResponseResolver;
import com.pelaghisoftware.server.auth.operations.AuthOperations;
import com.pelaghisoftware.server.routes.UserRoutes;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.*;

public class Server extends AllDirectives
{
    final static Logger logger =
            LoggerFactory.getLogger(Server.class);

    final static Map<String, ActorRef> dataAccessors = new HashMap<>();

    final static Duration duration = Duration.ofSeconds(1);

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
        final ActorMaterializer materializer =
            ActorMaterializer.create(system);

        //Set up the database session factory
        SessionFactory sessionFactory =
                DatabaseCommonOps.createSessionFactory().get();

        //Ensures that the session factory is closed when the system terminates
        system.registerOnTermination(sessionFactory::close);

        //Create the necessary actors and place them in a map
        dataAccessors.put("UserAccessor", system.actorOf(SiteUserAccessor.props(sessionFactory).withDispatcher("route-blocking-dispatcher"), "UserAccessor"));
        dataAccessors.put("AuthAccessor", system.actorOf(AuthResolver.props(dataAccessors.get("UserAccessor")), "AuthAccessor"));
        dataAccessors.put("ResponseResolver", system.actorOf(ResponseResolver.props(), "ResponseResolver"));


        //In order to access all directives we need an instance where the routes
        // are defined. I used Main as my class name.
        // Use whatever you name your class.
        Server app = new Server();

        //Maps the routes into the system allowing the HttpRequest to be used
        //and output a HttpRequest back to the User.
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow =
            app.createRoute()
            .flow(system, materializer);

        //Binds the server to a port to start accepting requests
        final CompletionStage<ServerBinding> binding =
            http.bindAndHandle(routeFlow,
                               ConnectHttp.toHost("localhost",
                                             8099),
                               materializer);

        logger.info("Server online at http://localhost:8099/");
        logger.info("Press Return to stop...");

        System.in.read(); //let it run until user presses retur
        binding
            //trigger unbinding from the port
            .thenCompose(ServerBinding::unbind)
            // and shutdown when done
            .thenAccept(unbound -> system.terminate());
    }

    /**
     * Returns an endpoint that user can access
     * @return Route. An endpoint
     */
    private Route createRoute()
    {
        //Gets all necessary ActorRefs for easier/cleaner use
        ActorRef responseResolver = dataAccessors.get("ResponseResolver");
        ActorRef authAccessor = dataAccessors.get("AuthAccessor");
        ActorRef userAccessor = dataAccessors.get("UserAccessor");

        //Provider for routes related to user entities
        UserRoutes userRoutes = new UserRoutes(authAccessor, userAccessor, responseResolver, duration);

        return concat(
            //Adds the user entity routes
            userRoutes.getUserRoutes(),
            //Adds the route for authentication
            post(() ->
                concat(
                    path(segment("auth"), () ->
                        entity(Jackson.unmarshaller(User.class),
                            user ->
                        {
                            CompletionStage<HttpResponse> response =
                                ask(authAccessor, user, duration)
                                    .thenApply(AuthOperations.JwtMessage.class::cast)
                                    .thenCompose(message -> ask(responseResolver, message, duration)
                                        .thenApply(HttpResponse.class::cast));

                            return completeWithFuture(response);
                        })
                )
            ))

        );
    }
}
