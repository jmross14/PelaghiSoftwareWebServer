package com.pelaghisoftware.server.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.http.javadsl.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pelaghisoftware.data.actors.operations.DBOperations;
import com.pelaghisoftware.server.auth.operations.AuthOperations;
import com.pelaghisoftware.server.response.messages.ErrorMessage;

/**
 * Actor to create an HttpResponse
 */
public class ResponseResolver extends AbstractActor
{
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Create the props needed to initialize a Response resolver in the actor system
     * @return Props for a Response Resolver.
     */
    public static Props props()
    {
        return Props.create(ResponseResolver.class, () -> new ResponseResolver());
    }

    /**
     * Constructor
     */
    public ResponseResolver(){}

    /**
     * Runs when a message is received
     * @return
     */
    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                //Response that returns a list of all entities
                .match(DBOperations.GetAllEntities.class, value ->
                {
                    HttpResponse response = createResponse(StatusCodes.OK, ContentTypes.APPLICATION_JSON, value.entities);

                    //Send the response back to the sender
                    getSender().tell(response, self());
                })
                //Response that returns an entity
                .match(DBOperations.GetEntity.class, value ->
                {
                    HttpResponse response = null;
                    //Checks if the entity is present
                    if(value.entity.isPresent())
                    {
                        response = createResponse(StatusCodes.OK, ContentTypes.APPLICATION_JSON, value.entity.get());
                    }
                    //Runs if the requested entity is not found
                    else
                    {
                        response = createResponse(StatusCodes.NOT_FOUND, ContentTypes.APPLICATION_JSON, ErrorMessage.resourceNotFoundMessage());
                    }

                    //Send the response back to the sender
                    getSender().tell(response, self());
                })
                //Response that responds to a request to insert an entiry
                .match(DBOperations.InsertEntity.class, value ->
                {
                    HttpResponse response = null;
                    //runs if the requested entity already exists.
                    if(value.entity.isPresent())
                    {
                        response = createResponse(StatusCodes.BAD_REQUEST, ContentTypes.APPLICATION_JSON, ErrorMessage.userAlreadyExistsMessage());
                    }
                    //runs if there was a failure on insert.
                    else if(!value.completed)
                    {
                        response = createResponse(StatusCodes.BAD_REQUEST, ContentTypes.APPLICATION_JSON, ErrorMessage.badRequestMessage());
                    }
                    //runs when the entity was inserted and is good to go
                    else
                    {
                        response = createResponse(StatusCodes.NO_CONTENT, null, null);
                    }

                    //Send the response back to the sender
                    getSender().tell(response, self());
                })
                //Response for when a user is updated
                .match(DBOperations.UpdateEntity.class, value ->
                {
                    HttpResponse response = null;
                    //Runs when the incoming user was not found
                    if(value.notFound)
                    {
                        response = createResponse(StatusCodes.NOT_FOUND, ContentTypes.APPLICATION_JSON, ErrorMessage.userDoesNotExistMessage());
                    }
                    //Runs if there is an error in updating
                    else if(!value.completed)
                    {
                        response = createResponse(StatusCodes.BAD_REQUEST, ContentTypes.APPLICATION_JSON, ErrorMessage.badRequestMessage());
                    }
                    //Runs if the user was updated and is good to go
                    else
                    {
                        response = createResponse(StatusCodes.NO_CONTENT, null, null);
                    }

                    //Send the response back to the sender
                    getSender().tell(response, self());
                })
                //Response for when a user is deleted
                .match(DBOperations.DeleteEntity.class, value ->
                {
                    HttpResponse response = null;

                    //Runs when the incoming user was not found
                    if(value.notFound)
                    {
                        response = createResponse(StatusCodes.NOT_FOUND, ContentTypes.APPLICATION_JSON, ErrorMessage.userDoesNotExistMessage());
                    }
                    //Runs if there is an error in deleting
                    else if(!value.completed)
                    {
                        response = createResponse(StatusCodes.BAD_REQUEST, ContentTypes.APPLICATION_JSON, ErrorMessage.badRequestMessage());
                    }
                    //Runs if the user was deleted and is good to go
                    else
                    {
                        response = createResponse(StatusCodes.NO_CONTENT, null, null);
                    }

                    //Send the response back to the sender
                    getSender().tell(response, self());
                })
                //Response for login
                .match(AuthOperations.JwtMessage.class, value ->
                {
                    HttpResponse response = null;

                    //runs if login failed.
                    if(value.jwt == null)
                    {
                        response = createResponse(StatusCodes.UNAUTHORIZED, ContentTypes.APPLICATION_JSON, ErrorMessage.usernamePasswordIncorrect());
                    }
                    //runs if login succeeded
                    else
                    {
                        response = createResponse(StatusCodes.OK, ContentTypes.APPLICATION_JSON, value.jwt);
                    }

                    //Send the response back to the sender
                    getSender().tell(response, self());
                })
                //Response for when an unauthorized operation occurs.
                .match(AuthOperations.Unauthorized.class, value ->
                {
                    HttpResponse response = createResponse(value.code, ContentTypes.APPLICATION_JSON, value.message);

                    //Send the response back to the sender
                    sender().tell(response, self());
                })
                .build();
    }

    /**
     * Create an HttpResponse
     * @param statusCode The Status Code for the response
     * @param type The Content-Type for the response
     * @param object The object to serialize into the message
     * @return HttpResponse. Response to the user
     */
    private HttpResponse createResponse(StatusCode statusCode, ContentType type, Object object)
    {
        try
        {
            HttpResponse response = HttpResponse.create().withStatus(statusCode);

            if(type != null && object != null)
            {
                response = response.withEntity(type, mapper.writeValueAsString(object).getBytes());
            }

            return response;
        }
        catch (JsonProcessingException e)
        {
            return HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
