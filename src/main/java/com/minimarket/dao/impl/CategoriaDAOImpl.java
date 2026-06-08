package com.minimarket.dao.impl;

import com.minimarket.dao.CategoriaDAO;
import com.minimarket.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    private final Connection connection;

    public CategoriaDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Categoria findById(Integer id) throws SQLException {
        String sql = "SELECT Id_categoria, nombre_categoria FROM categoria WHERE Id_categoria = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(
                            rs.getInt("Id_categoria"),
                            rs.getString("nombre_categoria")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Categoria> findAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT Id_categoria, nombre_categoria FROM categoria";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(
                        rs.getInt("Id_categoria"),
                        rs.getString("nombre_categoria")
                ));
            }
        }
        return categorias;
    }

    @Override
    public boolean insert(Categoria entity) throws SQLException {
        String sql = "INSERT INTO categoria (nombre_categoria) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getNombreCategoria());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setIdCategoria(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Categoria entity) throws SQLException {
        String sql = "UPDATE categoria SET nombre_categoria = ? WHERE Id_categoria = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getNombreCategoria());
            ps.setInt(2, entity.getIdCategoria());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE Id_categoria = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
