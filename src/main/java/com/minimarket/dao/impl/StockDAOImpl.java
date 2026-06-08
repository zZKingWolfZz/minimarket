package com.minimarket.dao.impl;

import com.minimarket.dao.StockDAO;
import com.minimarket.model.Stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StockDAOImpl implements StockDAO {

    private final Connection connection;

    public StockDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Stock findById(Integer id) throws SQLException {
        String sql = "SELECT Id_stock, Cantidad, Id_Producto FROM stock WHERE Id_stock = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Stock(
                            rs.getInt("Id_stock"),
                            rs.getInt("Cantidad"),
                            rs.getInt("Id_Producto")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Stock> findAll() throws SQLException {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT Id_stock, Cantidad, Id_Producto FROM stock";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stocks.add(new Stock(
                        rs.getInt("Id_stock"),
                        rs.getInt("Cantidad"),
                        rs.getInt("Id_Producto")
                ));
            }
        }
        return stocks;
    }

    @Override
    public boolean insert(Stock entity) throws SQLException {
        String sql = "INSERT INTO stock (Cantidad, Id_Producto) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.getCantidad());
            ps.setInt(2, entity.getIdProducto());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setIdStock(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Stock entity) throws SQLException {
        String sql = "UPDATE stock SET Cantidad = ?, Id_Producto = ? WHERE Id_stock = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getCantidad());
            ps.setInt(2, entity.getIdProducto());
            ps.setInt(3, entity.getIdStock());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM stock WHERE Id_stock = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Stock> alertaStockMinimo(int limite) throws SQLException {
        List<Stock> stockMinimo = new ArrayList<>();
        String sql = "SELECT Id_stock, Cantidad, Id_Producto FROM stock WHERE Cantidad <= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockMinimo.add(new Stock(
                            rs.getInt("Id_stock"),
                            rs.getInt("Cantidad"),
                            rs.getInt("Id_Producto")
                    ));
                }
            }
        }
        return stockMinimo;
    }

    @Override
    public Stock findByProductoId(int idProducto) throws SQLException {
        String sql = "SELECT Id_stock, Cantidad, Id_Producto FROM stock WHERE Id_Producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Stock(
                            rs.getInt("Id_stock"),
                            rs.getInt("Cantidad"),
                            rs.getInt("Id_Producto")
                    );
                }
            }
        }
        return null;
    }
}
