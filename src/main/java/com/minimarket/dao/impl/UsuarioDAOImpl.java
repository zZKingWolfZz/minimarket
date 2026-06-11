package com.minimarket.dao.impl;

import com.minimarket.dao.UsuarioDAO;
import com.minimarket.model.Rol;
import com.minimarket.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    private final Connection connection;

    public UsuarioDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Usuario login(String username, String password) throws SQLException {
        String sql = "SELECT Id_usuario, username, nombre, apellido_paterno, apellido_materno, estado FROM usuario WHERE username = ? AND password = SHA2(?, 256) AND estado = 1";
        Usuario usuario = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("Id_usuario"),
                            rs.getString("username"),
                            null,
                            rs.getString("nombre"),
                            rs.getString("apellido_paterno"),
                            rs.getString("apellido_materno"),
                            rs.getInt("estado"));
                }
            }
        }

        if (usuario != null) {
            String roleSql = "SELECT r.Id_rol, r.nombre_rol FROM rol r " +
                    "INNER JOIN usuario_rol ur ON r.Id_rol = ur.Id_rol " +
                    "WHERE ur.Id_usuario = ?";
            try (PreparedStatement psRole = connection.prepareStatement(roleSql)) {
                psRole.setInt(1, usuario.getIdUsuario());
                try (ResultSet rsRole = psRole.executeQuery()) {
                    while (rsRole.next()) {
                        Rol rol = new Rol(
                                rsRole.getInt("Id_rol"),
                                rsRole.getString("nombre_rol"));
                        usuario.addRol(rol);
                    }
                }
            }
        }

        return usuario;
    }

    @Override
    public Usuario findById(Integer id) throws SQLException {
        String sql = "SELECT Id_usuario, username, nombre, apellido_paterno, apellido_materno, estado FROM usuario WHERE Id_usuario = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getInt("Id_usuario"),
                            rs.getString("username"),
                            null,
                            rs.getString("nombre"),
                            rs.getString("apellido_paterno"),
                            rs.getString("apellido_materno"),
                            rs.getInt("estado"));

                    String roleSql = "SELECT r.Id_rol, r.nombre_rol FROM rol r " +
                            "INNER JOIN usuario_rol ur ON r.Id_rol = ur.Id_rol " +
                            "WHERE ur.Id_usuario = ?";
                    try (PreparedStatement psRole = connection.prepareStatement(roleSql)) {
                        psRole.setInt(1, u.getIdUsuario());
                        try (ResultSet rsRole = psRole.executeQuery()) {
                            while (rsRole.next()) {
                                u.addRol(new Rol(rsRole.getInt("Id_rol"), rsRole.getString("nombre_rol")));
                            }
                        }
                    }
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT Id_usuario, username, nombre, apellido_paterno, apellido_materno, estado FROM usuario";
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario(
                        rs.getInt("Id_usuario"),
                        rs.getString("username"),
                        null,
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getInt("estado"));
                usuarios.add(u);
            }
        }

        for (Usuario u : usuarios) {
            String roleSql = "SELECT r.Id_rol, r.nombre_rol FROM rol r " +
                    "INNER JOIN usuario_rol ur ON r.Id_rol = ur.Id_rol " +
                    "WHERE ur.Id_usuario = ?";
            try (PreparedStatement psRole = connection.prepareStatement(roleSql)) {
                psRole.setInt(1, u.getIdUsuario());
                try (ResultSet rsRole = psRole.executeQuery()) {
                    while (rsRole.next()) {
                        u.addRol(new Rol(rsRole.getInt("Id_rol"), rsRole.getString("nombre_rol")));
                    }
                }
            }
        }

        return usuarios;
    }

    @Override
    public boolean insert(Usuario entity) throws SQLException {
        String sql = "INSERT INTO usuario (username, password, nombre, apellido_paterno, apellido_materno, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getPassword());
            ps.setString(3, entity.getNombre());
            ps.setString(4, entity.getApellidoPaterno());
            ps.setString(5, entity.getApellidoMaterno());
            ps.setInt(6, entity.getEstado());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setIdUsuario(generatedKeys.getInt(1));
                    }
                }

                if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
                    String insertRoleSql = "INSERT INTO usuario_rol (Id_usuario, Id_rol) VALUES (?, ?)";
                    try (PreparedStatement psRole = connection.prepareStatement(insertRoleSql)) {
                        for (Rol rol : entity.getRoles()) {
                            psRole.setInt(1, entity.getIdUsuario());
                            psRole.setInt(2, rol.getIdRol());
                            psRole.addBatch();
                        }
                        psRole.executeBatch();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Usuario entity) throws SQLException {
        String sql = "UPDATE usuario SET username = ?, nombre = ?, apellido_paterno = ?, apellido_materno = ?, estado = ? WHERE Id_usuario = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getNombre());
            ps.setString(3, entity.getApellidoPaterno());
            ps.setString(4, entity.getApellidoMaterno());
            ps.setInt(5, entity.getEstado());
            ps.setInt(6, entity.getIdUsuario());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {

                String deleteRoles = "DELETE FROM usuario_rol WHERE Id_usuario = ?";
                try (PreparedStatement psDelete = connection.prepareStatement(deleteRoles)) {
                    psDelete.setInt(1, entity.getIdUsuario());
                    psDelete.executeUpdate();
                }

                if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
                    String insertRoleSql = "INSERT INTO usuario_rol (Id_usuario, Id_rol) VALUES (?, ?)";
                    try (PreparedStatement psRole = connection.prepareStatement(insertRoleSql)) {
                        for (Rol rol : entity.getRoles()) {
                            psRole.setInt(1, entity.getIdUsuario());
                            psRole.setInt(2, rol.getIdRol());
                            psRole.addBatch();
                        }
                        psRole.executeBatch();
                    }
                }
            }
            return updated;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String deleteRoles = "DELETE FROM usuario_rol WHERE Id_usuario = ?";
        try (PreparedStatement psRole = connection.prepareStatement(deleteRoles)) {
            psRole.setInt(1, id);
            psRole.executeUpdate();
        }

        String sql = "DELETE FROM usuario WHERE Id_usuario = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}