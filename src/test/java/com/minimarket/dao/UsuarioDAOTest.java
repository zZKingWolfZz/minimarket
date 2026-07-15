package com.minimarket.dao;

import com.minimarket.dao.impl.UsuarioDAOImpl;
import com.minimarket.model.Rol;
import com.minimarket.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private PreparedStatement mockRolePreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockRoleResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private UsuarioDAO usuarioDAO;

    @BeforeEach
    public void setUp() {
        usuarioDAO = new UsuarioDAOImpl(mockConnection);
    }

    @Test
    public void testLogin_Success() throws SQLException {
        String username = "admin";
        String password = "password";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_usuario")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("nombre")).thenReturn("Admin");
        when(mockResultSet.getString("apellido_paterno")).thenReturn("User");
        when(mockResultSet.getString("apellido_materno")).thenReturn("Test");
        when(mockResultSet.getInt("estado")).thenReturn(1);

        // Mocking roles
        when(mockConnection.prepareStatement(contains("INNER JOIN usuario_rol"))).thenReturn(mockRolePreparedStatement);
        when(mockRolePreparedStatement.executeQuery()).thenReturn(mockRoleResultSet);
        when(mockRoleResultSet.next()).thenReturn(true, false);
        when(mockRoleResultSet.getInt("Id_rol")).thenReturn(2);
        when(mockRoleResultSet.getString("nombre_rol")).thenReturn("Administrador");

        Usuario user = usuarioDAO.login(username, password);

        assertNotNull(user);
        assertEquals(1, user.getIdUsuario());
        assertEquals(username, user.getUsername());
        assertEquals(1, user.getRoles().size());
        assertEquals("Administrador", user.getRoles().get(0).getNombreRol());
    }

    @Test
    public void testLogin_NotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Usuario user = usuarioDAO.login("invalid", "invalid");
        assertNull(user);
    }

    @Test
    public void testFindById_Success() throws SQLException {
        int id = 1;
        when(mockConnection.prepareStatement(startsWith("SELECT Id_usuario"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_usuario")).thenReturn(id);
        when(mockResultSet.getString("username")).thenReturn("admin");
        when(mockResultSet.getString("nombre")).thenReturn("Admin");
        when(mockResultSet.getString("apellido_paterno")).thenReturn("User");
        when(mockResultSet.getString("apellido_materno")).thenReturn("Test");
        when(mockResultSet.getInt("estado")).thenReturn(1);

        // Role select mock
        when(mockConnection.prepareStatement(contains("INNER JOIN usuario_rol"))).thenReturn(mockRolePreparedStatement);
        when(mockRolePreparedStatement.executeQuery()).thenReturn(mockRoleResultSet);
        when(mockRoleResultSet.next()).thenReturn(false);

        Usuario user = usuarioDAO.findById(id);

        assertNotNull(user);
        assertEquals(id, user.getIdUsuario());
        verify(mockPreparedStatement).setInt(1, id);
    }

    @Test
    public void testFindAll_Success() throws SQLException {
        when(mockConnection.prepareStatement(startsWith("SELECT Id_usuario"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Id_usuario")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("admin");
        when(mockResultSet.getString("nombre")).thenReturn("Admin");
        when(mockResultSet.getString("apellido_paterno")).thenReturn("User");
        when(mockResultSet.getString("apellido_materno")).thenReturn("Test");
        when(mockResultSet.getInt("estado")).thenReturn(1);

        // Role query for each user
        when(mockConnection.prepareStatement(contains("INNER JOIN usuario_rol"))).thenReturn(mockRolePreparedStatement);
        when(mockRolePreparedStatement.executeQuery()).thenReturn(mockRoleResultSet);
        when(mockRoleResultSet.next()).thenReturn(false);

        List<Usuario> list = usuarioDAO.findAll();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("admin", list.get(0).getUsername());
    }

    @Test
    public void testInsert_Success() throws SQLException {
        Usuario user = new Usuario(0, "new_user", "password", "Name", "Paterno", "Materno", 1);
        Rol rol = new Rol(2, "Cajero");
        user.addRol(rol);

        when(mockConnection.prepareStatement(contains("INSERT INTO usuario"), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(42);

        // Role insert statement
        when(mockConnection.prepareStatement(contains("INSERT INTO usuario_rol"))).thenReturn(mockRolePreparedStatement);
        when(mockRolePreparedStatement.executeBatch()).thenReturn(new int[]{1});

        boolean result = usuarioDAO.insert(user);

        assertTrue(result);
        assertEquals(42, user.getIdUsuario());
        verify(mockPreparedStatement).setString(1, "new_user");
        verify(mockPreparedStatement).setString(2, "password");
        verify(mockRolePreparedStatement).setInt(1, 42);
        verify(mockRolePreparedStatement).setInt(2, 2);
    }

    @Test
    public void testUpdate_Success() throws SQLException {
        Usuario user = new Usuario(10, "edit_user", null, "Name", "Paterno", "Materno", 1);
        Rol rol = new Rol(3, "Supervisor");
        user.addRol(rol);

        when(mockConnection.prepareStatement(startsWith("UPDATE usuario"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Delete old roles
        when(mockConnection.prepareStatement(contains("DELETE FROM usuario_rol"))).thenReturn(mockRolePreparedStatement);

        // Insert new roles
        PreparedStatement mockInsertRolePs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("INSERT INTO usuario_rol"))).thenReturn(mockInsertRolePs);
        when(mockInsertRolePs.executeBatch()).thenReturn(new int[]{1});

        boolean result = usuarioDAO.update(user);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "edit_user");
        verify(mockPreparedStatement).setInt(6, 10);
        verify(mockRolePreparedStatement).setInt(1, 10);
        verify(mockInsertRolePs).setInt(1, 10);
        verify(mockInsertRolePs).setInt(2, 3);
    }

    @Test
    public void testDelete_Success() throws SQLException {
        int id = 5;
        when(mockConnection.prepareStatement("DELETE FROM usuario_rol WHERE Id_usuario = ?")).thenReturn(mockRolePreparedStatement);
        when(mockConnection.prepareStatement("DELETE FROM usuario WHERE Id_usuario = ?")).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = usuarioDAO.delete(id);

        assertTrue(result);
        verify(mockRolePreparedStatement).setInt(1, id);
        verify(mockPreparedStatement).setInt(1, id);
    }
}
