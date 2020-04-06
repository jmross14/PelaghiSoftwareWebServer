package com.pelaghisoftware.data.dao;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;

/**
 * Interface for all DAO to implement. Creates a specified API for
 * data access
 * @param <T> The type of Object to be used in the implementation
 */
public interface Dao<T>
{
    Optional<T> get(String id);
    List<T> getAll();
    boolean insert(T t);
    boolean update(T t);
    boolean delete(T t);
}
