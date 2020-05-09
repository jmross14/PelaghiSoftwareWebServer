package com.pelaghisoftware.server.auth;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object containing a JWT for easy
 * Serialization/Deserialization to JSON
 */
public class JWTObject
{
    public String jwt;

    /**
     * Base Constructor
     */
    public JWTObject(){};

    /**
     * Constructor to set the JWT
     * @param jwt A JWT
     */
    public JWTObject(@JsonProperty("jwt") String jwt)
    {
        this.jwt = jwt;
    }

    /**
     * Getter for a JWT
     * @return String. A JWT
     */
    @JsonGetter("jwt")
    public String getJwt()
    {
        return jwt;
    }

    /**
     * Setter for a JWT
     * @param jwt A JWT
     */
    public void setJwt(String jwt)
    {
        this.jwt = jwt;
    }
}
