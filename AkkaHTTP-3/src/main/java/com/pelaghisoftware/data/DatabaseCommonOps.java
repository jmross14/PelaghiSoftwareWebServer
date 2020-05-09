package com.pelaghisoftware.data;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;

/**
 * This class contains the static methods for common database
 * operations.
 *
 * Note: Class is not meant to be instantiated.
 */
public class DatabaseCommonOps
{
    private static final Logger logger =
            LoggerFactory.getLogger(DatabaseCommonOps.class);

    /**
     * Creates a Session Factory for the database.
     * @return Optional of Session Factory
     */
    public static Optional<SessionFactory> createSessionFactory()
    {
        SessionFactory sessionFactory;

        final StandardServiceRegistry registry =
            new StandardServiceRegistryBuilder()
            .configure()
            .build();
        try
        {
            sessionFactory = new MetadataSources(registry)
                .buildMetadata()
                .buildSessionFactory();

            return Optional.of(sessionFactory);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            StandardServiceRegistryBuilder.destroy(registry);
            return Optional.empty();
        }
    }

    /**
     * Returns a list of all of the specified entities in the
     * database
     * @param type Entity Class to search the db for.
     * @param session A current session
     * @param <T> The type of Entity
     * @return List of the entities specified by the type param
     */
    public static <T> List<T> loadAllData(Class<T> type,
                                          Session session)
    {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        criteria.from(type);

        List<T> data = session
            .createQuery(criteria)
            .getResultList();
        return data;
    }
}
