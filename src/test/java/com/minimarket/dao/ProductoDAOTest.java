package com.minimarket.dao;

import com.minimarket.dao.impl.ProductoDAOImpl;
import com.minimarket.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private ProductoDAO productoDAO;

    @BeforeEach
    public void setUp() {
        productoDAO = new ProductoDAOImpl(mockConnection);
    }

    @Test
    public void testFindById_Success() throws SQLException {

        int id = 1;
        String sql = "SELECT Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras FROM producto WHERE Id_producto = ?";

        when(mockConnection.prepareStatement(sql)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_producto")).thenReturn(id);
        when(mockResultSet.getString("nombre_Producto")).thenReturn("Arroz Costeño 1kg");
        when(mockResultSet.getBigDecimal("Precio_unitario")).thenReturn(new BigDecimal("4.50"));
        when(mockResultSet.getInt("Id_categoria")).thenReturn(2);
        when(mockResultSet.getString("codigo_barras")).thenReturn("123456789012");

        Producto result = productoDAO.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getIdProducto());
        assertEquals("Arroz Costeño 1kg", result.getNombreProducto());
        assertEquals(new BigDecimal("4.50"), result.getPrecioUnitario());
        assertEquals(2, result.getIdCategoria());

        verify(mockConnection).prepareStatement(sql);
        verify(mockPreparedStatement).setInt(1, id);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    public void testFindById_NotFound() throws SQLException {

        int id = 999;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Producto result = productoDAO.findById(id);

        assertNull(result);
    }
}
