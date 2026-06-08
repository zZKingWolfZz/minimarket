package com.minimarket.dao;

import com.minimarket.dao.impl.VentaDAOImpl;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.model.Venta;
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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockSelectPreparedStatement;

    @Mock
    private PreparedStatement mockUpdatePreparedStatement;

    @Mock
    private PreparedStatement mockInsertPreparedStatement;

    @Mock
    private ResultSet mockSelectResultSet;

    @Mock
    private ResultSet mockInsertGeneratedKeysResultSet;

    private VentaDAO ventaDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        ventaDAO = new VentaDAOImpl(mockConnection);
        lenient().when(mockConnection.getAutoCommit()).thenReturn(true);
    }

    @Test
    public void testRegistrarVentaTransaccional_Success() throws SQLException {

        Venta venta = new Venta(5, 3, new BigDecimal("45.00"), LocalDate.now(), 10); 

        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockSelectPreparedStatement);
        when(mockConnection.prepareStatement(startsWith("UPDATE"))).thenReturn(mockUpdatePreparedStatement);
        when(mockConnection.prepareStatement(startsWith("INSERT"), eq(PreparedStatement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockInsertPreparedStatement);

        when(mockSelectPreparedStatement.executeQuery()).thenReturn(mockSelectResultSet);
        when(mockSelectResultSet.next()).thenReturn(true);
        when(mockSelectResultSet.getInt("Id_stock")).thenReturn(12);
        when(mockSelectResultSet.getInt("Cantidad")).thenReturn(10); 

        when(mockInsertPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockInsertPreparedStatement.getGeneratedKeys()).thenReturn(mockInsertGeneratedKeysResultSet);
        when(mockInsertGeneratedKeysResultSet.next()).thenReturn(true);
        when(mockInsertGeneratedKeysResultSet.getInt(1)).thenReturn(101); 

        boolean result = ventaDAO.registrarVentaTransaccional(venta);

        assertTrue(result);
        assertEquals(101, venta.getIdVenta());

        verify(mockConnection).setAutoCommit(false);
        verify(mockSelectPreparedStatement).setInt(1, 5);
        verify(mockUpdatePreparedStatement).setInt(1, 7); 
        verify(mockUpdatePreparedStatement).setInt(2, 12); 
        verify(mockUpdatePreparedStatement).executeUpdate();
        verify(mockInsertPreparedStatement).executeUpdate();
        verify(mockConnection).commit();
        verify(mockConnection, never()).rollback();
        verify(mockConnection).setAutoCommit(true); 
    }

    @Test
    public void testRegistrarVentaTransaccional_InsufficientStock_Rollback() throws SQLException {

        Venta venta = new Venta(5, 15, new BigDecimal("225.00"), LocalDate.now(), 10); 

        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockSelectPreparedStatement);
        when(mockSelectPreparedStatement.executeQuery()).thenReturn(mockSelectResultSet);
        when(mockSelectResultSet.next()).thenReturn(true);
        when(mockSelectResultSet.getInt("Id_stock")).thenReturn(12);
        when(mockSelectResultSet.getInt("Cantidad")).thenReturn(10); 

        InsufficientStockException thrown = assertThrows(InsufficientStockException.class, () -> {
            ventaDAO.registrarVentaTransaccional(venta);
        });

        assertTrue(thrown.getMessage().contains("Stock insuficiente"));
        assertEquals(10, thrown.getStockDisponible());
        assertEquals(15, thrown.getCantidadRequerida());

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection, never()).commit();
        verify(mockUpdatePreparedStatement, never()).executeUpdate();
        verify(mockInsertPreparedStatement, never()).executeUpdate();
        verify(mockConnection).setAutoCommit(true); 
    }
}
