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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    private ActionListener capturedRegistrarVentaListener;

    @BeforeEach
    public void setUp() {
        // Capture the RegistrarVentaListener when it's added to the view
        doAnswer(inv -> {
            capturedRegistrarVentaListener = inv.getArgument(0);
            return null;
        }).when(mockView).addRegistrarVentaListener(any(ActionListener.class));

        ventasController = new VentasController(mockView, mockVentaDAO, mockProductoDAO, mockClienteDAO);
    }

    @Test
    public void testGetView() {
        assertSame(mockView, ventasController.getView());
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

    @Test
    public void testInitData_Success() throws SQLException {
        List<Producto> prods = List.of(new Producto(1, "Prod 1", BigDecimal.TEN, 2));
        List<Cliente> clients = List.of(new Cliente(1, "Juan", "Perez", "Gomez", "12345678"));
        List<Venta> sales = List.of(new Venta(1, 2, BigDecimal.TEN, null, 1));

        when(mockProductoDAO.findAll()).thenReturn(prods);
        when(mockClienteDAO.findAll()).thenReturn(clients);
        when(mockVentaDAO.findAll()).thenReturn(sales);

        ventasController.initData();

        verify(mockView).clearFields();
        verify(mockView).setProductos(prods);
        verify(mockView).setClientes(clients);
        verify(mockView).setSalesTableData(sales, prods, clients);
    }

    @Test
    public void testInitData_SQLException() throws SQLException {
        when(mockProductoDAO.findAll()).thenThrow(new SQLException("Database offline"));

        ventasController.initData();

        verify(mockView).mostrarMensaje(contains("Error cargando datos"), eq(true));
    }

    @Test
    public void testRegistrarVenta_EmptyDni() {
        when(mockView.getDniCliente()).thenReturn("");

        capturedRegistrarVentaListener.actionPerformed(mock(ActionEvent.class));

        verify(mockView).mostrarMensaje(contains("no está permitida si no se ha asignado un cliente"), eq(true));
    }

    @Test
    public void testRegistrarVenta_InvalidDniLength() {
        when(mockView.getDniCliente()).thenReturn("12345"); // Not 8 or 11

        capturedRegistrarVentaListener.actionPerformed(mock(ActionEvent.class));

        verify(mockView).mostrarMensaje(contains("debe tener exactamente 8 u 11 dígitos"), eq(true));
    }

    @Test
    public void testRegistrarVenta_NewClientNotRegistered() {
        when(mockView.getDniCliente()).thenReturn("12345678");
        when(mockView.getSelectedCliente()).thenReturn(null);

        capturedRegistrarVentaListener.actionPerformed(mock(ActionEvent.class));

        verify(mockView).mostrarMensaje(contains("es nuevo y no está registrado"), eq(true));
    }

    @Test
    public void testRegistrarVenta_EmptyCart() {
        when(mockView.getDniCliente()).thenReturn("12345678");
        Cliente c = new Cliente(1, "Juan", "12345678");
        when(mockView.getSelectedCliente()).thenReturn(c);
        when(mockView.getCartItems()).thenReturn(Collections.emptyList());

        capturedRegistrarVentaListener.actionPerformed(mock(ActionEvent.class));

        verify(mockView).mostrarMensaje(contains("El carrito de compras está vacío"), eq(true));
    }

    @Test
    public void testRegistrarVenta_Success() throws SQLException {
        when(mockView.getDniCliente()).thenReturn("12345678");
        Cliente c = new Cliente(0, "Juan", "12345678"); // id 0 means it will trigger clientDAO.insert
        when(mockView.getSelectedCliente()).thenReturn(c);

        VentasView.CartItem item = mock(VentasView.CartItem.class);
        Producto p = new Producto(1, "Prod", BigDecimal.ONE, 2);
        when(item.getProducto()).thenReturn(p);
        when(item.getCantidad()).thenReturn(3);
        when(item.getSubtotal()).thenReturn(BigDecimal.TEN);
        when(mockView.getCartItems()).thenReturn(List.of(item));

        capturedRegistrarVentaListener.actionPerformed(mock(ActionEvent.class));

        verify(mockClienteDAO).insert(c);
        verify(mockVentaDAO).registrarVentaTransaccional(any(Venta.class));
        verify(mockView).mostrarMensaje(contains("Venta registrada con éxito"), eq(false));
    }

    @Test
    public void testRegistrarVenta_InsufficientStock() throws SQLException {
        when(mockView.getDniCliente()).thenReturn("12345678");
        Cliente c = new Cliente(2, "Juan", "12345678");
        when(mockView.getSelectedCliente()).thenReturn(c);

        VentasView.CartItem item = mock(VentasView.CartItem.class);
        Producto p = new Producto(1, "Prod", BigDecimal.ONE, 2);
        when(item.getProducto()).thenReturn(p);
        when(mockView.getCartItems()).thenReturn(List.of(item));

        doThrow(new InsufficientStockException("Out of stock", 0, 5))
                .when(mockVentaDAO).registrarVentaTransaccional(any(Venta.class));

        capturedRegistrarVentaListener.actionPerformed(mock(ActionEvent.class));

        verify(mockView).mostrarMensaje(contains("Error: Out of stock"), eq(true));
    }
}
