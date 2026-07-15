package com.minimarket.dao;

import com.minimarket.dao.impl.ClienteDAOImpl;
import com.minimarket.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private ClienteDAO clienteDAO;

    @BeforeEach
    public void setUp() {
        clienteDAO = new ClienteDAOImpl(mockConnection);
    }

    @Test
    public void testFindById_Success() throws SQLException {
        int id = 1;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_cliente")).thenReturn(id);
        when(mockResultSet.getString("nombre")).thenReturn("Juan");
        when(mockResultSet.getString("apellido_paterno")).thenReturn("Perez");
        when(mockResultSet.getString("apellido_materno")).thenReturn("Gomez");
        when(mockResultSet.getString("DNI_RUC")).thenReturn("12345678");

        Cliente c = clienteDAO.findById(id);

        assertNotNull(c);
        assertEquals(id, c.getIdCliente());
        assertEquals("Juan", c.getNombre());
        assertEquals("12345678", c.getDniRuc());
        verify(mockPreparedStatement).setInt(1, id);
    }

    @Test
    public void testFindById_NotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Cliente c = clienteDAO.findById(999);
        assertNull(c);
    }

    @Test
    public void testFindAll() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Id_cliente")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("Juan");
        when(mockResultSet.getString("apellido_paterno")).thenReturn("Perez");
        when(mockResultSet.getString("apellido_materno")).thenReturn("Gomez");
        when(mockResultSet.getString("DNI_RUC")).thenReturn("12345678");

        List<Cliente> list = clienteDAO.findAll();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Juan", list.get(0).getNombre());
    }

    @Test
    public void testInsert_Success() throws SQLException {
        Cliente c = new Cliente(0, "Maria", "Lopez", "Diaz", "87654321");

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(5);

        boolean result = clienteDAO.insert(c);

        assertTrue(result);
        assertEquals(5, c.getIdCliente());
        verify(mockPreparedStatement).setString(1, "Maria");
        verify(mockPreparedStatement).setString(4, "87654321");
    }

    @Test
    public void testUpdate_Success() throws SQLException {
        Cliente c = new Cliente(3, "Maria", "Lopez", "Diaz", "87654321");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = clienteDAO.update(c);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Maria");
        verify(mockPreparedStatement).setInt(5, 3);
    }

    @Test
    public void testDelete_Success() throws SQLException {
        int id = 4;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = clienteDAO.delete(id);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, id);
    }
}
