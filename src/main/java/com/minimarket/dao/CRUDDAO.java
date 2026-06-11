package com.minimarket.dao;

import java.sql.SQLException;
import java.util.List;

public interface CRUDDAO<T, ID> {
    T findById(ID id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean insert(T entity) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(ID id) throws SQLException;
}
