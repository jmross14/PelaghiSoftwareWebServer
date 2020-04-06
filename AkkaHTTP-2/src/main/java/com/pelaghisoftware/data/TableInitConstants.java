package com.pelaghisoftware.data;

/**
 * This class contains all of the constants necessary to initialize the tables in
 * the database
 */
public class TableInitConstants
{
    //Section to specify the table name for the DAO classes

    public final static String SITE_USERS= "SITE_USERS";

    //Section to specify the queries to initialize the tables

    public final static String SITE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + SITE_USERS +
            "(id SERIAL PRIMARY KEY, userName VARCHAR(100) UNIQUE NOT NULL, " +
            "password VARCHAR(100) NOT NULL)";

    //Section to create an array of the queries to initialize the tables
    public final static String[] TABLE_INIT_ARRAY = new String[]
    {
        SITE_USERS_TABLE
    };
}
