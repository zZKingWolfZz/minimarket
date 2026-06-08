package com.minimarket.dao.impl;

import com.minimarket.dao.ClienteDAO;
import com.minimarket.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements ClienteDAO {

    private final Connection connection;

    public ClienteDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Cliente findById(Integer id) throws SQLException {
        String sql = "SELECT Id_cliente, nombre, apellido_paterno, apellido_materno, DNI_RUC FROM cliente WHERE Id_cliente = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                            rs.getInt("Id_cliente"),
                            rs.getString("nombre"),
                            rs.getString("apellido_paterno"),
                            rs.getString("apellido_materno"),
                            rs.getString("DNI_RUC")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public List<Cliente> findAll() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT Id_cliente, nombre, apellido_paterno, apellido_materno, DNI_RUC FROM cliente";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(new Cliente(
                        rs.getInt("Id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getString("DNI_RUC")
                ));
            }
        }
        return clientes;
    }

    @Override
    public boolean insert(Cliente entity) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, apellido_paterno, apellido_materno, DNI_RUC) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getNombre());
            ps.setString(2, entity.getApellidoPaterno());
            ps.setString(3, entity.getApellidoMaterno());
            ps.setString(4, entity.getDniRuc());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setIdCliente(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Cliente entity) throws SQLException {
        String sql = "UPDATE cliente SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, DNI_RUC = ? WHERE Id_cliente = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getNombre());
            ps.setString(2, entity.getApellidoPaterno());
            ps.setString(3, entity.getApellidoMaterno());
            ps.setString(4, entity.getDniRuc());
            ps.setInt(5, entity.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE Id_cliente = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
