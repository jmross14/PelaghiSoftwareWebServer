package com.pelaghisoftware.data.entity;

import com.fasterxml.jackson.annotation.*;
import com.pelaghisoftware.data.constants.TableInitConstants;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class User
{
    private String userName;
    @JsonIgnore
    private String encryptedPassword;

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
        this.userName = userName == null ? "" : userName;
        this.encryptedPassword = password;
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
    @Column(name = "encryptedPassword")
    public String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    /**
     * Sets the User's password
     * @param password The user's password
     */
    public void setEncryptedPassword(String password)
    {
        this.encryptedPassword = password;
    }
}
