package com.pelaghisoftware.server.routes;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.pelaghisoftware.data.actors.operations.DBOperations;
import com.pelaghisoftware.data.entity.User;
import com.pelaghisoftware.server.auth.operations.AuthOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static akka.http.javadsl.server.PathMatchers.*;
import static akka.pattern.Patterns.ask;

/**
 * Class with all User Entity Endpoints
 */
public class UserRoutes extends AllDirectives
{
    private final ActorRef authAccessor;
    private final ActorRef userAccessor;
    private final ActorRef responseResolver;
    private final Duration duration;

    /**
     * Constructor
     * @param authAccessor Actor to perform authentication operations
     * @param userAccessor Actor to access User Entities from the database
     * @param responseResolver Actor to create an HttpResponse
     * @param duration Time duration before an operation fails
     */
    public UserRoutes(ActorRef authAccessor, ActorRef userAccessor, ActorRef responseResolver, Duration duration)
    {
        this.authAccessor = authAccessor;
        this.userAccessor = userAccessor;
        this.responseResolver = responseResolver;
        this.duration = duration;
    }

    /**
     * Get all routes concatenated together for cleanliness in server code
     * @return Route. Endpoints for accessing User entities
     */
    public Route getUserRoutes()
    {
        return concat(
            getAllUsersRoute(),
            getUser(),
            addUser(),
            updateUser(),
            deleteUser()
        );
    }

    /**
     * Endpoint to get all Users
     * @return Route. An endpoint
     */
    private Route getAllUsersRoute()
    {
        return get(() ->
            path(segment("user"), () ->
                optionalHeaderValue(AuthOperations.getAuthorizationHeader, jwt ->
                {
                    //Function to get all user entities after authentication is complete
                    Supplier<CompletionStage<HttpResponse>> allUsersResponse = () ->
                        ask(userAccessor, new DBOperations.GetAllEntities(), duration)
                            .thenApply(List.class::cast)
                            .thenCompose(userList ->
                                ask(responseResolver,
                                    new DBOperations.GetAllEntities((List<User>)userList),
                                                                    duration))
                            .thenApply(HttpResponse.class::cast);

                    //Perform Authentication and returns an HttpResponse
                    CompletionStage<HttpResponse> response = AuthOperations.authCheck(authAccessor,
                                                                                      responseResolver,
                                                                                      jwt,
                                                                                      duration,
                                                                                      allUsersResponse);

                    return completeWithFuture(response);
                }))
        );
    }

    /**
     * Endpoint to get a User Entity
     * @return Route. An endpoint
     */
    public Route getUser()
    {
        return get(() ->
                path(segment("user")
                    .slash()
                    .concat(segment()), (String userName) ->
                        optionalHeaderValue(AuthOperations.getAuthorizationHeader, jwt ->
                        {
                            //Function to get a user entity after authentication
                            Supplier<CompletionStage<HttpResponse>> userResponse = () ->
                                ask(userAccessor, new DBOperations.GetEntity(userName), duration)
                                    .thenApply(Optional.class::cast)
                                    .thenCompose(entity -> ask(responseResolver,
                                                               new DBOperations.GetEntity(entity),
                                                               duration))
                                    .thenApply(HttpResponse.class::cast);

                            //Performs authentication and then returns an HttpResponse
                            CompletionStage<HttpResponse> response = AuthOperations.authCheck(authAccessor,
                                                                                              responseResolver,
                                                                                              jwt,
                                                                                              duration,
                                                                                              userResponse);

                            return completeWithFuture(response);
                        }))
        );
    }

    /**
     * Endpoint to add a user
     * @return Route. An endpoint
     */
    private Route addUser()
    {
        return post(() ->
            path(segment("user").slash().concat("add"), () ->
                optionalHeaderValue(AuthOperations.getAuthorizationHeader, jwt ->
                    entity(Jackson.unmarshaller(User.class),
                        user ->
                    {
                        //Function to insert a user
                        Supplier<CompletionStage<HttpResponse>> insertResponse = () ->
                            ask(userAccessor, new DBOperations.InsertEntity(Optional.of(user)), duration)
                                .thenApply(DBOperations.InsertEntity.class::cast)
                                .thenCompose(message -> ask(responseResolver, message, duration))
                                .thenApply(HttpResponse.class::cast);

                        //Function to perfomr authentication and return an HttpResponse
                        CompletionStage<HttpResponse> response = AuthOperations.authCheck(authAccessor,
                                                                                          responseResolver,
                                                                                          jwt,
                                                                                          duration,
                                                                                          insertResponse);
                        return completeWithFuture(response);
                    }))
            )
        );
    }

    /**
     * Endpoint to update an User entity
     * @return Route. An endpoint
     */
    private Route updateUser()
    {
        return put(() ->
            path(segment("user").slash().concat("update"), () ->
                optionalHeaderValue(AuthOperations.getAuthorizationHeader, jwt ->
                    entity(Jackson.unmarshaller(User.class),
                        user ->
                    {
                        //Function to update a user after authentication
                        Supplier<CompletionStage<HttpResponse>> updateResponse = () ->
                            ask(userAccessor, new DBOperations.UpdateEntity(Optional.of(user)), duration)
                                .thenApply(DBOperations.UpdateEntity.class::cast)
                                .thenCompose(message -> ask(responseResolver, message, duration))
                                .thenApply(HttpResponse.class::cast);

                        //Authenticate a user and return an HttpResponse
                        CompletionStage<HttpResponse> response = AuthOperations.authCheck(authAccessor,
                                                                                          responseResolver,
                                                                                          jwt,
                                                                                          duration,
                                                                                          updateResponse);
                        return completeWithFuture(response);
                    })))
        );
    }

    /**
     * Endpoint to delete a user entity
     * @return Route. An endpoint
     */
    private Route deleteUser()
    {
        return delete(() ->
            path(segment("user").slash().concat("delete"), () ->
                optionalHeaderValue(AuthOperations.getAuthorizationHeader, jwt ->
                    entity(Jackson.unmarshaller(User.class),
                        user ->
                    {
                        //Function to delete a user after authentication
                        Supplier<CompletionStage<HttpResponse>> deleteResponse = () ->
                            ask(userAccessor, new DBOperations.DeleteEntity(Optional.of(user)), duration)
                                .thenApply(DBOperations.DeleteEntity.class::cast)
                                .thenCompose(message -> ask(responseResolver, message, duration))
                                .thenApply(HttpResponse.class::cast);

                        //Authenticate a user and return an HttpResponse
                        CompletionStage<HttpResponse> response = AuthOperations.authCheck(authAccessor,
                                                                                          responseResolver,
                                                                                          jwt,
                                                                                          duration,
                                                                                          deleteResponse);

                        return completeWithFuture(response);
                    }))
            )
        );
    }


}
