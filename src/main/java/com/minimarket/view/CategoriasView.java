package com.minimarket.view;

import com.minimarket.model.Categoria;
import com.minimarket.model.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriasView extends JPanel {

    // In-memory cache for customized category metadata (since DB schema only has Id
    // and Nombre)
    public static final Map<Integer, String> customSkuPrefixes = new HashMap<>();
    public static final Map<Integer, String> customDescriptions = new HashMap<>();
    public static final Map<Integer, String> customTagColors = new HashMap<>();
    public static final Map<Integer, String> customIcons = new HashMap<>();
    public static final Map<Integer, Boolean> customPosActive = new HashMap<>();

    // Left Panel Components
    private JTextField txtSearchCategories;
    private JList<Categoria> lstCategories;
    private DefaultListModel<Categoria> categoriesListModel;
    private JButton btnCrearCategoria;
    private JLabel lblMostPopulatedVal;
    private JLabel lblTotalStockVal;
    private JProgressBar progressTotalStock;
    private JLabel lblCategoriesCountBadge;

    // Right CardLayout Container
    private CardLayout rightCardLayout;
    private JPanel rightCardContainer;

    // CARD 1: Product Grid Components
    private JLabel lblCategoryTitle;
    private JLabel lblCategorySubtitle;
    private JLabel lblSkuBadge;
    private JTextField txtSearchProducts;
    private JButton btnOrdenar;
    private JPanel pnlProductsGrid; // Grid panel for product cards
    private JScrollPane scrollProductsGrid;
    private JButton btnAjustarReglas;
    private JLabel lblVisibilidadConfig;

    // Internal State
    private List<Categoria> originalCategories = new ArrayList<>();
    private Map<Integer, Integer> categoryProductCounts = new HashMap<>();
    private List<Producto> currentProducts = new ArrayList<>();
    private String currentSortOrder = "None";

    public CategoriasView() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0)); // No horizontal gap here, as leftPanel has border
        setOpaque(false);

        // 1. LEFT PANEL (Lista de Categorías)
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(226, 232, 240)),
                new EmptyBorder(24, 24, 24, 24)));

        // Botón "Crear Nueva Categoría"
        btnCrearCategoria = new JButton("+ Crear Nueva Categoría") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(21, 128, 61));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(22, 163, 74));
                } else {
                    g2.setColor(new Color(24, 119, 242)); // Blue primary
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCrearCategoria.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCrearCategoria.setForeground(Color.WHITE);
        btnCrearCategoria.setContentAreaFilled(false);
        btnCrearCategoria.setFocusPainted(false);
        btnCrearCategoria.setBorder(new EmptyBorder(10, 0, 10, 0));
        btnCrearCategoria.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCrearCategoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Subtítulo "PRINCIPALES CATEGORÍAS"
        JPanel categoriesHeader = new JPanel(new BorderLayout());
        categoriesHeader.setOpaque(false);
        categoriesHeader.setBorder(new EmptyBorder(15, 4, 8, 4));
        categoriesHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel lblHeaderTitle = new JLabel("PRINCIPALES CATEGORÍAS");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeaderTitle.setForeground(new Color(148, 163, 184));
        categoriesHeader.add(lblHeaderTitle, BorderLayout.WEST);

        lblCategoriesCountBadge = new JLabel("0 TOTAL", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCategoriesCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lblCategoriesCountBadge.setForeground(new Color(100, 116, 139));
        lblCategoriesCountBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
        categoriesHeader.add(lblCategoriesCountBadge, BorderLayout.EAST);

        // Campo de búsqueda de categorías
        RoundedSearchField pnlSearch = new RoundedSearchField();
        txtSearchCategories = pnlSearch.getTextField();
        pnlSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Lista de categorías
        categoriesListModel = new DefaultListModel<>();
        lstCategories = new JList<>(categoriesListModel);
        lstCategories.setCellRenderer(new CategoryListCellRenderer());
        lstCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstCategories.setBorder(null);
        lstCategories.setOpaque(false);
        lstCategories.setBackground(Color.WHITE);
        lstCategories.setFixedCellHeight(52);

        JScrollPane scrollCategories = new JScrollPane(lstCategories);
        scrollCategories.setBorder(null);
        scrollCategories.setOpaque(false);
        scrollCategories.getViewport().setOpaque(false);

        // Panel de Resumen de Inventario
        RoundedPanel pnlSummary = new RoundedPanel(12);
        pnlSummary.setBackground(Color.WHITE);
        pnlSummary.setLayout(new BoxLayout(pnlSummary, BoxLayout.Y_AXIS));
        pnlSummary.setBorder(new EmptyBorder(14, 16, 14, 16));
        pnlSummary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel lblSummaryTitle = new JLabel("RESUMEN DE INVENTARIO");
        lblSummaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblSummaryTitle.setForeground(new Color(148, 163, 184));

        JPanel rowMostPopulated = new JPanel(new BorderLayout());
        rowMostPopulated.setOpaque(false);
        JLabel lblMostPopulated = new JLabel("Categoría Más Poblada");
        lblMostPopulated.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMostPopulated.setForeground(new Color(100, 116, 139));
        lblMostPopulatedVal = new JLabel("Snacks");
        lblMostPopulatedVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMostPopulatedVal.setForeground(new Color(24, 119, 242));
        rowMostPopulated.add(lblMostPopulated, BorderLayout.WEST);
        rowMostPopulated.add(lblMostPopulatedVal, BorderLayout.EAST);

        JPanel rowTotalStock = new JPanel(new BorderLayout());
        rowTotalStock.setOpaque(false);
        JLabel lblTotalStock = new JLabel("Stock Total");
        lblTotalStock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotalStock.setForeground(new Color(100, 116, 139));
        lblTotalStockVal = new JLabel("14,204");
        lblTotalStockVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTotalStockVal.setForeground(new Color(15, 23, 42));
        rowTotalStock.add(lblTotalStock, BorderLayout.WEST);
        rowTotalStock.add(lblTotalStockVal, BorderLayout.EAST);

        progressTotalStock = new JProgressBar(0, 100);
        progressTotalStock.setValue(75);
        progressTotalStock.setPreferredSize(new Dimension(0, 6));
        progressTotalStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressTotalStock.setForeground(new Color(24, 119, 242));
        progressTotalStock.setBackground(new Color(241, 245, 249));
        progressTotalStock.setBorder(null);

        pnlSummary.add(lblSummaryTitle);
        pnlSummary.add(Box.createVerticalStrut(10));
        pnlSummary.add(rowMostPopulated);
        pnlSummary.add(Box.createVerticalStrut(6));
        pnlSummary.add(rowTotalStock);
        pnlSummary.add(Box.createVerticalStrut(8));
        pnlSummary.add(progressTotalStock);

        leftPanel.add(btnCrearCategoria);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(categoriesHeader);
        leftPanel.add(scrollCategories);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(pnlSummary);

        // 2. RIGHT CARD CONTAINER (CardLayout)
        rightCardLayout = new CardLayout();
        rightCardContainer = new JPanel(rightCardLayout);
        rightCardContainer.setOpaque(false);

        // ==========================================
        // CARD 1: PRODUCT GRID PANEL (Vista de Categoría)
        // ==========================================
        JPanel pnlGridCard = new JPanel(new BorderLayout(0, 14));
        pnlGridCard.setOpaque(false);
        pnlGridCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header de la Categoría Seleccionada
        JPanel pnlCategoryHeader = new JPanel(new BorderLayout(10, 0));
        pnlCategoryHeader.setOpaque(false);

        JPanel pnlTitleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTitleWrapper.setOpaque(false);

        // Cup/Category Title Label
        lblCategoryTitle = new JLabel("Bebidas y Jugos", new ViewCategoryIcon("Cup", true), SwingConstants.LEFT);
        lblCategoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCategoryTitle.setForeground(new Color(15, 23, 42));
        pnlTitleWrapper.add(lblCategoryTitle);

        lblSkuBadge = new JLabel("[BEV]", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSkuBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSkuBadge.setForeground(new Color(24, 119, 242));
        lblSkuBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        // Subtitle wrapper to stretch below
        JPanel pnlHeaderLeftWrapper = new JPanel();
        pnlHeaderLeftWrapper.setLayout(new BoxLayout(pnlHeaderLeftWrapper, BoxLayout.Y_AXIS));
        pnlHeaderLeftWrapper.setOpaque(false);

        lblCategorySubtitle = new JLabel("Previsualización de los productos vinculados a");
        lblCategorySubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCategorySubtitle.setForeground(new Color(100, 116, 139));

        JPanel pnlSubtitleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlSubtitleWrapper.setOpaque(false);
        pnlSubtitleWrapper.add(lblCategorySubtitle);
        pnlSubtitleWrapper.add(lblSkuBadge);

        pnlHeaderLeftWrapper.add(pnlTitleWrapper);
        pnlHeaderLeftWrapper.add(Box.createVerticalStrut(4));
        pnlHeaderLeftWrapper.add(pnlSubtitleWrapper);

        // Actions Header
        JPanel pnlGridHeaderActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlGridHeaderActions.setOpaque(false);

        RoundedSearchField pnlGridSearch = new RoundedSearchField();
        txtSearchProducts = pnlGridSearch.getTextField();
        pnlGridSearch.setPreferredSize(new Dimension(180, 32));

        btnOrdenar = new JButton("Ordenar", new OrderIcon()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnOrdenar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnOrdenar.setForeground(new Color(71, 85, 105));
        btnOrdenar.setContentAreaFilled(false);
        btnOrdenar.setFocusPainted(false);
        btnOrdenar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOrdenar.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnOrdenar.addActionListener(e -> {
            if ("None".equals(currentSortOrder)) {
                currentSortOrder = "NameAsc";
                btnOrdenar.setText("Ordenar: A-Z");
            } else if ("NameAsc".equals(currentSortOrder)) {
                currentSortOrder = "PriceAsc";
                btnOrdenar.setText("Ordenar: S/ Min");
            } else {
                currentSortOrder = "None";
                btnOrdenar.setText("Ordenar");
            }
            filterProducts();
        });

        JLabel lblGridHeaderDots = new JLabel("⋮");
        lblGridHeaderDots.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGridHeaderDots.setForeground(new Color(148, 163, 184));
        lblGridHeaderDots.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlGridHeaderActions.add(pnlGridSearch);
        pnlGridHeaderActions.add(btnOrdenar);
        pnlGridHeaderActions.add(lblGridHeaderDots);

        pnlCategoryHeader.add(pnlHeaderLeftWrapper, BorderLayout.WEST);
        pnlCategoryHeader.add(pnlGridHeaderActions, BorderLayout.EAST);

        // Cuadrícula de productos
        pnlProductsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)) {
            @Override
            public Dimension getPreferredSize() {
                int count = getComponentCount();
                if (count == 0)
                    return new Dimension(100, 100);

                java.awt.Container parent = getParent();
                int width = parent != null ? parent.getWidth() : 600;
                if (width <= 0)
                    width = 600;

                int cardWidth = 160;
                int gap = 15;

                int cols = (width - gap) / (cardWidth + gap);
                if (cols < 1)
                    cols = 1;

                int rows = (int) Math.ceil((double) count / cols);
                int h = rows * 220 + (rows + 1) * gap;
                return new Dimension(width, h);
            }
        };
        pnlProductsGrid.setBackground(new Color(248, 250, 252));
        pnlProductsGrid.setOpaque(false);

        scrollProductsGrid = new JScrollPane(pnlProductsGrid);
        scrollProductsGrid.setBorder(null);
        scrollProductsGrid.setOpaque(false);
        scrollProductsGrid.getViewport().setOpaque(false);
        scrollProductsGrid.getVerticalScrollBar().setUnitIncrement(16);

        // Configuración de Visibilidad inferior
        RoundedPanel pnlVisibility = new RoundedPanel(12) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // Light Blue background
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(191, 219, 254)); // Light Blue border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        pnlVisibility.setBackground(new Color(239, 246, 255));
        pnlVisibility.setLayout(new BorderLayout(15, 0));
        pnlVisibility.setBorder(new EmptyBorder(14, 18, 14, 18));
        pnlVisibility.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel pnlVisText = new JPanel();
        pnlVisText.setLayout(new BoxLayout(pnlVisText, BoxLayout.Y_AXIS));
        pnlVisText.setOpaque(false);

        JLabel lblVisTitle = new JLabel("Configuración de Visibilidad");
        lblVisTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVisTitle.setForeground(new Color(24, 119, 242));

        lblVisibilidadConfig = new JLabel(
                "Esta categoría está configurada como PÚBLICA y aparece en el terminal de ventas (POS).");
        lblVisibilidadConfig.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVisibilidadConfig.setForeground(new Color(100, 116, 139));

        pnlVisText.add(lblVisTitle);
        pnlVisText.add(Box.createVerticalStrut(2));
        pnlVisText.add(lblVisibilidadConfig);

        btnAjustarReglas = new JButton("Ajustar Reglas") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAjustarReglas.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAjustarReglas.setForeground(new Color(71, 85, 105));
        btnAjustarReglas.setContentAreaFilled(false);
        btnAjustarReglas.setFocusPainted(false);
        btnAjustarReglas.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnAjustarReglas.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Visibilidad slider vector icon
        JLabel lblVisIcon = new JLabel(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(24, 119, 242));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawOval(x + 2, y + 2, 20, 20);
                g2.drawLine(x + 7, y + 12, x + 17, y + 12);
                g2.fillOval(x + 10, y + 9, 6, 6);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 24;
            }

            @Override
            public int getIconHeight() {
                return 24;
            }
        });

        pnlVisibility.add(lblVisIcon, BorderLayout.WEST);
        pnlVisibility.add(pnlVisText, BorderLayout.CENTER);
        pnlVisibility.add(btnAjustarReglas, BorderLayout.EAST);

        pnlGridCard.add(pnlCategoryHeader, BorderLayout.NORTH);
        pnlGridCard.add(scrollProductsGrid, BorderLayout.CENTER);
        pnlGridCard.add(pnlVisibility, BorderLayout.SOUTH);

        // Añadir la pantalla principal al contenedor de tarjetas
        rightCardContainer.add(pnlGridCard, "grid");

        add(leftPanel, BorderLayout.WEST);
        add(rightCardContainer, BorderLayout.CENTER);

        // Eventos locales
        btnAjustarReglas.addActionListener(e -> showCategoryEditor(false));

        // Búsqueda en vivo de categorías
        txtSearchCategories.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterCategories();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterCategories();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterCategories();
            }
        });

        // Búsqueda en vivo de productos
        txtSearchProducts.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterProducts();
            }
        });
    }

    // Navegación
    public void showProductsGrid() {
        rightCardLayout.show(rightCardContainer, "grid");
        filterProducts();
    }

    public void showCategoryEditor(boolean isNew) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof DashboardView) {
            DashboardView dash = (DashboardView) parentWindow;
            if (isNew) {
                dash.setViewPanel(new CategoriasAddView());
                dash.setHeaderTitle("Registrar Nueva Categoría");
            } else {
                Categoria current = getSelectedCategory();
                if (current != null) {
                    dash.setViewPanel(new CategoriasEditView(current));
                    dash.setHeaderTitle("Ajustar Reglas de Categoría");
                }
            }
        }
    }

    // Public Getters / Setters para datos
    public Categoria getSelectedCategory() {
        return lstCategories.getSelectedValue();
    }



    public void setCategorias(List<Categoria> categories, Map<Integer, Integer> productCounts) {
        this.originalCategories = new ArrayList<>(categories);
        this.categoryProductCounts = new HashMap<>(productCounts);
        filterCategories();

        if (lblCategoriesCountBadge != null) {
            lblCategoriesCountBadge.setText(categories.size() + " TOTAL");
        }



        if (!categories.isEmpty() && lstCategories.getSelectedIndex() < 0) {
            lstCategories.setSelectedIndex(0);
        }
    }

    public void setProductos(List<Producto> products) {
        this.currentProducts = new ArrayList<>(products);
        filterProducts();
    }

    public void setResumenInventario(String mostPopulated, int totalStock, int maxStockEstimate) {
        lblMostPopulatedVal.setText(mostPopulated);
        lblTotalStockVal.setText(String.format("%,d", totalStock));
        if (maxStockEstimate > 0) {
            int pct = (int) ((double) totalStock / maxStockEstimate * 100);
            progressTotalStock.setValue(Math.min(100, pct));
        } else {
            progressTotalStock.setValue(0);
        }
    }

    public void mostrarMensaje(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message,
                isError ? "Error" : "Éxito",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // Filtros locales
    private void filterCategories() {
        String filter = txtSearchCategories.getText().trim().toLowerCase();
        categoriesListModel.clear();
        for (Categoria c : originalCategories) {
            if (filter.isEmpty() || c.getNombreCategoria().toLowerCase().contains(filter)) {
                categoriesListModel.addElement(c);
            }
        }
    }

    // Obtener Stock real o fallback en mockup de forma consistente
    private int getProductStock(int idProducto) {
        try {
            Connection connection = com.minimarket.config.DatabaseConnection.getInstance().getConnection();
            if (connection != null) {
                String sql = "SELECT Cantidad FROM stock WHERE Id_Producto = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, idProducto);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("Cantidad");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback
        }
        return (idProducto * 7 + 12) % 150;
    }

    private void filterProducts() {
        pnlProductsGrid.removeAll();

        // 1. Calcular estadísticas derechas para editor
        int totalProducts = currentProducts.size();

        // Filtrado por buscador para la vista de Grid
        String filterStr = txtSearchProducts != null ? txtSearchProducts.getText().trim().toLowerCase() : "";

        List<Producto> productsList = new ArrayList<>(currentProducts);
        if ("NameAsc".equals(currentSortOrder)) {
            productsList.sort((p1, p2) -> p1.getNombreProducto().compareToIgnoreCase(p2.getNombreProducto()));
        } else if ("PriceAsc".equals(currentSortOrder)) {
            productsList.sort((p1, p2) -> p1.getPrecioUnitario().compareTo(p2.getPrecioUnitario()));
        }

        for (Producto p : productsList) {
            // Añadir a la vista Grid (si calza con la búsqueda)
            if (filterStr.isEmpty() || p.getNombreProducto().toLowerCase().contains(filterStr)) {
                pnlProductsGrid.add(createProductCard(p));
            }
        }

        Categoria current = getSelectedCategory();
        String currentCatName = current != null ? current.getNombreCategoria() : "Bebidas y Jugos";
        String skuPrefix = current != null ? getMockSkuPrefix(current.getNombreCategoria()) : "BEV";
        if (customSkuPrefixes.containsKey(current != null ? current.getIdCategoria() : 0)) {
            skuPrefix = customSkuPrefixes.get(current.getIdCategoria());
        }

        // Actualizar encabezados de la vista Grid
        lblCategoryTitle.setText(currentCatName);
        lblCategoryTitle.setIcon(getCategoryHeaderIcon(currentCatName));
        lblSkuBadge.setText("[" + skuPrefix + "]");
        lblCategorySubtitle.setText("Previsualización de los " + totalProducts + " productos vinculados a");

        pnlProductsGrid.revalidate();
        pnlProductsGrid.repaint();
    }

    private Icon getCategoryHeaderIcon(String catName) {
        String n = catName.toLowerCase();
        String type = "Tag";
        if (n.contains("bebida"))
            type = "Cup";
        else if (n.contains("fruta") || n.contains("verdura"))
            type = "Apple";
        else if (n.contains("carne") || n.contains("ave"))
            type = "Meat";
        else if (n.contains("snack") || n.contains("dulce"))
            type = "Cookie";
        else if (n.contains("lácteo") || n.contains("lacteo") || n.contains("huevo"))
            type = "Box";
        else if (n.contains("vino") || n.contains("licor"))
            type = "Wine";

        return new ViewCategoryIcon(type, true);
    }

    // Creación dinámica de Tarjetas de Productos (Grid View)
    private JPanel createProductCard(Producto p) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(160, 220));
        card.setLayout(new BorderLayout());

        // Imagen/Header del producto
        JPanel pnlImageHeader = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dibujar fondo superior redondeado crema/gris suave
                g2.setColor(new Color(245, 245, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 12, 12, 12);
                g2.dispose();
            }
        };
        pnlImageHeader.setPreferredSize(new Dimension(0, 130));
        pnlImageHeader.setOpaque(false);
        pnlImageHeader.setBorder(new EmptyBorder(8, 8, 8, 8));

        // SKU Badge
        Categoria current = getSelectedCategory();
        String prefix = current != null ? getMockSkuPrefix(current.getNombreCategoria()) : "BEV";
        if (current != null && customSkuPrefixes.containsKey(current.getIdCategoria())) {
            prefix = customSkuPrefixes.get(current.getIdCategoria());
        }
        String sku = prefix + "-" + String.format("%03d", p.getIdProducto());

        JLabel lblSku = new JLabel(sku) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSku.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lblSku.setForeground(new Color(100, 116, 139));
        lblSku.setBorder(new EmptyBorder(2, 6, 2, 6));
        lblSku.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel pnlSkuWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlSkuWrapper.setOpaque(false);
        pnlSkuWrapper.add(lblSku);

        // Icono representativo del tipo de producto (Ilustración vectorial de alta
        // fidelidad)
        JLabel lblProductIcon = new JLabel(new ProductVectorIcon(p.getNombreProducto(), 64));
        lblProductIcon.setHorizontalAlignment(SwingConstants.CENTER);

        pnlImageHeader.add(pnlSkuWrapper, BorderLayout.NORTH);
        pnlImageHeader.add(lblProductIcon, BorderLayout.CENTER);

        // Info del Producto
        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblName = new JLabel(p.getNombreProducto());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblName.setForeground(new Color(15, 23, 42));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlBottomDetails = new JPanel(new BorderLayout());
        pnlBottomDetails.setOpaque(false);
        pnlBottomDetails.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrice = new JLabel("S/ " + String.format("%.2f", p.getPrecioUnitario()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPrice.setForeground(new Color(24, 119, 242));

        int stockVal = getProductStock(p.getIdProducto());
        JLabel lblStockVal = new JLabel("STOCK: " + stockVal);
        lblStockVal.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lblStockVal.setForeground(stockVal <= 5 ? new Color(220, 38, 38) : new Color(148, 163, 184));

        pnlBottomDetails.add(lblPrice, BorderLayout.WEST);
        pnlBottomDetails.add(lblStockVal, BorderLayout.EAST);

        pnlInfo.add(lblName);
        pnlInfo.add(Box.createVerticalStrut(6));
        pnlInfo.add(pnlBottomDetails);

        card.add(pnlImageHeader, BorderLayout.NORTH);
        card.add(pnlInfo, BorderLayout.CENTER);

        return card;
    }

    private String getMockSkuPrefix(String catName) {
        String n = catName.toUpperCase();
        if (n.contains("BEBIDA") || n.contains("JUGO"))
            return "BEV";
        if (n.contains("FRUTA") || n.contains("VERDURA"))
            return "FRV";
        if (n.contains("CARNE") || n.contains("AVE"))
            return "CRN";
        if (n.contains("SNACK") || n.contains("DULCE"))
            return "SNK";
        if (n.contains("LÁCTEO") || n.contains("LACTEO") || n.contains("HUEVO"))
            return "LAC";
        if (n.contains("VINO") || n.contains("LICOR"))
            return "VIN";
        if (n.contains("LIMPIEZA"))
            return "LIM";
        if (n.contains("CUIDADO") || n.contains("PERSONAL"))
            return "CUI";
        if (n.contains("PAN"))
            return "PAN";
        return n.substring(0, Math.min(3, n.length()));
    }



    // Listeners bindings
    public void addCategorySelectionListener(ListSelectionListener l) {
        lstCategories.addListSelectionListener(l);
    }

    public void addCrearCategoriaListener(ActionListener l) {
        btnCrearCategoria.addActionListener(l);
    }

    // Inner classes for graphics
    private class CategoryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Categoria c = (Categoria) value;

            JPanel pnl = new JPanel(new BorderLayout(12, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected) {
                        g2.setColor(new Color(239, 246, 255)); // Light Blue select background
                        g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 8, 8);
                        g2.setColor(new Color(59, 130, 246));
                        g2.fillRect(8, 6, 3, getHeight() - 12);
                    }
                    g2.dispose();
                }
            };
            pnl.setOpaque(false);
            pnl.setBorder(new EmptyBorder(6, 16, 6, 16));

            // Icono
            JLabel lblIcon = new JLabel(getCategoryVectorIcon(c.getNombreCategoria(), isSelected));
            lblIcon.setPreferredSize(new Dimension(32, 32));

            // Text Wrapper
            JPanel pnlTexts = new JPanel(new GridLayout(2, 1, 0, 1));
            pnlTexts.setOpaque(false);

            JLabel lblName = new JLabel(c.getNombreCategoria());
            lblName.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 12));
            lblName.setForeground(isSelected ? new Color(24, 119, 242) : new Color(15, 23, 42));

            int prodCount = categoryProductCounts.getOrDefault(c.getIdCategoria(), 0);
            String prefix = getMockSkuPrefix(c.getNombreCategoria());
            if (customSkuPrefixes.containsKey(c.getIdCategoria())) {
                prefix = customSkuPrefixes.get(c.getIdCategoria());
            }

            JLabel lblDetails = new JLabel(prodCount + " productos • " + prefix);
            lblDetails.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblDetails.setForeground(new Color(148, 163, 184));

            pnlTexts.add(lblName);
            pnlTexts.add(lblDetails);

            pnl.add(lblIcon, BorderLayout.WEST);
            pnl.add(pnlTexts, BorderLayout.CENTER);

            return pnl;
        }

        private Icon getCategoryVectorIcon(String name, boolean active) {
            String n = name.toLowerCase();
            String type = "Tag";
            if (n.contains("bebida"))
                type = "Cup";
            else if (n.contains("fruta") || n.contains("verdura"))
                type = "Apple";
            else if (n.contains("carne") || n.contains("ave"))
                type = "Meat";
            else if (n.contains("snack") || n.contains("dulce"))
                type = "Cookie";
            else if (n.contains("lácteo") || n.contains("lacteo") || n.contains("huevo"))
                type = "Box";
            else if (n.contains("vino") || n.contains("licor"))
                type = "Wine";

            return new ViewCategoryIcon(type, active);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 245, 249));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // Vector drawing for category icons
    private static class ViewCategoryIcon implements Icon {
        private final String type;
        private final boolean active;

        public ViewCategoryIcon(String type) {
            this(type, false);
        }

        public ViewCategoryIcon(String type, boolean active) {
            this.type = type;
            this.active = active;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color primary = active ? new Color(24, 119, 242) : new Color(100, 116, 139);

            g2.setColor(primary);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if ("Cup".equalsIgnoreCase(type)) {
                g2.drawRoundRect(x + 4, y + 6, 12, 12, 4, 4);
                g2.drawArc(x + 12, y + 8, 6, 8, 270, 180);
                g2.drawLine(x + 3, y + 18, x + 17, y + 18);
            } else if ("Apple".equalsIgnoreCase(type)) {
                g2.drawOval(x + 4, y + 6, 10, 12);
                g2.drawOval(x + 10, y + 6, 10, 12);
                g2.drawLine(x + 12, y + 6, x + 14, y + 2);
            } else if ("Meat".equalsIgnoreCase(type)) {
                Path2D path = new Path2D.Double();
                path.moveTo(x + 5, y + 6);
                path.curveTo(x + 10, y + 2, x + 18, y + 4, x + 19, y + 10);
                path.curveTo(x + 20, y + 16, x + 12, y + 20, x + 7, y + 17);
                path.curveTo(x + 2, y + 14, x + 2, y + 9, x + 5, y + 6);
                path.closePath();
                g2.draw(path);
                g2.drawOval(x + 10, y + 10, 3, 3);
            } else if ("Cookie".equalsIgnoreCase(type)) {
                g2.drawOval(x + 3, y + 3, 18, 18);
                g2.fillOval(x + 8, y + 7, 2, 2);
                g2.fillOval(x + 14, y + 9, 2, 2);
                g2.fillOval(x + 9, y + 13, 2, 2);
                g2.fillOval(x + 14, y + 14, 2, 2);
            } else if ("Box".equalsIgnoreCase(type)) {
                g2.drawRect(x + 4, y + 4, 16, 16);
                g2.drawLine(x + 4, y + 4, x + 20, y + 20);
                g2.drawLine(x + 20, y + 4, x + 4, y + 20);
            } else if ("Wine".equalsIgnoreCase(type)) {
                g2.drawArc(x + 6, y + 4, 12, 10, 180, 180);
                g2.drawLine(x + 6, y + 4, x + 18, y + 4);
                g2.drawLine(x + 12, y + 14, x + 12, y + 18);
                g2.drawLine(x + 8, y + 18, x + 16, y + 18);
            } else {
                g2.drawRoundRect(x + 5, y + 5, 14, 14, 4, 4);
                g2.drawOval(x + 9, y + 9, 4, 4);
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
    }

    private static class RoundedSearchField extends JPanel {
        private JTextField txt;

        public RoundedSearchField() {
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(2, 10, 2, 10));

            txt = new JTextField();
            txt.setBorder(null);
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txt.setForeground(new Color(15, 23, 42));

            JLabel lblSearch = new JLabel(new SearchIcon());

            add(lblSearch, BorderLayout.WEST);
            add(txt, BorderLayout.CENTER);
        }

        public JTextField getTextField() {
            return txt;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }

    private static class SearchIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x + 2, y + 2, 7, 7);
            g2.drawLine(x + 8, y + 8, x + 12, y + 12);
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

    private static class OrderIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 2, y + 4, x + 14, y + 4);
            g2.drawLine(x + 4, y + 8, x + 12, y + 8);
            g2.drawLine(x + 6, y + 12, x + 10, y + 12);
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

    private static class ProductVectorIcon implements Icon {
        private final String name;
        private final int size;

        public ProductVectorIcon(String name, int size) {
            this.name = name.toLowerCase();
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (size != 64) {
                double scale = (double) size / 64.0;
                g2.translate(x, y);
                g2.scale(scale, scale);
                x = 0;
                y = 0;
            }

            int cx = x + 8;
            int cy = y + 8;

            if (name.contains("café") || name.contains("cafe")) {
                // Coffee bag (brown)
                g2.setPaint(new GradientPaint(cx + 16, cy + 8, new Color(110, 70, 45), cx + 48, cy + 56,
                        new Color(70, 40, 20)));
                int[] xPoints = { cx + 20, cx + 44, cx + 40, cx + 24 };
                int[] yPoints = { cy + 8, cy + 8, cy + 14, cy + 14 };
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.fillRoundRect(cx + 16, cy + 14, 32, 42, 6, 6);

                // Label on bag
                g2.setColor(new Color(230, 210, 180));
                g2.fillRoundRect(cx + 22, cy + 24, 20, 20, 4, 4);

                g2.setColor(new Color(110, 70, 45));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(cx + 26, cy + 30, cx + 38, cy + 30);
                g2.drawLine(cx + 26, cy + 34, cx + 34, cy + 34);
            } else if (name.contains("naranja") || name.contains("jugo")) {
                // Orange juice / slice
                g2.setColor(new Color(249, 115, 22)); // Orange color
                g2.fillOval(cx + 12, cy + 12, 40, 40);

                // Orange rim/skin
                g2.setColor(new Color(251, 146, 60));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(cx + 12, cy + 12, 40, 40);

                // Inside lines (wedges)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                int ocx = cx + 32;
                int ocy = cy + 32;
                g2.drawOval(cx + 16, cy + 16, 32, 32);
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    g2.drawLine(ocx, ocy, (int) (ocx + 16 * Math.cos(angle)), (int) (ocy + 16 * Math.sin(angle)));
                }

                // Green leaf
                g2.setColor(new Color(34, 197, 94));
                g2.fillOval(cx + 36, cy + 2, 14, 8);
                g2.setColor(new Color(21, 128, 61));
                g2.drawOval(cx + 36, cy + 2, 14, 8);
            } else if (name.contains("agua") || name.contains("mineral")) {
                // Glass water bottle (blue/translucent)
                g2.setPaint(new GradientPaint(cx + 24, cy + 4, new Color(186, 230, 253, 200), cx + 40, cy + 60,
                        new Color(56, 189, 248, 150)));
                g2.fillRoundRect(cx + 26, cy + 4, 12, 12, 2, 2); // Neck
                // Body
                Path2D.Float bottle = new Path2D.Float();
                bottle.moveTo(cx + 24, cy + 16);
                bottle.curveTo(cx + 24, cy + 16, cx + 20, cy + 28, cx + 20, cy + 32);
                bottle.lineTo(cx + 20, cy + 56);
                bottle.curveTo(cx + 20, cy + 58, cx + 22, cy + 60, cx + 24, cy + 60);
                bottle.lineTo(cx + 40, cy + 60);
                bottle.curveTo(cx + 42, cy + 60, cx + 44, cy + 58, cx + 44, cy + 56);
                bottle.lineTo(cx + 44, cy + 32);
                bottle.curveTo(cx + 44, cy + 28, cx + 40, cy + 16, cx + 40, cy + 16);
                bottle.closePath();
                g2.fill(bottle);

                // Draw bottle outline
                g2.setColor(new Color(14, 165, 233));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(bottle);
                g2.drawRoundRect(cx + 26, cy + 4, 12, 12, 2, 2);

                // Cap (grey)
                g2.setColor(new Color(148, 163, 184));
                g2.fillRoundRect(cx + 25, cy + 2, 14, 4, 2, 2);
            } else if (name.contains("té") || name.contains("te ") || name.contains("verde")) {
                // Green tea canister
                g2.setPaint(new GradientPaint(cx + 18, cy + 10, new Color(74, 117, 89), cx + 46, cy + 56,
                        new Color(40, 80, 50)));
                g2.fillRoundRect(cx + 18, cy + 10, 28, 46, 6, 6);

                // Canister lid
                g2.setColor(new Color(212, 175, 55)); // Gold lid
                g2.fillRoundRect(cx + 20, cy + 6, 24, 6, 2, 2);

                // Label
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx + 22, cy + 24, 20, 20, 4, 4);
                // A small green leaf on label
                g2.setColor(new Color(34, 197, 94));
                g2.fillOval(cx + 28, cy + 30, 8, 8);
            } else if (name.contains("energizante") || name.contains("lata")) {
                // Metallic blue/orange energy can
                g2.setPaint(new GradientPaint(cx + 20, cy + 10, new Color(59, 130, 246), cx + 44, cy + 54,
                        new Color(30, 58, 138)));
                g2.fillRoundRect(cx + 20, cy + 10, 24, 44, 6, 6);

                // Shiny metallic outline
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx + 20, cy + 10, 24, 44, 6, 6);

                // Lightning bolt (orange/yellow)
                g2.setColor(new Color(245, 158, 11));
                Path2D.Float bolt = new Path2D.Float();
                bolt.moveTo(cx + 34, cy + 18);
                bolt.lineTo(cx + 26, cy + 32);
                bolt.lineTo(cx + 32, cy + 32);
                bolt.lineTo(cx + 30, cy + 46);
                bolt.lineTo(cx + 38, cy + 32);
                bolt.lineTo(cx + 32, cy + 32);
                bolt.closePath();
                g2.fill(bolt);
            } else if (name.contains("cola") || name.contains("soda") || name.contains("refresco")) {
                // Plastic Coca-cola bottle
                g2.setPaint(new GradientPaint(cx + 24, cy + 6, new Color(80, 20, 10), cx + 40, cy + 58,
                        new Color(30, 10, 5)));

                // Bottle neck and body shape
                Path2D.Float bottle = new Path2D.Float();
                bottle.moveTo(cx + 27, cy + 8);
                bottle.lineTo(cx + 27, cy + 18);
                bottle.curveTo(cx + 27, cy + 18, cx + 22, cy + 26, cx + 22, cy + 32);
                bottle.lineTo(cx + 22, cy + 56);
                bottle.lineTo(cx + 42, cy + 56);
                bottle.lineTo(cx + 42, cy + 32);
                bottle.curveTo(cx + 42, cy + 32, cx + 37, cy + 26, cx + 37, cy + 18);
                bottle.lineTo(cx + 37, cy + 8);
                bottle.closePath();
                g2.fill(bottle);

                // Red label
                g2.setColor(new Color(220, 38, 38));
                g2.fillRect(cx + 22, cy + 32, 20, 12);

                // Label text line (white wave)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawArc(cx + 23, cy + 36, 18, 4, 0, 180);

                // Cap (red or black)
                g2.setColor(new Color(220, 38, 38));
                g2.fillRect(cx + 26, cy + 4, 12, 4);
            } else {
                // Default product container/box
                g2.setPaint(new GradientPaint(cx + 16, cy + 12, new Color(203, 213, 225), cx + 48, cy + 52,
                        new Color(148, 163, 184)));
                g2.fillRoundRect(cx + 16, cy + 12, 32, 40, 6, 6);
                g2.setColor(new Color(100, 116, 139));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx + 16, cy + 12, 32, 40, 6, 6);

                g2.drawLine(cx + 16, cy + 12, cx + 48, cy + 52);
                g2.drawLine(cx + 48, cy + 12, cx + 16, cy + 52);
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }


}
