package com.pelaghisoftware.data.dao.impl.base;

import akka.stream.Materializer;
import akka.stream.alpakka.slick.javadsl.Slick;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.javadsl.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class contains the static methods for common database operations.
 * All other DAO implementations should extend this class.
 *
 * Note: Class is not meant to be instantiated. Making the class abstract
 * allows for the use of the static methods but prevents instantiation.
 */
public abstract class BaseDaoImpl
{
    private static final Logger logger = LoggerFactory.getLogger(BaseDaoImpl.class);

    /**
     * Creates a Database Session.
     * @return SlickSession
     */
    public static SlickSession createSession()
    {
        return SlickSession.forConfig("slick-postgres");
    }

    /**
     * Initializes the tables for the Database.
     * Note: Please make sure the queries are start with:
     * "CREATE TABLE IF NOT EXISTS". This will ensure that it only
     * tries to create the table if it does not exist
     * @param tableQueries String array of the CREATE TABLE queries
     * @param session Current SlickSession
     * @param materializer Current ActorMaterializer
     * @return True if the queries were successful. Returns boolean to make testing easier.
     * @throws SQLException Will throw an exception if any of the
     *      queries were unable to be processed.
     */
    public static boolean initTables(String [] tableQueries, SlickSession session, Materializer materializer) throws SQLException
    {
        for(String tableQuery : tableQueries)
        {
            //Will throw a SQLException if there is a problem running the SQL statement
            if(!executeStatement(tableQuery, session, materializer))
            {
                logger.error("Query was incorrect or unable to be added for: {}", tableQuery);
                throw new SQLException();
            }
        }

        return true;
    }

    /**
     * Will execute the specified SQL statement. Use only for statements where there
     * should be no return value.
     * @param statement The SQL Query
     * @param session The current SlickSession
     * @param materializer The current ActorMaterializer
     * @return True if the statement is completed with no exceptions. False otherwise
     */
    public static boolean executeStatement(String statement, SlickSession session, Materializer materializer)
    {
        try
        {
            Source.single(statement)
                    .runWith(Slick.sink(session), materializer)
                    .toCompletableFuture()
                    .get(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            return false;
        }

        return true;
    }
}
