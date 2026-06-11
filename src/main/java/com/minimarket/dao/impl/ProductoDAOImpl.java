package com.minimarket.dao.impl;

import com.minimarket.dao.ProductoDAO;
import com.minimarket.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    private final Connection connection;

    public ProductoDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Producto findById(Integer id) throws SQLException {
        String sql = "SELECT Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras FROM producto WHERE Id_producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getInt("Id_producto"),
                            rs.getString("nombre_Producto"),
                            rs.getBigDecimal("Precio_unitario"),
                            rs.getInt("Id_categoria"),
                            rs.getString("codigo_barras")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Producto> findAll() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras FROM producto";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(new Producto(
                        rs.getInt("Id_producto"),
                        rs.getString("nombre_Producto"),
                        rs.getBigDecimal("Precio_unitario"),
                        rs.getInt("Id_categoria"),
                        rs.getString("codigo_barras")
                ));
            }
        }
        return productos;
    }

    @Override
    public boolean insert(Producto entity) throws SQLException {
        String sql = "INSERT INTO producto (nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getNombreProducto());
            ps.setBigDecimal(2, entity.getPrecioUnitario());
            ps.setInt(3, entity.getIdCategoria());
            ps.setString(4, entity.getCodigoBarras());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setIdProducto(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Producto entity) throws SQLException {
        String sql = "UPDATE producto SET nombre_Producto = ?, Precio_unitario = ?, Id_categoria = ?, codigo_barras = ? WHERE Id_producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getNombreProducto());
            ps.setBigDecimal(2, entity.getPrecioUnitario());
            ps.setInt(3, entity.getIdCategoria());
            ps.setString(4, entity.getCodigoBarras());
            ps.setInt(5, entity.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM producto WHERE Id_producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
