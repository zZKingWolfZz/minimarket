package com.minimarket.dao.impl;

import com.minimarket.dao.VentaDAO;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.model.Venta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VentaDAOImpl implements VentaDAO {

    private final Connection connection;

    public VentaDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean registrarVentaTransaccional(Venta venta) throws SQLException {
        boolean success = false;
        boolean originalAutoCommit = connection.getAutoCommit();

        String selectStockSql = "SELECT Id_stock, Cantidad FROM stock WHERE Id_Producto = ? FOR UPDATE";
        String updateStockSql = "UPDATE stock SET Cantidad = ? WHERE Id_stock = ?";
        String insertVentaSql = "INSERT INTO venta (Id_producto, cantidad, Precio_total, Fecha, Id_cliente) VALUES (?, ?, ?, ?, ?)";

        try {

            connection.setAutoCommit(false);

            int stockId = -1;
            int stockActual = -1;

            try (PreparedStatement psSelect = connection.prepareStatement(selectStockSql)) {
                psSelect.setInt(1, venta.getIdProducto());
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        stockId = rs.getInt("Id_stock");
                        stockActual = rs.getInt("Cantidad");
                    } else {
                        throw new InsufficientStockException("El producto seleccionado no está registrado en el inventario de stock.", 0, venta.getCantidad());
                    }
                }
            }

            if (stockActual < venta.getCantidad()) {
                throw new InsufficientStockException(
                        "Stock insuficiente. Disponible: " + stockActual + ", Requerido: " + venta.getCantidad(),
                        stockActual,
                        venta.getCantidad()
                );
            }

            int nuevoStock = stockActual - venta.getCantidad();
            try (PreparedStatement psUpdateStock = connection.prepareStatement(updateStockSql)) {
                psUpdateStock.setInt(1, nuevoStock);
                psUpdateStock.setInt(2, stockId);
                psUpdateStock.executeUpdate();
            }

            try (PreparedStatement psInsertVenta = connection.prepareStatement(insertVentaSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsertVenta.setInt(1, venta.getIdProducto());
                psInsertVenta.setInt(2, venta.getCantidad());
                psInsertVenta.setBigDecimal(3, venta.getPrecioTotal());
                psInsertVenta.setDate(4, Date.valueOf(venta.getFecha()));
                psInsertVenta.setInt(5, venta.getIdCliente());

                int rows = psInsertVenta.executeUpdate();
                if (rows > 0) {
                    try (ResultSet generatedKeys = psInsertVenta.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            venta.setIdVenta(generatedKeys.getInt(1));
                        }
                    }
                }
            }

            connection.commit();
            success = true;

        } catch (SQLException | RuntimeException e) {

            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            throw e; 
        } finally {

            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException autocommitEx) {
                System.err.println("Error restoring auto-commit: " + autocommitEx.getMessage());
            }
        }

        return success;
    }

    @Override
    public Venta findById(Integer id) throws SQLException {
        String sql = "SELECT Id_venta, Id_producto, cantidad, Precio_total, Fecha, Id_cliente FROM venta WHERE Id_venta = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Venta(
                            rs.getInt("Id_venta"),
                            rs.getInt("Id_producto"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("Precio_total"),
                            rs.getDate("Fecha").toLocalDate(),
                            rs.getInt("Id_cliente")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Venta> findAll() throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT Id_venta, Id_producto, cantidad, Precio_total, Fecha, Id_cliente FROM venta";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ventas.add(new Venta(
                        rs.getInt("Id_venta"),
                        rs.getInt("Id_producto"),
                        rs.getInt("cantidad"),
                        rs.getBigDecimal("Precio_total"),
                        rs.getDate("Fecha").toLocalDate(),
                        rs.getInt("Id_cliente")
                ));
            }
        }
        return ventas;
    }

    @Override
    public boolean insert(Venta entity) throws SQLException {

        String sql = "INSERT INTO venta (Id_producto, cantidad, Precio_total, Fecha, Id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.getIdProducto());
            ps.setInt(2, entity.getCantidad());
            ps.setBigDecimal(3, entity.getPrecioTotal());
            ps.setDate(4, Date.valueOf(entity.getFecha()));
            ps.setInt(5, entity.getIdCliente());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setIdVenta(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Venta entity) throws SQLException {
        String sql = "UPDATE venta SET Id_producto = ?, cantidad = ?, Precio_total = ?, Fecha = ?, Id_cliente = ? WHERE Id_venta = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getIdProducto());
            ps.setInt(2, entity.getCantidad());
            ps.setBigDecimal(3, entity.getPrecioTotal());
            ps.setDate(4, Date.valueOf(entity.getFecha()));
            ps.setInt(5, entity.getIdCliente());
            ps.setInt(6, entity.getIdVenta());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM venta WHERE Id_venta = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
