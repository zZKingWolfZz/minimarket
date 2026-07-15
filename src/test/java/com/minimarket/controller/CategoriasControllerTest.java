package com.minimarket.controller;

import com.minimarket.dao.CategoriaDAO;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.StockDAO;
import com.minimarket.model.Categoria;
import com.minimarket.model.Producto;
import com.minimarket.model.Stock;
import com.minimarket.view.CategoriasView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
public class CategoriasControllerTest {

    @Mock
    private CategoriasView mockView;

    @Mock
    private CategoriaDAO mockCategoriaDAO;

    @Mock
    private ProductoDAO mockProductoDAO;

    @Mock
    private StockDAO mockStockDAO;

    private CategoriasController categoriasController;

    @BeforeEach
    public void setUp() {
        categoriasController = new CategoriasController(mockView, mockCategoriaDAO, mockProductoDAO, mockStockDAO);
    }

    @Test
    public void testGetView() {
        assertSame(mockView, categoriasController.getView());
    }

    @Test
    public void testInitData_Success() throws SQLException {
        Categoria cat1 = new Categoria(1, "Abarrotes");
        Categoria cat2 = new Categoria(2, "Bebidas");
        List<Categoria> cats = List.of(cat1, cat2);

        Producto p1 = new Producto(101, "Arroz", new BigDecimal("4.50"), 1);
        Producto p2 = new Producto(102, "Azucar", new BigDecimal("3.80"), 1);
        Producto p3 = new Producto(103, "Gaseosa", new BigDecimal("2.50"), 2);
        List<Producto> prods = List.of(p1, p2, p3);

        Stock s1 = new Stock(1, 100, 101);
        Stock s2 = new Stock(2, 50, 102);
        Stock s3 = new Stock(3, 80, 103);
        List<Stock> stocks = List.of(s1, s2, s3);

        when(mockCategoriaDAO.findAll()).thenReturn(cats);
        when(mockProductoDAO.findAll()).thenReturn(prods);
        when(mockStockDAO.findAll()).thenReturn(stocks);

        categoriasController.initData();

        verify(mockView).setCategorias(eq(cats), anyMap());
        verify(mockView).setResumenInventario("Abarrotes", 230, 20000);
        // Should select the first category
        verify(mockView).setProductos(anyList());
    }

    @Test
    public void testInitData_SQLException() throws SQLException {
        when(mockCategoriaDAO.findAll()).thenThrow(new SQLException("DB connection lost"));

        categoriasController.initData();

        verify(mockView).mostrarMensaje(contains("Error al cargar datos"), eq(true));
    }

    @Test
    public void testCrearCategoriaListener() {
        // Retrieve internal ActionListener using argument captor or directly mocking trigger
        doAnswer(inv -> {
            ActionListener listener = inv.getArgument(0);
            listener.actionPerformed(mock(ActionEvent.class));
            return null;
        }).when(mockView).addCrearCategoriaListener(any(ActionListener.class));

        // Re-initialize to trigger the stubbed doAnswer in constructor listeners setup
        new CategoriasController(mockView, mockCategoriaDAO, mockProductoDAO, mockStockDAO);

        verify(mockView, atLeastOnce()).showCategoryEditor(true);
    }

    @Test
    public void testCategorySelectionListener() throws SQLException {
        Categoria selectedCat = new Categoria(2, "Bebidas");
        when(mockView.getSelectedCategory()).thenReturn(selectedCat);

        Producto p = new Producto(103, "Gaseosa", new BigDecimal("2.50"), 2);
        when(mockProductoDAO.findAll()).thenReturn(List.of(p));

        doAnswer(inv -> {
            ListSelectionListener listener = inv.getArgument(0);
            ListSelectionEvent mockEvent = mock(ListSelectionEvent.class);
            when(mockEvent.getValueIsAdjusting()).thenReturn(false);
            listener.valueChanged(mockEvent);
            return null;
        }).when(mockView).addCategorySelectionListener(any(ListSelectionListener.class));

        new CategoriasController(mockView, mockCategoriaDAO, mockProductoDAO, mockStockDAO);

        verify(mockView).setProductos(argThat(list -> list.size() == 1 && list.get(0).getIdCategoria() == 2));
        verify(mockView).showProductsGrid();
    }
}
