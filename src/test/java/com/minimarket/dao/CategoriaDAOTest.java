package com.minimarket.dao;

import com.minimarket.dao.impl.CategoriaDAOImpl;
import com.minimarket.model.Categoria;
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
public class CategoriaDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private CategoriaDAO categoriaDAO;

    @BeforeEach
    public void setUp() {
        categoriaDAO = new CategoriaDAOImpl(mockConnection);
    }

    @Test
    public void testFindById_Success() throws SQLException {
        int id = 1;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_categoria")).thenReturn(id);
        when(mockResultSet.getString("nombre_categoria")).thenReturn("Abarrotes");

        Categoria c = categoriaDAO.findById(id);

        assertNotNull(c);
        assertEquals(id, c.getIdCategoria());
        assertEquals("Abarrotes", c.getNombreCategoria());
        verify(mockPreparedStatement).setInt(1, id);
    }

    @Test
    public void testFindById_NotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Categoria c = categoriaDAO.findById(999);
        assertNull(c);
    }

    @Test
    public void testFindAll() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Id_categoria")).thenReturn(1);
        when(mockResultSet.getString("nombre_categoria")).thenReturn("Abarrotes");

        List<Categoria> list = categoriaDAO.findAll();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Abarrotes", list.get(0).getNombreCategoria());
    }

    @Test
    public void testInsert_Success() throws SQLException {
        Categoria c = new Categoria(0, "Bebidas");

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(3);

        boolean result = categoriaDAO.insert(c);

        assertTrue(result);
        assertEquals(3, c.getIdCategoria());
        verify(mockPreparedStatement).setString(1, "Bebidas");
    }

    @Test
    public void testUpdate_Success() throws SQLException {
        Categoria c = new Categoria(2, "Lácteos");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = categoriaDAO.update(c);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Lácteos");
        verify(mockPreparedStatement).setInt(2, 2);
    }

    @Test
    public void testDelete_Success() throws SQLException {
        int id = 4;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = categoriaDAO.delete(id);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, id);
    }
}
