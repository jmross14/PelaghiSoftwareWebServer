package com.pelaghisoftware.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pelaghisoftware.data.TableInitConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class to store a User's information. Set up to use Jackson to
 * marshall/unmarshall data to/from JSON.
 */
@Entity
@Table(name = TableInitConstants.SITE_USERS)
public class User
{
    private String userName;
    private String password;

    /**
     * Create an empty User
     */
    public User(){};

    /**
     * Create a User with the specified information
     * @param userName The user's username
     * @param password The user's password
     */
    @JsonCreator
    public User(
            @JsonProperty("userName") String userName,
            @JsonProperty("password") String password)
    {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Gets the User's username
     * @return String. The user's username
     */
    @Id
    @Column(name = "userName")
    @JsonGetter("userName")
    public String getUserName()
    {
        return userName;
    }

    /**
     * Sets the User's username
     * @param userName The user's username
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }


    /**
     * Gets the User's password
     * @return String. The user's password
     */
    @JsonGetter("password")
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the User's password
     * @param password The user's password
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
}
