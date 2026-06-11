package com.minimarket.controller;

import com.minimarket.dao.CategoriaDAO;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.StockDAO;
import com.minimarket.model.Categoria;
import com.minimarket.model.Producto;
import com.minimarket.model.Stock;
import com.minimarket.view.CategoriasView;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriasController {

    private final CategoriasView view;
    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO;
    private final StockDAO stockDAO;

    public CategoriasController(CategoriasView view, CategoriaDAO categoriaDAO, ProductoDAO productoDAO, StockDAO stockDAO) {
        this.view = view;
        this.categoriaDAO = categoriaDAO;
        this.productoDAO = productoDAO;
        this.stockDAO = stockDAO;

        initListeners();
    }

    private void initListeners() {
        view.addCategorySelectionListener(new CategorySelectionListener());
        view.addCrearCategoriaListener(new CrearCategoriaListener());
    }

    public CategoriasView getView() {
        return view;
    }

    public void initData() {
        try {
            // Cargar Categorias
            List<Categoria> categorias = categoriaDAO.findAll();
            List<Producto> todosProductos = productoDAO.findAll();

            // Mapear conteo de productos por categoria
            Map<Integer, Integer> counts = new HashMap<>();
            for (Producto p : todosProductos) {
                counts.put(p.getIdCategoria(), counts.getOrDefault(p.getIdCategoria(), 0) + 1);
            }

            view.setCategorias(categorias, counts);

            // Cargar Resumen de Inventario
            cargarResumenInventario(categorias, todosProductos);

            // Seleccionar primera categoria por defecto si existe
            if (!categorias.isEmpty()) {
                selectCategory(categorias.get(0));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            view.mostrarMensaje("Error al cargar datos de categorías: " + ex.getMessage(), true);
        }
    }

    private void selectCategory(Categoria categoria) {
        if (categoria == null) return;
        try {
            List<Producto> todosProductos = productoDAO.findAll();
            List<Producto> filtrados = new ArrayList<>();
            for (Producto p : todosProductos) {
                if (p.getIdCategoria() == categoria.getIdCategoria()) {
                    filtrados.add(p);
                }
            }
            view.setProductos(filtrados);
        } catch (SQLException ex) {
            ex.printStackTrace();
            view.mostrarMensaje("Error al cargar productos de la categoría: " + ex.getMessage(), true);
        }
    }

    private void cargarResumenInventario(List<Categoria> categorias, List<Producto> productos) {
        try {
            // 1. Categoría más poblada
            Map<Integer, Integer> counts = new HashMap<>();
            int maxCount = -1;
            int maxCatId = -1;
            for (Producto p : productos) {
                int count = counts.getOrDefault(p.getIdCategoria(), 0) + 1;
                counts.put(p.getIdCategoria(), count);
                if (count > maxCount) {
                    maxCount = count;
                    maxCatId = p.getIdCategoria();
                }
            }

            String mostPopulated = "Ninguna";
            if (maxCatId != -1) {
                for (Categoria c : categorias) {
                    if (c.getIdCategoria() == maxCatId) {
                        mostPopulated = c.getNombreCategoria();
                        break;
                    }
                }
            }

            // 2. Stock total
            List<Stock> stocks = stockDAO.findAll();
            int totalStock = 0;
            for (Stock s : stocks) {
                totalStock += s.getCantidad();
            }

            // 3. Estimación máxima para la barra de progreso
            int maxStockEstimate = Math.max(20000, totalStock * 2);

            view.setResumenInventario(mostPopulated, totalStock, maxStockEstimate);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private class CategorySelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                Categoria selected = view.getSelectedCategory();
                if (selected != null) {
                    selectCategory(selected);
                    view.showProductsGrid();
                }
            }
        }
    }

    private class CrearCategoriaListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.showCategoryEditor(true);
        }
    }
}
