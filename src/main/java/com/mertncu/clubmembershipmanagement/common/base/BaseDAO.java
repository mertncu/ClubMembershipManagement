package com.mertncu.clubmembershipmanagement.common.base;

import java.util.List;
import java.util.Optional;

/**
 * Generic BaseDAO interface defining standard CRUD operations.
 * 
 * @param <T> The Entity type (e.g., User, Event)
 */
public interface BaseDAO<T extends BaseEntity> {
    
    T save(T entity);
    
    boolean update(T entity);
    
    boolean delete(int id);
    
    Optional<T> findById(int id);
    
    List<T> findAll();
}
