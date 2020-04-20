package com.pelaghisoftware.data.entity.response.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to get error messages for HTTP Responses. Set up to use
 * Jackson to marshall/unmarshall data to/from JSON.
 */
public class ErrorMessage
{
    private String error;

    /**
     * Create an empty ErrorMessage
     */
    public ErrorMessage(){};

    /**
     * Create an ErrorMessage with the specified message
     * @param error Error Message
     */
    @JsonCreator
    public ErrorMessage(
            @JsonProperty("error") String error)
    {
        this.error = error;
    }

    /**
     * Gets the error message.
     * @return String. The error message.
     */
    @JsonGetter("error")
    public String getError()
    {
        return error;
    }

    /**
     * Get an ErrorMessage object for Resource Not Found
     * @return ErrorMessage
     */
    public static ErrorMessage resourceNotFoundMessage()
    {
        return new ErrorMessage("Resource not found");
    }

    /**
     * Get an ErrorMessage object for Bad Request
     * @return ErrorMessage
     */
    public static ErrorMessage badRequestMessage()
    {
        return new ErrorMessage("Bad Request made. " +
                "Double check your request");
    }

    /**
     * Get an ErrorMessage object for when the User Already Exists
     * @return ErrorMessage
     */
    public static ErrorMessage userAlreadyExistsMessage()
    {
        return new ErrorMessage("User Already Exists. " +
                "Double check your request");
    }

    /**
     * Get an ErrorMessage object for User Does Not Exist
     * @return ErrorMessage
     */
    public static ErrorMessage userDoesNotExistMessage()
    {
        return new ErrorMessage("User does not Exist. " +
                "Double check your request");
    }
}