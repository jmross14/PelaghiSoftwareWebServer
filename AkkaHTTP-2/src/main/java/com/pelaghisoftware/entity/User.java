package com.pelaghisoftware.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User
{
    private String userName;
    private String password;

    public User(){};

    @JsonCreator
    public User(
            @JsonProperty("userName") String userName,
            @JsonProperty("password") String password)
    {
        this.userName = userName;
        this.password = password;
    }

    @JsonGetter("userName")
    public String getUserName()
    {
        return userName;
    }

    @JsonGetter("password")
    public String getPassword()
    {
        return password;
    }
}
