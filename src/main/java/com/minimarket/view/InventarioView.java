package com.minimarket.view;

import com.minimarket.model.Producto;
import com.minimarket.model.Stock;
import com.minimarket.model.Categoria;
import com.minimarket.dao.CategoriaDAO;
import com.minimarket.dao.impl.CategoriaDAOImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InventarioView extends JPanel {
    private JSpinner spLimite;
    private JComboBox<String> cbStock;
    private JButton btnBuscarAlertas;
    private JButton btnVerTodo;
    private JTable tblStock;
    private DefaultTableModel tableModel;

    // Componentes adicionales para el diseño premium
    private JLabel lblTotalValorizado;
    private JLabel lblCriticalItemsCount;
    private JLabel lblTotalProductsCount;
    private JTextField txtQuickSearch;
    private JComboBox<Object> cbCategoriasFiltro;

    // Caché de datos para renderizado avanzado en tiempo real
    private List<Stock> lastStocks = new ArrayList<>();
    private List<Producto> lastProducts = new ArrayList<>();
    private List<Categoria> allCategories = new ArrayList<>();

    public InventarioView() {
        setPreferredSize(new Dimension(850, 680));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 250, 252)); // Slate 50 background
        setBorder(new EmptyBorder(24, 30, 24, 30));

        // 1. PANEL CABECERA (Header Panel)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Catálogo de Productos");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(15, 23, 42)); // Slate 900

        JLabel lblSubtitle = new JLabel("Supervisa y ajusta los niveles de inventario en tiempo real.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139)); // Slate 500

        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblSubtitle);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);

        // Botón Exportar (btnExportarExcel)
        btnVerTodo = new JButton("Limpiar Filtros") {
            @Override
            protected void paintComponent(Graphics g) {
                // Renders link button dynamically in filters, but kept in code
                super.paintComponent(g);
            }
        }; // We will place btnVerTodo beautifully inside the filters panel!

        JButton btnExportar = new JButton("Exportar", new ExportIcon()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(241, 245, 249));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportar.setForeground(new Color(71, 85, 105)); // Slate 700
        btnExportar.setContentAreaFilled(false);
        btnExportar.setFocusPainted(false);
        btnExportar.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportar.setIconTextGap(8);
        btnExportar.addActionListener(e -> {
            for (ActionListener al : btnExportarExcelMockListeners) {
                al.actionPerformed(e);
            }
        });

        // Alias para el botón del controlador
        btnBuscarAlertas = new JButton("Buscar Alertas de Stock"); // We'll place it in the filter panel!

        JButton btnAddProductMock = new JButton("+ Añadir Producto") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(21, 128, 61));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(22, 163, 74));
                } else {
                    g2.setColor(new Color(24, 119, 242)); // Vibrant blue
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btnAddProductMock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window parentWindow = SwingUtilities.getWindowAncestor(InventarioView.this);
                if (parentWindow instanceof DashboardView) {
                    DashboardView dash = (DashboardView) parentWindow;
                    dash.setViewPanel(new InventarioAddView());
                    dash.setHeaderTitle("Registrar Nuevo Producto");
                }
            }
        });

        btnAddProductMock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddProductMock.setForeground(Color.WHITE);
        btnAddProductMock.setContentAreaFilled(false);
        btnAddProductMock.setFocusPainted(false);
        btnAddProductMock.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnAddProductMock.setCursor(new Cursor(Cursor.HAND_CURSOR));

        headerRight.add(btnExportar);
        headerRight.add(btnAddProductMock);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. CONTENIDO PRINCIPAL (White Card with Filters & Table)
        RoundedPanel mainCard = new RoundedPanel(16);
        mainCard.setBackground(Color.WHITE);
        mainCard.setLayout(new BorderLayout(0, 15));
        mainCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        // 2.1 Panel de Filtros (Filters Row)
        JPanel filtersWrapper = new JPanel(new BorderLayout());
        filtersWrapper.setOpaque(false);

        JPanel filtersLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtersLeft.setOpaque(false);

        JLabel lblFiltrar = new JLabel("Filtrar por:");
        lblFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFiltrar.setForeground(new Color(148, 163, 184)); // Slate 400

        java.util.List<Object> filterItems = new java.util.ArrayList<>();
        filterItems.add("Todas las categorías");
        try {
            java.sql.Connection connection = com.minimarket.config.DatabaseConnection.getInstance().getConnection();
            if (connection != null) {
                CategoriaDAO catDAO = new CategoriaDAOImpl(connection);
                java.util.List<Categoria> list = catDAO.findAll();
                this.allCategories = list;
                filterItems.addAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filterItems.size() == 1) {
            this.allCategories.add(new Categoria(1, "Abarrotes"));
            this.allCategories.add(new Categoria(2, "Bebidas"));
            this.allCategories.add(new Categoria(3, "Lácteos"));
            this.allCategories.add(new Categoria(4, "Limpieza"));
            this.allCategories.add(new Categoria(5, "Cuidado Personal"));
            this.allCategories.add(new Categoria(6, "Snacks y Golosinas"));
            this.allCategories.add(new Categoria(7, "Panadería"));
            filterItems.addAll(this.allCategories);
        }
        cbCategoriasFiltro = new JComboBox<>(filterItems.toArray());
        styleComboBox(cbCategoriasFiltro);

        filtersLeft.add(lblFiltrar);
        filtersLeft.add(cbCategoriasFiltro);

        // Botón Ver Todo ("Limpiar Filtros" en el mockup)
        btnVerTodo = new JButton("Limpiar Filtros") {
            @Override
            protected void paintComponent(Graphics g) {
                // Soft flat text button
                super.paintComponent(g);
            }
        };
        btnVerTodo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVerTodo.setForeground(new Color(24, 119, 242)); // Blue link color
        btnVerTodo.setContentAreaFilled(false);
        btnVerTodo.setFocusPainted(false);
        btnVerTodo.setBorder(null);
        btnVerTodo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerTodo.addActionListener(e -> {
            if (cbCategoriasFiltro != null && cbCategoriasFiltro.getItemCount() > 0) {
                cbCategoriasFiltro.setSelectedIndex(0);
            }
            if (cbStock != null && cbStock.getItemCount() > 0) {
                cbStock.setSelectedIndex(0);
            }
        });

        filtersWrapper.add(filtersLeft, BorderLayout.WEST);

        btnBuscarAlertas = new JButton(); // Botón virtual para compatibilidad del listener
        spLimite = new JSpinner(); // Spinner virtual para retrocompatibilidad defensiva

        cbStock = new JComboBox<>(new String[] { "Todos los niveles", "Stock Bajo / Crítico", "Límite Crítico (≤ 5)", "Límite Moderado (≤ 15)" });
        styleComboBox(cbStock);
        filtersLeft.add(cbStock);

        // Disparar doClick del controlador en tiempo real al cambiar de selección
        cbStock.addActionListener(e -> {
            btnBuscarAlertas.doClick();
        });

        filtersWrapper.add(btnVerTodo, BorderLayout.EAST);

        mainCard.add(filtersWrapper, BorderLayout.NORTH);

        // 2.2 TABLA DE INVENTARIO (tblStock con TableModel original de 4 columnas)
        // Usamos exactamente los mismos nombres y tipos de columnas que espera el
        // controlador
        String[] columns = { "ID Stock", "Producto", "Stock Actual", "Estado / Alerta" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblStock = new JTable(tableModel);
        tblStock.setRowHeight(58); // Row height spacious for circular icons
        tblStock.setShowGrid(false);
        tblStock.setBackground(Color.WHITE);
        tblStock.setIntercellSpacing(new Dimension(0, 0));
        tblStock.setSelectionBackground(new Color(248, 250, 252));
        tblStock.setSelectionForeground(new Color(15, 23, 42));

        // Asignamos Renderers Personalizados para simular 100% el diseño avanzado
        tblStock.getColumnModel().getColumn(0).setCellRenderer(new SKUCellRenderer());
        tblStock.getColumnModel().getColumn(1).setCellRenderer(new ProductCellRenderer());
        tblStock.getColumnModel().getColumn(2).setCellRenderer(new StockLevelCellRenderer());
        tblStock.getColumnModel().getColumn(3).setCellRenderer(new EstadoAlertaCellRenderer());

        // Tooltip para indicar que se puede editar con doble clic
        tblStock.setToolTipText("Doble clic sobre un producto para editar su ficha técnica y stock");

        // Evento de doble clic para abrir el formulario de edición
        tblStock.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblStock.getSelectedRow();
                    if (row != -1) {
                        int modelRow = tblStock.convertRowIndexToModel(row);
                        Object idStockObj = tableModel.getValueAt(modelRow, 0);
                        if (idStockObj instanceof Integer) {
                            int idStock = (Integer) idStockObj;
                            Stock selectedStock = null;
                            for (Stock s : lastStocks) {
                                if (s.getIdStock() == idStock) {
                                    selectedStock = s;
                                    break;
                                }
                            }
                            if (selectedStock != null) {
                                Producto selectedProduct = null;
                                for (Producto p : lastProducts) {
                                    if (p.getIdProducto() == selectedStock.getIdProducto()) {
                                        selectedProduct = p;
                                        break;
                                    }
                                }
                                if (selectedProduct != null) {
                                    Window parentWindow = SwingUtilities.getWindowAncestor(InventarioView.this);
                                    if (parentWindow instanceof DashboardView) {
                                        DashboardView dash = (DashboardView) parentWindow;
                                        dash.setViewPanel(new InventarioEditView(selectedProduct));
                                        dash.setHeaderTitle("Editar Producto");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        // Cabecera estilizada de la tabla
        JTableHeader tableHeader = tblStock.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableHeader.setForeground(new Color(148, 163, 184)); // Slate 400
        tableHeader.setBackground(new Color(248, 250, 252));
        tableHeader.setDefaultRenderer(new CustomHeaderRenderer());
        tableHeader.setPreferredSize(new Dimension(0, 38));

        JScrollPane scrollPane = new JScrollPane(tblStock);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(800, 360));

        mainCard.add(scrollPane, BorderLayout.CENTER);

        // 2.3 Pie de Tabla (Pagination & Counts)
        JPanel footerTablePanel = new JPanel(new BorderLayout());
        footerTablePanel.setOpaque(false);
        footerTablePanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblTotalProductsCount = new JLabel("Mostrando 6 de 142 productos registrados");
        lblTotalProductsCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotalProductsCount.setForeground(new Color(100, 116, 139)); // Slate 500

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        paginationPanel.setOpaque(false);

        JButton btnPrev = createPageBtn("Anterior");
        JButton btnNext = createPageBtn("Siguiente");
        paginationPanel.add(btnPrev);
        paginationPanel.add(btnNext);

        footerTablePanel.add(lblTotalProductsCount, BorderLayout.WEST);
        footerTablePanel.add(paginationPanel, BorderLayout.EAST);

        mainCard.add(footerTablePanel, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);

        // 3. TARJETAS DE RESUMEN (South Summary Cards)
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);

        // Tarjeta 1: Total Valorizado
        lblTotalValorizado = new JLabel("S/0.00");
        lblTotalValorizado.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalValorizado.setForeground(new Color(15, 23, 42));
        summaryPanel.add(createSummaryCard("Total Valorizado", lblTotalValorizado, new BlueBoxIcon(),
                new Color(248, 250, 252), false));

        // Tarjeta 2: Stock Crítico
        lblCriticalItemsCount = new JLabel("0 Items");
        lblCriticalItemsCount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCriticalItemsCount.setForeground(new Color(15, 23, 42));
        summaryPanel.add(createSummaryCard("Stock Crítico", lblCriticalItemsCount, new AmberAlertIcon(),
                new Color(255, 251, 235), true)); // Yellow tint background

        // Tarjeta 3: Rotación Mensual
        JLabel lblRotacion = new JLabel("82.4%");
        lblRotacion.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRotacion.setForeground(new Color(24, 119, 242));
        summaryPanel.add(createSummaryCard("Rotación Mensual", lblRotacion, new TrendingUpIcon(),
                new Color(248, 250, 252), false));

        add(summaryPanel, BorderLayout.SOUTH);

        // Conectar exportación al botón del controlador
        btnExportar.addActionListener(e -> {
            for (ActionListener al : btnExportarExcelMockListeners) {
                al.actionPerformed(e);
            }
        });

        // Popular datos mock iniciales para que el Diseñador Visual de Eclipse
        // (WindowBuilder)
        // dibuje la interfaz al 100% en tiempo de diseño sin colapsar.
        lastProducts.add(new Producto(1, "Leche Entera 1L", new BigDecimal("1.20"), 1, "PRD-001"));
        lastProducts.add(new Producto(25, "Galletas de Chocolate", new BigDecimal("0.85"), 2, "PRD-045"));
        lastProducts.add(new Producto(122, "Detergente Líquido 2kg", new BigDecimal("5.50"), 3, "PRD-122"));
        lastProducts.add(new Producto(89, "Agua Mineral 500ml", new BigDecimal("0.60"), 4, "PRD-089"));
        lastProducts.add(new Producto(201, "Pan Tajado Integral", new BigDecimal("2.10"), 5, "PRD-201"));
        lastProducts.add(new Producto(330, "Aceite Vegetal 900ml", new BigDecimal("3.40"), 6, "PRD-330"));

        lastStocks.add(new Stock(1, 12, 1));
        lastStocks.add(new Stock(2, 85, 25));
        lastStocks.add(new Stock(3, 5, 122));
        lastStocks.add(new Stock(4, 150, 89));
        lastStocks.add(new Stock(5, 18, 201));
        lastStocks.add(new Stock(6, 8, 330));

        // Insertar filas mock en el tableModel
        tableModel.addRow(new Object[] { 1, "Leche Entera 1L", 12, "¡STOCK CRÍTICO BAJO!" });
        tableModel.addRow(new Object[] { 25, "Galletas de Chocolate", 85, "SUFICIENTE" });
        tableModel.addRow(new Object[] { 122, "Detergente Líquido 2kg", 5, "¡STOCK CRÍTICO BAJO!" });
        tableModel.addRow(new Object[] { 89, "Agua Mineral 500ml", 150, "SUFICIENTE" });
        tableModel.addRow(new Object[] { 201, "Pan Tajado Integral", 18, "SUFICIENTE" });
        tableModel.addRow(new Object[] { 330, "Aceite Vegetal 900ml", 8, "¡STOCK CRÍTICO BAJO!" });

        recalculateTotals();

        // Configurar buscador rápido del mockup
        cbCategoriasFiltro.addActionListener(e -> filterTableData());
    }

    // --- MÉTODOS DE FILTRO Y CÁLCULOS DILIGENTES ---
    private void filterTableData() {
        if (lastStocks == null || lastStocks.isEmpty())
            return;

        Object selected = cbCategoriasFiltro.getSelectedItem();
        int filterCategoryId = -1;
        if (selected instanceof Categoria) {
            filterCategoryId = ((Categoria) selected).getIdCategoria();
        }
        tableModel.setRowCount(0);
        int limiteAlerta = getLimite();

        for (Stock s : lastStocks) {
            Producto matchingProd = null;
            for (Producto p : lastProducts) {
                if (p.getIdProducto() == s.getIdProducto()) {
                    matchingProd = p;
                    break;
                }
            }

            if (matchingProd == null)
                continue;

            // Filtro por categoría
            if (filterCategoryId != -1 && matchingProd.getIdCategoria() != filterCategoryId) {
                continue;
            }

            String estado = s.getCantidad() <= limiteAlerta ? "¡STOCK CRÍTICO BAJO!" : "SUFICIENTE";
            tableModel.addRow(new Object[] {
                    s.getIdStock(),
                    matchingProd.getNombreProducto(),
                    s.getCantidad(),
                    estado
            });
        }
    }

    private void recalculateTotals() {
        BigDecimal totalValorizado = BigDecimal.ZERO;
        int criticalCount = 0;
        int limiteAlerta = getLimite();

        for (Stock s : lastStocks) {
            Producto p = findCachedProduct(s.getIdProducto());
            if (p != null) {
                BigDecimal subtotal = p.getPrecioUnitario().multiply(new BigDecimal(s.getCantidad()));
                totalValorizado = totalValorizado.add(subtotal);
            }
            if (s.getCantidad() <= limiteAlerta) {
                criticalCount++;
            }
        }

        lblTotalValorizado.setText("S/" + String.format("%,.2f", totalValorizado));
        lblCriticalItemsCount.setText(criticalCount + " Items");
        lblTotalProductsCount
                .setText("Mostrando " + lastStocks.size() + " de " + lastStocks.size() + " productos registrados");
    }

    private Producto findCachedProduct(int productId) {
        for (Producto p : lastProducts) {
            if (p.getIdProducto() == productId) {
                return p;
            }
        }
        return null;
    }

    private Stock findCachedStock(int idStock) {
        for (Stock s : lastStocks) {
            if (s.getIdStock() == idStock) {
                return s;
            }
        }
        return null;
    }

    private Categoria findCachedCategory(int idCategoria) {
        for (Categoria c : allCategories) {
            if (c.getIdCategoria() == idCategoria) {
                return c;
            }
        }
        return null;
    }

    // --- CONTROLLER API BINDINGS (100% COMPATIBLES) ---

    public int getLimite() {
        if (cbStock == null)
            return 9999;
        int index = cbStock.getSelectedIndex();
        if (index == 1)
            return 10; // "Stock Bajo / Crítico" -> Límite 10
        if (index == 2)
            return 5; // "Límite Crítico (≤ 5)" -> Límite 5
        if (index == 3)
            return 15; // "Límite Moderado (≤ 15)" -> Límite 15
        return 9999; // "Todos" o "Todos los niveles" -> Muestra todo
    }

    public void addBuscarStockMinimoListener(ActionListener l) {
        btnBuscarAlertas.addActionListener(l);
    }

    public void addVerTodoListener(ActionListener l) {
        btnVerTodo.addActionListener(l);
    }

    private final List<ActionListener> btnExportarExcelMockListeners = new ArrayList<>();

    public void addExportarExcelListener(ActionListener l) {
        btnExportarExcelMockListeners.add(l);
    }

    public void setStockTableData(List<Stock> stocks, List<Producto> products) {
        // Guardamos en caché local para los renders inteligentes
        if (stocks != null)
            this.lastStocks = stocks;
        if (products != null)
            this.lastProducts = products;

        tableModel.setRowCount(0);
        int limiteAlerta = getLimite();

        if (stocks != null) {
            for (Stock s : stocks) {
                String nameProduct = "Prod " + s.getIdProducto();
                if (products != null) {
                    for (Producto p : products) {
                        if (p.getIdProducto() == s.getIdProducto()) {
                            nameProduct = p.getNombreProducto();
                            break;
                        }
                    }
                }

                String estado = s.getCantidad() <= limiteAlerta ? "¡STOCK CRÍTICO BAJO!" : "SUFICIENTE";
                tableModel.addRow(new Object[] {
                        s.getIdStock(),
                        nameProduct,
                        s.getCantidad(),
                        estado
                });
            }
        }

        recalculateTotals();
    }

    public void mostrarMensaje(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message,
                isError ? "Error" : "Éxito",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // --- COMBOBOX & BUTTON STYLING HELPERS ---
    private void styleComboBox(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(160, 32));
        combo.setBackground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        // Simple elegant renderer to center text slightly
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
    }

    private JButton createPageBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(241, 245, 249));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(new Color(100, 116, 139)); // Slate 500
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Icon icon, Color bgColor, boolean isAmber) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                if (isAmber) {
                    g2.setColor(new Color(253, 230, 138)); // Amber 200 soft border
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                } else {
                    g2.setColor(new Color(241, 245, 249)); // Slate 100 border
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139)); // Slate 500

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);

        JLabel lblIcon = new JLabel(icon);
        card.add(lblIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // --- TABLE CELL RENDERERS (ESTÉTICA MODERNÍSIMA) ---

    // Column 0: ID SKU (renders PRD-XXX based on product ID)
    private class SKUCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(new Color(148, 163, 184)); // Slate 400
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 15, 0, 15));

            int idStock = (Integer) value;
            Stock s = findCachedStock(idStock);
            Producto p = s != null ? findCachedProduct(s.getIdProducto()) : null;

            if (p != null) {
                setText("PRD-" + String.format("%03d", p.getIdProducto()));
            } else {
                setText("PRD-" + String.format("%03d", idStock));
            }

            if (isSelected) {
                setBackground(new Color(248, 250, 252));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    // Column 1: Producto (Avatares circulares + Nombre Bold + Proveedor + Warning)
    private class ProductCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblAvatar;
        private final JLabel lblName;
        private final JLabel lblProvider;
        private final JLabel lblWarning;

        public ProductCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setOpaque(true);

            lblAvatar = new JLabel("", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249)); // Circle slate background
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblAvatar.setPreferredSize(new Dimension(36, 36));
            lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblAvatar.setForeground(new Color(71, 85, 105));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            lblName = new JLabel();
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(new Color(15, 23, 42)); // Slate 900

            lblProvider = new JLabel();
            lblProvider.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblProvider.setForeground(new Color(148, 163, 184)); // Slate 400

            textPanel.add(Box.createVerticalGlue());
            textPanel.add(lblName);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(lblProvider);
            textPanel.add(Box.createVerticalGlue());

            lblWarning = new JLabel(new WarningIconMini());
            lblWarning.setVisible(false);

            JPanel textWarningContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            textWarningContainer.setOpaque(false);
            textWarningContainer.add(textPanel);
            textWarningContainer.add(lblWarning);

            add(lblAvatar, BorderLayout.WEST);
            add(textWarningContainer, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            String name = (String) value;
            lblName.setText(name);

            // Buscar producto en caché para obtener proveedor e inicial del avatar
            String initial = name.isEmpty() ? "P" : name.substring(0, 1).toUpperCase();
            lblAvatar.setText(initial);

            // Mapear proveedor ficticio y comprobar stock crítico para warning
            String provider = "Distribuidor Local";
            boolean isCritical = false;

            int modelRow = table.convertRowIndexToModel(row);
            Object idStockObj = table.getModel().getValueAt(modelRow, 0);
            if (idStockObj instanceof Integer) {
                int idStock = (Integer) idStockObj;
                Stock s = findCachedStock(idStock);
                if (s != null) {
                    Producto p = findCachedProduct(s.getIdProducto());
                    if (p != null) {
                        isCritical = s.getCantidad() <= getLimite();
                        switch (p.getIdCategoria()) {
                            case 1:
                                provider = "Alquería Corp";
                                break;
                            case 2:
                                provider = "Global Foods";
                                break;
                            case 3:
                                provider = "CleanHouse S.A.";
                                break;
                            case 4:
                                provider = "PureSprings";
                                break;
                            case 5:
                                provider = "Bimbo Group";
                                break;
                            default:
                                provider = "Aceites del Norte";
                        }
                    }
                }
            }

            lblProvider.setText(provider);
            lblWarning.setVisible(isCritical);

            if (isSelected) {
                setBackground(new Color(248, 250, 252));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    // Column 2: Nivel de Stock (bold units + progress bar + percentage!)
    private class StockLevelCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblText;
        private final JLabel lblPercent;
        private final JProgressBar progressBar;

        public StockLevelCellRenderer() {
            setLayout(new BorderLayout(5, 4));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setOpaque(true);

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            lblText = new JLabel();
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblText.setForeground(new Color(15, 23, 42)); // Slate 900

            lblPercent = new JLabel();
            lblPercent.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblPercent.setForeground(new Color(148, 163, 184)); // Slate 400

            topRow.add(lblText, BorderLayout.WEST);
            topRow.add(lblPercent, BorderLayout.EAST);

            progressBar = new JProgressBar(0, 200) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw rounded track
                    g2.setColor(new Color(241, 245, 249)); // light grey track
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

                    // Calculate fill width
                    int fillWidth = (int) ((((double) getValue()) / getMaximum()) * getWidth());
                    fillWidth = Math.min(fillWidth, getWidth());

                    if (fillWidth > 0) {
                        // Color based on status
                        int val = getValue();
                        if (val <= getLimite()) {
                            g2.setColor(new Color(239, 68, 68)); // red-500
                        } else if (val <= 30) {
                            g2.setColor(new Color(245, 158, 11)); // amber-500
                        } else {
                            g2.setColor(new Color(24, 119, 242)); // vibrant blue
                        }
                        g2.fillRoundRect(0, 0, fillWidth, getHeight(), 4, 4);
                    }
                    g2.dispose();
                }
            };
            progressBar.setPreferredSize(new Dimension(0, 6));

            add(topRow, BorderLayout.NORTH);
            add(progressBar, BorderLayout.SOUTH);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            int qty = (Integer) value;
            lblText.setText(qty + " UNIDADES");

            // Calculate mock percentage based on 200 max units for rendering
            int percent = (qty * 100) / 200;
            percent = Math.min(percent, 100);
            lblPercent.setText(percent + "%");

            progressBar.setValue(qty);

            if (isSelected) {
                setBackground(new Color(248, 250, 252));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    // Column 3: Estado / Alerta -> Renders Category Pill Badge (100% same as
    // mockup)
    private class EstadoAlertaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            // Override super component to custom paint the pill badge
            JLabel label = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new EmptyBorder(0, 10, 0, 10));

            String textCategory = "Abarrotes";
            Color bg = new Color(241, 245, 249); // slate 100
            Color fg = new Color(71, 85, 105); // slate 600

            int modelRow = table.convertRowIndexToModel(row);
            Object idStockObj = table.getModel().getValueAt(modelRow, 0);
            if (idStockObj instanceof Integer) {
                int idStock = (Integer) idStockObj;
                Stock s = findCachedStock(idStock);
                if (s != null) {
                    Producto p = findCachedProduct(s.getIdProducto());
                    if (p != null) {
                        Categoria cat = findCachedCategory(p.getIdCategoria());
                        if (cat != null) {
                            textCategory = cat.getNombreCategoria();
                        }
                        
                        // Harmonious pill colors based on category ID
                        switch (p.getIdCategoria() % 6) {
                            case 1:
                                bg = new Color(254, 226, 226); // Red 100
                                fg = new Color(185, 28, 28); // Red 700
                                break;
                            case 2:
                                bg = new Color(224, 242, 254); // Sky/Blue 100
                                fg = new Color(3, 105, 161); // Sky 700
                                break;
                            case 3:
                                bg = new Color(243, 232, 255); // Purple 100
                                fg = new Color(126, 34, 206); // Purple 700
                                break;
                            case 4:
                                bg = new Color(220, 252, 231); // Green 100
                                fg = new Color(21, 128, 61); // Green 700
                                break;
                            case 5:
                                bg = new Color(254, 243, 199); // Amber 100
                                fg = new Color(180, 83, 9); // Amber 700
                                break;
                            default:
                                bg = new Color(241, 245, 249); // Slate 100
                                fg = new Color(71, 85, 105); // Slate 700
                        }
                    }
                }
            }

            label.setText(textCategory);
            label.setBackground(bg);
            label.setForeground(fg);

            if (isSelected) {
                label.setBackground(bg.darker());
            }

            return label;
        }
    }

    private static class CustomHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setForeground(new Color(100, 116, 139)); // Slate 500
            setBackground(new Color(248, 250, 252));
            setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
            return this;
        }
    }

    // --- CUSTOM VECTOR ICONS (DISEÑO ESPECTACULAR INDEPENDIENTE DE IMÁGENES) ---

    private static class ExportIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(1.5f));

            // Draw box tray
            g2.drawRoundRect(x + 2, y + 10, 12, 4, 1, 1);
            // Draw up arrow
            g2.drawLine(x + 8, y + 2, x + 8, y + 9);
            g2.drawLine(x + 8, y + 2, x + 5, y + 5);
            g2.drawLine(x + 8, y + 2, x + 11, y + 5);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class BlueBoxIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded circular background
            g2.setColor(new Color(224, 242, 254)); // Light Blue
            g2.fillOval(x, y, 36, 36);

            // Draw isometric box lines
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(2.0f));

            Path2D.Double path = new Path2D.Double();
            path.moveTo(x + 18, y + 10);
            path.lineTo(x + 27, y + 15);
            path.lineTo(x + 18, y + 20);
            path.lineTo(x + 9, y + 15);
            path.closePath();
            g2.draw(path);

            g2.drawLine(x + 18, y + 20, x + 18, y + 28);
            g2.drawLine(x + 9, y + 15, x + 9, y + 23);
            g2.drawLine(x + 27, y + 15, x + 27, y + 23);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 36;
        }

        @Override
        public int getIconHeight() {
            return 36;
        }
    }

    private static class AmberAlertIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw circular background
            g2.setColor(new Color(254, 243, 199)); // Amber-100
            g2.fillOval(x, y, 36, 36);

            // Draw warning triangle
            g2.setColor(new Color(217, 119, 6)); // Amber-600
            g2.setStroke(new BasicStroke(2.0f));

            Path2D.Double tri = new Path2D.Double();
            tri.moveTo(x + 18, y + 10);
            tri.lineTo(x + 28, y + 26);
            tri.lineTo(x + 8, y + 26);
            tri.closePath();
            g2.draw(tri);

            // Exclamation dot
            g2.fillRect(x + 17, y + 15, 2, 6);
            g2.fillRect(x + 17, y + 23, 2, 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 36;
        }

        @Override
        public int getIconHeight() {
            return 36;
        }
    }

    private static class TrendingUpIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw circular background
            g2.setColor(new Color(239, 246, 255)); // Blue-50
            g2.fillOval(x, y, 36, 36);

            // Draw trending arrow
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(x + 10, y + 24, x + 16, y + 18);
            g2.drawLine(x + 16, y + 18, x + 21, y + 22);
            g2.drawLine(x + 21, y + 22, x + 27, y + 13);

            // Arrow head
            g2.drawLine(x + 27, y + 13, x + 22, y + 13);
            g2.drawLine(x + 27, y + 13, x + 27, y + 18);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 36;
        }

        @Override
        public int getIconHeight() {
            return 36;
        }
    }

    private static class WarningIconMini implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw warning triangle
            g2.setColor(new Color(220, 38, 38)); // Red-600
            g2.setStroke(new BasicStroke(1.2f));

            Path2D.Double tri = new Path2D.Double();
            tri.moveTo(x + 7, y + 2);
            tri.lineTo(x + 13, y + 12);
            tri.lineTo(x + 1, y + 12);
            tri.closePath();
            g2.draw(tri);

            g2.fillRect(x + 6, y + 5, 2, 4);
            g2.fillRect(x + 6, y + 10, 2, 1);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }

    // --- NESTED CUSTOM SHAPE COMPONENT ---
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;

        public RoundedPanel(int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Soft border tint
            g2.setColor(new Color(241, 245, 249));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }
}
