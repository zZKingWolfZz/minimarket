package com.minimarket.dao;

import com.minimarket.dao.impl.StockDAOImpl;
import com.minimarket.model.Stock;
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
public class StockDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private StockDAO stockDAO;

    @BeforeEach
    public void setUp() {
        stockDAO = new StockDAOImpl(mockConnection);
    }

    @Test
    public void testFindById_Success() throws SQLException {
        int id = 1;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_stock")).thenReturn(id);
        when(mockResultSet.getInt("Cantidad")).thenReturn(50);
        when(mockResultSet.getInt("Id_Producto")).thenReturn(12);

        Stock s = stockDAO.findById(id);

        assertNotNull(s);
        assertEquals(id, s.getIdStock());
        assertEquals(50, s.getCantidad());
        assertEquals(12, s.getIdProducto());
        verify(mockPreparedStatement).setInt(1, id);
    }

    @Test
    public void testFindAll() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Id_stock")).thenReturn(1);
        when(mockResultSet.getInt("Cantidad")).thenReturn(50);
        when(mockResultSet.getInt("Id_Producto")).thenReturn(12);

        List<Stock> list = stockDAO.findAll();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(50, list.get(0).getCantidad());
    }

    @Test
    public void testInsert_Success() throws SQLException {
        Stock s = new Stock(0, 30, 8);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(9);

        boolean result = stockDAO.insert(s);

        assertTrue(result);
        assertEquals(9, s.getIdStock());
        verify(mockPreparedStatement).setInt(1, 30);
        verify(mockPreparedStatement).setInt(2, 8);
    }

    @Test
    public void testUpdate_Success() throws SQLException {
        Stock s = new Stock(2, 40, 8);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = stockDAO.update(s);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 40);
        verify(mockPreparedStatement).setInt(3, 2);
    }

    @Test
    public void testDelete_Success() throws SQLException {
        int id = 3;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = stockDAO.delete(id);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, id);
    }

    @Test
    public void testAlertaStockMinimo() throws SQLException {
        int limite = 10;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Id_stock")).thenReturn(1);
        when(mockResultSet.getInt("Cantidad")).thenReturn(5);
        when(mockResultSet.getInt("Id_Producto")).thenReturn(2);

        List<Stock> alerts = stockDAO.alertaStockMinimo(limite);

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertEquals(5, alerts.get(0).getCantidad());
        verify(mockPreparedStatement).setInt(1, limite);
    }

    @Test
    public void testFindByProductoId_Success() throws SQLException {
        int prodId = 7;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("Id_stock")).thenReturn(15);
        when(mockResultSet.getInt("Cantidad")).thenReturn(25);
        when(mockResultSet.getInt("Id_Producto")).thenReturn(prodId);

        Stock s = stockDAO.findByProductoId(prodId);

        assertNotNull(s);
        assertEquals(15, s.getIdStock());
        assertEquals(25, s.getCantidad());
        assertEquals(prodId, s.getIdProducto());
        verify(mockPreparedStatement).setInt(1, prodId);
    }

    @Test
    public void testFindByProductoId_NotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Stock s = stockDAO.findByProductoId(99);
        assertNull(s);
    }
}
