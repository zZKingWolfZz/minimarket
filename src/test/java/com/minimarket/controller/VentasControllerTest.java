package com.minimarket.controller;

import com.minimarket.dao.ClienteDAO;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.VentaDAO;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.model.Cliente;
import com.minimarket.model.Producto;
import com.minimarket.model.Venta;
import com.minimarket.view.VentasView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentasControllerTest {

    @Mock
    private VentasView mockView;

    @Mock
    private VentaDAO mockVentaDAO;

    @Mock
    private ProductoDAO mockProductoDAO;

    @Mock
    private ClienteDAO mockClienteDAO;

    private VentasController ventasController;

    @BeforeEach
    public void setUp() {
        ventasController = new VentasController(mockView, mockVentaDAO, mockProductoDAO, mockClienteDAO);
    }

    @Test
    public void testCalcularTotal_Success() {

        Producto prod = new Producto(1, "Galletas Soda", new BigDecimal("1.20"), 1);
        when(mockView.getSelectedProducto()).thenReturn(prod);
        when(mockView.getCantidad()).thenReturn(5); 

        ventasController.calcularTotal();

        verify(mockView).setPrecioTotal(new BigDecimal("6.00"));
    }

    @Test
    public void testCalcularTotal_NoProductSelected() {

        when(mockView.getSelectedProducto()).thenReturn(null);

        ventasController.calcularTotal();

        verify(mockView).setPrecioTotal(BigDecimal.ZERO);
    }
}
