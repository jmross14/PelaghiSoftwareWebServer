package com.pelaghisoftware.data.actors.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Messages for Database Operations
 */
public class DBOperations
{
    /**
     * Message to get a single entity
     */
    public static class GetEntity
    {
        public String id;
        public Optional<?> entity;

        public GetEntity(){};

        public GetEntity(String id)
        {
            this.id = id;
        }

        public GetEntity(Optional<?> entity)
        {
            this.entity = entity;
        }

        public GetEntity(String id, Optional<?> entity)
        {
            this.id = id;
            this.entity = entity;
        }
    }

    /**
     * Message to get a list of all entities
     */
    public static class GetAllEntities
    {
        public List<?> entities = new ArrayList<>();

        public GetAllEntities(){}

        public GetAllEntities(List<?> entities)
        {
            this.entities = entities;
        }
    }

    /**
     * Message to insert and entity
     */
    public static class InsertEntity
    {
        public String id;
        public Optional<?> entity;
        public boolean completed;

        public InsertEntity(){}

        public InsertEntity(String id)
        {
            this.id = id;
        }

        public InsertEntity(Optional<?> entity)
        {
            this.entity = entity;
        }

        public InsertEntity(boolean completed)
        {
            this.completed = completed;
        }

        public InsertEntity(Optional<?> entity, boolean completed)
        {
            this.entity = entity;
            this.completed = completed;
        }
    }

    /**
     * Message to update an entity
     */
    public static class UpdateEntity
    {
        public String id;
        public Optional<?> entity;
        public boolean completed;
        public boolean notFound;

        public UpdateEntity(){}

        public UpdateEntity(String id)
        {
            this.id = id;
        }

        public UpdateEntity(Optional<?> entity)
        {
            this.entity = entity;
        }

        public UpdateEntity(boolean completed)
        {
            this.completed = completed;
        }

        public UpdateEntity(boolean completed, boolean notFound)
        {
            this.completed = completed;
            this.notFound = notFound;
        }
    }

    /**
     * Message to delete an entity
     */
    public static class DeleteEntity
    {
        public String id;
        public Optional<?> entity;
        public boolean completed;
        public boolean notFound;

        public DeleteEntity(){}

        public DeleteEntity(String id)
        {
            this.id = id;
        }

        public DeleteEntity(Optional<?> entity)
        {
            this.entity = entity;
        }

        public DeleteEntity(boolean completed)
        {
            this.completed = completed;
        }

        public DeleteEntity(boolean completed, boolean notFound)
        {
            this.completed = completed;
            this.notFound = notFound;
        }
    }
}
