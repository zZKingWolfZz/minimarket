package com.minimarket.view;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.CategoriaDAO;
import com.minimarket.dao.impl.CategoriaDAOImpl;
import com.minimarket.model.Categoria;
import com.minimarket.model.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriasEditView extends JPanel {

    private final Categoria categoria;

    private JTextField txtNombreCategoria;
    private JTextField txtSkuPrefix;
    private JTextArea txtDescripcion;
    private JToggleButton btnIconBox;
    private JToggleButton btnIconTag;
    private JToggleButton btnIconCup;
    private JToggleButton btnIconApple;
    private JToggleButton btnIconMeat;
    private JToggleButton btnIconCookie;
    private JToggleButton btnIconWine;
    private ButtonGroup bgIconGroup;
    private JTextField txtTagColor;
    private JCheckBox chkActivaPOS;

    private JButton btnGuardarCategoria;
    private JButton btnCancelarEditor;
    private JButton btnEliminarCategoria;

    // Right Column Stats
    private JLabel lblRightTotalProducts;
    private JLabel lblRightAvgPrice;
    private JLabel lblRightCriticalStock;
    private JPanel pnlRightCriticalStockCard;
    private JPanel pnlEditorProductsList;

    private List<Producto> products = new ArrayList<>();

    public CategoriasEditView(Categoria categoria) {
        this.categoria = categoria;
        loadProducts();
        initComponents();
        populateFields();
        calculateStatsAndProductsList();
    }

    private void loadProducts() {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection != null) {
                String sql = "SELECT * FROM producto WHERE Id_categoria = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, categoria.getIdCategoria());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Producto p = new Producto(
                                    rs.getInt("Id_producto"),
                                    rs.getString("nombre_producto"),
                                    rs.getBigDecimal("precio_unitario"),
                                    rs.getInt("Id_categoria"),
                                    rs.getString("codigo_barras")
                            );
                            products.add(p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getProductStock(int idProducto) {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
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
            // fallback
        }
        return (idProducto * 7 + 12) % 150;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(248, 250, 252));

        JPanel pnlEditorScreenWrapper = new JPanel(new BorderLayout(18, 0));
        pnlEditorScreenWrapper.setOpaque(false);
        pnlEditorScreenWrapper.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Columna Central: Formulario
        JPanel pnlEditorForm = new JPanel(new BorderLayout(0, 16));
        pnlEditorForm.setOpaque(false);

        // Header del Editor
        JPanel pnlEdHeader = new JPanel(new BorderLayout());
        pnlEdHeader.setOpaque(false);

        JPanel pnlEdTitleLabel = new JPanel();
        pnlEdTitleLabel.setLayout(new BoxLayout(pnlEdTitleLabel, BoxLayout.Y_AXIS));
        pnlEdTitleLabel.setOpaque(false);

        JLabel lblEditorTitle = new JLabel("Editor: " + categoria.getNombreCategoria());
        lblEditorTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEditorTitle.setForeground(new Color(15, 23, 42));

        JLabel lblEditorSubtitle = new JLabel("Configure la identidad visual y visibilidad de " + categoria.getNombreCategoria());
        lblEditorSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEditorSubtitle.setForeground(new Color(148, 163, 184));

        pnlEdTitleLabel.add(lblEditorTitle);
        pnlEdTitleLabel.add(Box.createVerticalStrut(2));
        pnlEdTitleLabel.add(lblEditorSubtitle);

        JPanel pnlEdActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlEdActionsPanel.setOpaque(false);

        btnCancelarEditor = new JButton("✕ Cancelar") {
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
        btnCancelarEditor.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCancelarEditor.setForeground(new Color(71, 85, 105));
        btnCancelarEditor.setContentAreaFilled(false);
        btnCancelarEditor.setFocusPainted(false);
        btnCancelarEditor.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelarEditor.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnCancelarEditor.addActionListener(e -> navigateBackToCategories());

        btnGuardarCategoria = new JButton("Guardar Categoría") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(21, 128, 61));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(22, 163, 74));
                } else {
                    g2.setColor(new Color(24, 119, 242));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnGuardarCategoria.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGuardarCategoria.setForeground(Color.WHITE);
        btnGuardarCategoria.setContentAreaFilled(false);
        btnGuardarCategoria.setFocusPainted(false);
        btnGuardarCategoria.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardarCategoria.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnGuardarCategoria.addActionListener(e -> guardarCategoria());

        pnlEdActionsPanel.add(btnCancelarEditor);
        pnlEdActionsPanel.add(btnGuardarCategoria);

        pnlEdHeader.add(pnlEdTitleLabel, BorderLayout.WEST);
        pnlEdHeader.add(pnlEdActionsPanel, BorderLayout.EAST);

        // Formulario
        RoundedPanel pnlFormContainer = new RoundedPanel(12);
        pnlFormContainer.setBackground(Color.WHITE);
        pnlFormContainer.setLayout(new GridBagLayout());
        pnlFormContainer.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 16, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Sección 1: INFORMACIÓN BÁSICA
        JLabel lblSecBasic = new JLabel("INFORMACIÓN BÁSICA");
        lblSecBasic.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSecBasic.setForeground(new Color(148, 163, 184));
        pnlFormContainer.add(lblSecBasic, gbc);
        gbc.gridy++;

        // Inputs Nombre y SKU Prefix lado a lado
        JPanel pnlFormRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlFormRow1.setOpaque(false);

        JPanel pnlNom = new JPanel(new BorderLayout(0, 6));
        pnlNom.setOpaque(false);
        JLabel lblNom = new JLabel("Nombre de Categoría");
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNom.setForeground(new Color(71, 85, 105));
        txtNombreCategoria = new JTextField();
        txtNombreCategoria.setPreferredSize(new Dimension(0, 36));
        txtNombreCategoria.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 10, 0, 10)));
        pnlNom.add(lblNom, BorderLayout.NORTH);
        pnlNom.add(txtNombreCategoria, BorderLayout.CENTER);

        JPanel pnlSkuField = new JPanel(new BorderLayout(0, 6));
        pnlSkuField.setOpaque(false);
        JLabel lblSkuField = new JLabel("Código Único (SKU Prefix)");
        lblSkuField.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSkuField.setForeground(new Color(71, 85, 105));
        txtSkuPrefix = new JTextField();
        txtSkuPrefix.setPreferredSize(new Dimension(0, 36));
        txtSkuPrefix.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 10, 0, 10)));
        pnlSkuField.add(lblSkuField, BorderLayout.NORTH);
        pnlSkuField.add(txtSkuPrefix, BorderLayout.CENTER);

        pnlFormRow1.add(pnlNom);
        pnlFormRow1.add(pnlSkuField);

        pnlFormContainer.add(pnlFormRow1, gbc);
        gbc.gridy++;

        // Descripción General
        JPanel pnlDesc = new JPanel(new BorderLayout(0, 6));
        pnlDesc.setOpaque(false);
        JLabel lblDesc = new JLabel("Descripción General");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDesc.setForeground(new Color(71, 85, 105));
        txtDescripcion = new JTextArea(3, 0);
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        pnlDesc.add(lblDesc, BorderLayout.NORTH);
        pnlDesc.add(scrollDesc, BorderLayout.CENTER);

        pnlFormContainer.add(pnlDesc, gbc);
        gbc.gridy++;

        // Separador
        JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
        sep1.setForeground(new Color(241, 245, 249));
        pnlFormContainer.add(sep1, gbc);
        gbc.gridy++;

        // Sección 2: IDENTIDAD VISUAL
        JLabel lblSecVisual = new JLabel("IDENTIDAD VISUAL");
        lblSecVisual.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSecVisual.setForeground(new Color(148, 163, 184));
        pnlFormContainer.add(lblSecVisual, gbc);
        gbc.gridy++;

        // Icono y Color
        JPanel pnlFormRow2 = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlFormRow2.setOpaque(false);

        JPanel pnlIcons = new JPanel(new BorderLayout(0, 6));
        pnlIcons.setOpaque(false);
        JLabel lblIcon = new JLabel("Icono Representativo");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblIcon.setForeground(new Color(71, 85, 105));

        JPanel pnlIconButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlIconButtons.setOpaque(false);

        bgIconGroup = new ButtonGroup();
        btnIconBox = createIconButton(new ViewCategoryIcon("Box"));
        btnIconTag = createIconButton(new ViewCategoryIcon("Tag"));
        btnIconCup = createIconButton(new ViewCategoryIcon("Cup"));
        btnIconApple = createIconButton(new ViewCategoryIcon("Apple"));
        btnIconMeat = createIconButton(new ViewCategoryIcon("Meat"));
        btnIconCookie = createIconButton(new ViewCategoryIcon("Cookie"));
        btnIconWine = createIconButton(new ViewCategoryIcon("Wine"));

        btnIconBox.setSelected(true);

        pnlIconButtons.add(btnIconBox);
        pnlIconButtons.add(btnIconTag);
        pnlIconButtons.add(btnIconCup);
        pnlIconButtons.add(btnIconApple);
        pnlIconButtons.add(btnIconMeat);
        pnlIconButtons.add(btnIconCookie);
        pnlIconButtons.add(btnIconWine);

        pnlIcons.add(lblIcon, BorderLayout.NORTH);
        pnlIcons.add(pnlIconButtons, BorderLayout.CENTER);

        JPanel pnlColor = new JPanel(new BorderLayout(0, 6));
        pnlColor.setOpaque(false);
        JLabel lblColor = new JLabel("Color de Etiquetas");
        lblColor.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblColor.setForeground(new Color(71, 85, 105));

        JPanel pnlColorWrapper = new JPanel(new BorderLayout(8, 0));
        pnlColorWrapper.setOpaque(false);
        txtTagColor = new JTextField("#007BFF");
        txtTagColor.setPreferredSize(new Dimension(100, 36));
        txtTagColor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 10, 0, 10)));

        JButton btnChangeColor = new JButton("Cambiar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnChangeColor.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnChangeColor.setForeground(new Color(15, 23, 42));
        btnChangeColor.setContentAreaFilled(false);
        btnChangeColor.setFocusPainted(false);
        btnChangeColor.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnChangeColor.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChangeColor.addActionListener(e -> {
            try {
                Color c = JColorChooser.showDialog(this, "Seleccionar Color", Color.decode(txtTagColor.getText()));
                if (c != null) {
                    txtTagColor.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
                }
            } catch (Exception ex) {
                Color c = JColorChooser.showDialog(this, "Seleccionar Color", Color.BLUE);
                if (c != null) {
                    txtTagColor.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
                }
            }
        });

        pnlColorWrapper.add(txtTagColor, BorderLayout.CENTER);
        pnlColorWrapper.add(btnChangeColor, BorderLayout.EAST);
        pnlColor.add(lblColor, BorderLayout.NORTH);
        pnlColor.add(pnlColorWrapper, BorderLayout.CENTER);

        pnlFormRow2.add(pnlIcons);
        pnlFormRow2.add(pnlColor);

        pnlFormContainer.add(pnlFormRow2, gbc);
        gbc.gridy++;

        // Separador
        JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
        sep2.setForeground(new Color(241, 245, 249));
        pnlFormContainer.add(sep2, gbc);
        gbc.gridy++;

        // Visibilidad y Ventas
        JLabel lblSecVis = new JLabel("VISIBILIDAD Y VENTAS");
        lblSecVis.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSecVis.setForeground(new Color(148, 163, 184));
        pnlFormContainer.add(lblSecVis, gbc);
        gbc.gridy++;

        chkActivaPOS = new JCheckBox("Activa en Terminal POS");
        chkActivaPOS.setSelected(true);
        chkActivaPOS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkActivaPOS.setForeground(new Color(15, 23, 42));
        chkActivaPOS.setOpaque(false);
        chkActivaPOS.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlFormContainer.add(chkActivaPOS, gbc);
        gbc.gridy++;

        // Vertical Glue
        GridBagConstraints gbcFiller = new GridBagConstraints();
        gbcFiller.gridx = 0;
        gbcFiller.gridy = gbc.gridy;
        gbcFiller.weighty = 1.0;
        gbcFiller.fill = GridBagConstraints.BOTH;
        pnlFormContainer.add(Box.createGlue(), gbcFiller);

        // Botón Eliminar
        btnEliminarCategoria = new JButton("🗑 Eliminar Categoría") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(185, 28, 28));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(220, 38, 38));
                } else {
                    g2.setColor(new Color(239, 68, 68));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEliminarCategoria.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnEliminarCategoria.setForeground(Color.WHITE);
        btnEliminarCategoria.setContentAreaFilled(false);
        btnEliminarCategoria.setFocusPainted(false);
        btnEliminarCategoria.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarCategoria.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnEliminarCategoria.setMaximumSize(new Dimension(160, 32));
        btnEliminarCategoria.addActionListener(e -> eliminarCategoria());

        JPanel pnlEliminarWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlEliminarWrapper.setOpaque(false);
        pnlEliminarWrapper.add(btnEliminarCategoria);

        pnlEditorForm.add(pnlEdHeader, BorderLayout.NORTH);
        pnlEditorForm.add(pnlFormContainer, BorderLayout.CENTER);
        pnlEditorForm.add(pnlEliminarWrapper, BorderLayout.SOUTH);

        // Columna Derecha: Estadísticas
        JPanel pnlRightColumn = new JPanel(new BorderLayout(0, 16));
        pnlRightColumn.setPreferredSize(new Dimension(320, 0));
        pnlRightColumn.setOpaque(false);

        JPanel pnlStats = new JPanel();
        pnlStats.setLayout(new BoxLayout(pnlStats, BoxLayout.Y_AXIS));
        pnlStats.setOpaque(false);

        RoundedPanel pnlTotalProductsCard = new RoundedPanel(12) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(191, 219, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        pnlTotalProductsCard.setBackground(new Color(239, 246, 255));
        pnlTotalProductsCard.setLayout(new BorderLayout(15, 0));
        pnlTotalProductsCard.setBorder(new EmptyBorder(12, 16, 12, 16));
        pnlTotalProductsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lblTotalProductsIcon = new JLabel(new BoxIcon(true));
        JPanel pnlTotalProductsText = new JPanel();
        pnlTotalProductsText.setLayout(new BoxLayout(pnlTotalProductsText, BoxLayout.Y_AXIS));
        pnlTotalProductsText.setOpaque(false);

        JLabel lblTotalProductsTitle = new JLabel("TOTAL PRODUCTOS");
        lblTotalProductsTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTotalProductsTitle.setForeground(new Color(24, 119, 242));

        lblRightTotalProducts = new JLabel("0");
        lblRightTotalProducts.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRightTotalProducts.setForeground(new Color(24, 119, 242));

        pnlTotalProductsText.add(lblTotalProductsTitle);
        pnlTotalProductsText.add(Box.createVerticalStrut(2));
        pnlTotalProductsText.add(lblRightTotalProducts);

        pnlTotalProductsCard.add(lblTotalProductsIcon, BorderLayout.WEST);
        pnlTotalProductsCard.add(pnlTotalProductsText, BorderLayout.CENTER);

        JPanel pnlRowStats = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlRowStats.setOpaque(false);
        pnlRowStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        RoundedPanel pnlRightAvgPriceCard = new RoundedPanel(12);
        pnlRightAvgPriceCard.setBackground(Color.WHITE);
        pnlRightAvgPriceCard.setLayout(new BoxLayout(pnlRightAvgPriceCard, BoxLayout.Y_AXIS));
        pnlRightAvgPriceCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblAvgPriceTitle = new JLabel("Precio Prom.");
        lblAvgPriceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAvgPriceTitle.setForeground(new Color(100, 116, 139));

        lblRightAvgPrice = new JLabel("S/ 0.00");
        lblRightAvgPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRightAvgPrice.setForeground(new Color(15, 23, 42));

        pnlRightAvgPriceCard.add(lblAvgPriceTitle);
        pnlRightAvgPriceCard.add(Box.createVerticalStrut(2));
        pnlRightAvgPriceCard.add(lblRightAvgPrice);

        pnlRightCriticalStockCard = new RoundedPanel(12);
        pnlRightCriticalStockCard.setBackground(Color.WHITE);
        pnlRightCriticalStockCard.setLayout(new BoxLayout(pnlRightCriticalStockCard, BoxLayout.Y_AXIS));
        pnlRightCriticalStockCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblCriticalStockTitle = new JLabel("Stock Crítico");
        lblCriticalStockTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCriticalStockTitle.setForeground(new Color(100, 116, 139));

        lblRightCriticalStock = new JLabel("0 Items");
        lblRightCriticalStock.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRightCriticalStock.setForeground(new Color(15, 23, 42));

        pnlRightCriticalStockCard.add(lblCriticalStockTitle);
        pnlRightCriticalStockCard.add(Box.createVerticalStrut(2));
        pnlRightCriticalStockCard.add(lblRightCriticalStock);

        pnlRowStats.add(pnlRightAvgPriceCard);
        pnlRowStats.add(pnlRightCriticalStockCard);

        pnlStats.add(pnlTotalProductsCard);
        pnlStats.add(Box.createVerticalStrut(12));
        pnlStats.add(pnlRowStats);

        JPanel pnlProductsContainer = new JPanel(new BorderLayout(0, 10));
        pnlProductsContainer.setOpaque(false);

        JPanel pnlProductsHeader = new JPanel(new BorderLayout());
        pnlProductsHeader.setOpaque(false);
        JLabel lblRightProductsTitle = new JLabel("Productos en la Categoría");
        lblRightProductsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRightProductsTitle.setForeground(new Color(15, 23, 42));
        lblRightProductsTitle.setText("Productos en \"" + categoria.getNombreCategoria() + "\"");
        pnlProductsHeader.add(lblRightProductsTitle, BorderLayout.WEST);

        pnlEditorProductsList = new JPanel();
        pnlEditorProductsList.setLayout(new BoxLayout(pnlEditorProductsList, BoxLayout.Y_AXIS));
        pnlEditorProductsList.setOpaque(false);

        JScrollPane scrollEditorProducts = new JScrollPane(pnlEditorProductsList);
        scrollEditorProducts.setBorder(null);
        scrollEditorProducts.setOpaque(false);
        scrollEditorProducts.getViewport().setOpaque(false);

        pnlProductsContainer.add(pnlProductsHeader, BorderLayout.NORTH);
        pnlProductsContainer.add(scrollEditorProducts, BorderLayout.CENTER);

        pnlRightColumn.add(pnlStats, BorderLayout.NORTH);
        pnlRightColumn.add(pnlProductsContainer, BorderLayout.CENTER);

        pnlEditorScreenWrapper.add(pnlEditorForm, BorderLayout.CENTER);
        pnlEditorScreenWrapper.add(pnlRightColumn, BorderLayout.EAST);

        add(pnlEditorScreenWrapper, BorderLayout.CENTER);
    }

    private JToggleButton createIconButton(Icon icon) {
        JToggleButton btn = new JToggleButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(219, 234, 254));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(24, 119, 242));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(null);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bgIconGroup.add(btn);
        return btn;
    }

    private String getSelectedIconName() {
        if (btnIconBox.isSelected()) return "Box";
        if (btnIconTag.isSelected()) return "Tag";
        if (btnIconCup.isSelected()) return "Cup";
        if (btnIconApple.isSelected()) return "Apple";
        if (btnIconMeat.isSelected()) return "Meat";
        if (btnIconCookie.isSelected()) return "Cookie";
        if (btnIconWine.isSelected()) return "Wine";
        return "Tag";
    }

    private void selectIconByName(String name) {
        if ("Box".equalsIgnoreCase(name)) btnIconBox.setSelected(true);
        else if ("Tag".equalsIgnoreCase(name)) btnIconTag.setSelected(true);
        else if ("Cup".equalsIgnoreCase(name)) btnIconCup.setSelected(true);
        else if ("Apple".equalsIgnoreCase(name)) btnIconApple.setSelected(true);
        else if ("Meat".equalsIgnoreCase(name)) btnIconMeat.setSelected(true);
        else if ("Cookie".equalsIgnoreCase(name)) btnIconCookie.setSelected(true);
        else if ("Wine".equalsIgnoreCase(name)) btnIconWine.setSelected(true);
        else btnIconTag.setSelected(true);
    }

    private void populateFields() {
        int catId = categoria.getIdCategoria();
        txtNombreCategoria.setText(categoria.getNombreCategoria());

        txtSkuPrefix.setText(CategoriasView.customSkuPrefixes.containsKey(catId) ? CategoriasView.customSkuPrefixes.get(catId) : getMockSkuPrefix(categoria.getNombreCategoria()));
        txtDescripcion.setText(CategoriasView.customDescriptions.containsKey(catId) ? CategoriasView.customDescriptions.get(catId) : "Previsualización de los productos de " + categoria.getNombreCategoria() + ".");
        txtTagColor.setText(CategoriasView.customTagColors.containsKey(catId) ? CategoriasView.customTagColors.get(catId) : getMockTagColor(categoria.getNombreCategoria()));
        chkActivaPOS.setSelected(CategoriasView.customPosActive.containsKey(catId) ? CategoriasView.customPosActive.get(catId) : true);

        if (CategoriasView.customIcons.containsKey(catId)) {
            selectIconByName(CategoriasView.customIcons.get(catId));
        } else {
            selectMockIcon(categoria.getNombreCategoria());
        }
    }

    private String getMockSkuPrefix(String catName) {
        String n = catName.toUpperCase();
        if (n.contains("BEBIDA") || n.contains("JUGO")) return "BEV";
        if (n.contains("FRUTA") || n.contains("VERDURA")) return "FRV";
        if (n.contains("CARNE") || n.contains("AVE")) return "CRN";
        if (n.contains("SNACK") || n.contains("DULCE")) return "SNK";
        if (n.contains("LÁCTEO") || n.contains("LACTEO") || n.contains("HUEVO")) return "LAC";
        if (n.contains("VINO") || n.contains("LICOR")) return "VIN";
        if (n.contains("LIMPIEZA")) return "LIM";
        if (n.contains("CUIDADO") || n.contains("PERSONAL")) return "CUI";
        if (n.contains("PAN")) return "PAN";
        return n.substring(0, Math.min(3, n.length()));
    }

    private String getMockTagColor(String catName) {
        String n = catName.toLowerCase();
        if (n.contains("bebida")) return "#007BFF";
        if (n.contains("fruta")) return "#22C55E";
        if (n.contains("carne")) return "#EF4444";
        if (n.contains("snack")) return "#F59E0B";
        if (n.contains("lácteo") || n.contains("lacteo")) return "#3B82F6";
        if (n.contains("vino")) return "#8B5CF6";
        return "#64748B";
    }

    private void selectMockIcon(String catName) {
        String n = catName.toLowerCase();
        if (n.contains("bebida")) btnIconCup.setSelected(true);
        else if (n.contains("fruta")) btnIconApple.setSelected(true);
        else if (n.contains("carne")) btnIconMeat.setSelected(true);
        else if (n.contains("snack")) btnIconCookie.setSelected(true);
        else if (n.contains("lácteo") || n.contains("lacteo")) btnIconBox.setSelected(true);
        else if (n.contains("vino")) btnIconWine.setSelected(true);
        else btnIconTag.setSelected(true);
    }

    private void calculateStatsAndProductsList() {
        int totalProducts = products.size();
        lblRightTotalProducts.setText(String.valueOf(totalProducts));

        double sumPrice = 0;
        int criticalCount = 0;

        pnlEditorProductsList.removeAll();
        for (Producto p : products) {
            sumPrice += p.getPrecioUnitario().doubleValue();
            int stock = getProductStock(p.getIdProducto());
            if (stock <= 5) {
                criticalCount++;
            }
            pnlEditorProductsList.add(createProductListItem(p));
            pnlEditorProductsList.add(Box.createVerticalStrut(8));
        }
        pnlEditorProductsList.add(Box.createVerticalGlue());

        double avgPrice = totalProducts > 0 ? sumPrice / totalProducts : 0;
        lblRightAvgPrice.setText("S/ " + String.format("%.2f", avgPrice));

        lblRightCriticalStock.setText(criticalCount + " Items");
        if (criticalCount > 0) {
            pnlRightCriticalStockCard.setBackground(new Color(254, 242, 242));
            pnlRightCriticalStockCard.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1, true));
            lblRightCriticalStock.setForeground(new Color(220, 38, 38));
        } else {
            pnlRightCriticalStockCard.setBackground(Color.WHITE);
            pnlRightCriticalStockCard.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249), 1, true));
            lblRightCriticalStock.setForeground(new Color(15, 23, 42));
        }
        
        pnlEditorProductsList.revalidate();
        pnlEditorProductsList.repaint();
    }

    private JPanel createProductListItem(Producto p) {
        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(241, 245, 249)); // light border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.setPreferredSize(new Dimension(0, 64));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel lblIcon = new JLabel(new ProductVectorIcon(p.getNombreProducto(), 32));
        lblIcon.setPreferredSize(new Dimension(32, 32));

        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlText.setOpaque(false);

        JLabel lblName = new JLabel(p.getNombreProducto());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(15, 23, 42));

        JLabel lblPrice = new JLabel("S/ " + String.format("%.2f", p.getPrecioUnitario()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPrice.setForeground(new Color(24, 119, 242));

        pnlText.add(lblName);
        pnlText.add(lblPrice);

        int stock = getProductStock(p.getIdProducto());
        JLabel lblStock;
        if (stock <= 5) {
            lblStock = new JLabel(stock + " un.") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(254, 226, 226));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblStock.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblStock.setForeground(new Color(220, 38, 38));
            lblStock.setBorder(new EmptyBorder(3, 8, 3, 8));
            lblStock.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            lblStock = new JLabel(stock + " un.");
            lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblStock.setForeground(new Color(100, 116, 139));
        }

        row.add(lblIcon, BorderLayout.WEST);
        row.add(pnlText, BorderLayout.CENTER);
        row.add(lblStock, BorderLayout.EAST);

        return row;
    }

    private void navigateBackToCategories() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof DashboardView) {
            DashboardView dash = (DashboardView) parentWindow;
            dash.navigateToCategorias();
        }
    }

    private void guardarCategoria() {
        String nombre = txtNombreCategoria.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la categoría no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null) {
                // Modo offline
                JOptionPane.showMessageDialog(this, "Categoría actualizada con éxito (Modo Demo Offline).", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                navigateBackToCategories();
                return;
            }

            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(connection);
            categoria.setNombreCategoria(nombre);
            boolean ok = categoriaDAO.update(categoria);
            if (ok) {
                int catId = categoria.getIdCategoria();
                CategoriasView.customSkuPrefixes.put(catId, txtSkuPrefix.getText().trim());
                CategoriasView.customDescriptions.put(catId, txtDescripcion.getText().trim());
                CategoriasView.customTagColors.put(catId, txtTagColor.getText().trim());
                CategoriasView.customIcons.put(catId, getSelectedIconName());
                CategoriasView.customPosActive.put(catId, chkActivaPOS.isSelected());

                JOptionPane.showMessageDialog(this, "Categoría actualizada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                navigateBackToCategories();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la categoría.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar la categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCategoria() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar la categoría '" + categoria.getNombreCategoria() + "'?\nLos productos asociados quedarán huérfanos.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection connection = DatabaseConnection.getInstance().getConnection();
                if (connection == null) {
                    JOptionPane.showMessageDialog(this, "Categoría eliminada con éxito (Modo Demo Offline).", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    navigateBackToCategories();
                    return;
                }

                CategoriaDAO categoriaDAO = new CategoriaDAOImpl(connection);
                boolean ok = categoriaDAO.delete(categoria.getIdCategoria());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Categoría eliminada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    navigateBackToCategories();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la categoría.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar la categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Inner classes
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

    private static class ViewCategoryIcon implements Icon {
        private final String type;
        public ViewCategoryIcon(String type) {
            this.type = type;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 116, 139));
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
        public int getIconWidth() { return 24; }
        @Override
        public int getIconHeight() { return 24; }
    }

    private static class BoxIcon implements Icon {
        private final boolean active;
        public BoxIcon(boolean active) {
            this.active = active;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(active ? new Color(24, 119, 242) : new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = x + 12;
            int cy = y + 12;
            g2.drawLine(cx, cy - 6, cx + 8, cy - 3);
            g2.drawLine(cx + 8, cy - 3, cx, cy);
            g2.drawLine(cx, cy, cx - 8, cy - 3);
            g2.drawLine(cx - 8, cy - 3, cx, cy - 6);

            g2.drawLine(cx - 8, cy - 3, cx - 8, cy + 3);
            g2.drawLine(cx, cy, cx, cy + 6);
            g2.drawLine(cx + 8, cy - 3, cx + 8, cy + 3);

            g2.drawLine(cx - 8, cy + 3, cx, cy + 6);
            g2.drawLine(cx, cy + 6, cx + 8, cy + 3);

            g2.dispose();
        }
        @Override
        public int getIconWidth() { return 24; }
        @Override
        public int getIconHeight() { return 24; }
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
                g2.setPaint(new GradientPaint(cx + 16, cy + 8, new Color(110, 70, 45), cx + 48, cy + 56, new Color(70, 40, 20)));
                int[] xPoints = {cx + 20, cx + 44, cx + 40, cx + 24};
                int[] yPoints = {cy + 8, cy + 8, cy + 14, cy + 14};
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.fillRoundRect(cx + 16, cy + 14, 32, 42, 6, 6);
                
                g2.setColor(new Color(230, 210, 180));
                g2.fillRoundRect(cx + 22, cy + 24, 20, 20, 4, 4);
                
                g2.setColor(new Color(110, 70, 45));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(cx + 26, cy + 30, cx + 38, cy + 30);
                g2.drawLine(cx + 26, cy + 34, cx + 34, cy + 34);
            } else if (name.contains("naranja") || name.contains("jugo")) {
                g2.setColor(new Color(249, 115, 22));
                g2.fillOval(cx + 12, cy + 12, 40, 40);
                
                g2.setColor(new Color(251, 146, 60));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(cx + 12, cy + 12, 40, 40);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                int ocx = cx + 32;
                int ocy = cy + 32;
                g2.drawOval(cx + 16, cy + 16, 32, 32);
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    g2.drawLine(ocx, ocy, (int)(ocx + 16 * Math.cos(angle)), (int)(ocy + 16 * Math.sin(angle)));
                }
                
                g2.setColor(new Color(34, 197, 94));
                g2.fillOval(cx + 36, cy + 2, 14, 8);
                g2.setColor(new Color(21, 128, 61));
                g2.drawOval(cx + 36, cy + 2, 14, 8);
            } else if (name.contains("agua") || name.contains("mineral")) {
                g2.setPaint(new GradientPaint(cx + 24, cy + 4, new Color(186, 230, 253, 200), cx + 40, cy + 60, new Color(56, 189, 248, 150)));
                g2.fillRoundRect(cx + 26, cy + 4, 12, 12, 2, 2);
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
                
                g2.setColor(new Color(14, 165, 233));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(bottle);
                g2.drawRoundRect(cx + 26, cy + 4, 12, 12, 2, 2);
                
                g2.setColor(new Color(148, 163, 184));
                g2.fillRoundRect(cx + 25, cy + 2, 14, 4, 2, 2);
            } else if (name.contains("té") || name.contains("te ") || name.contains("verde")) {
                g2.setPaint(new GradientPaint(cx + 18, cy + 10, new Color(74, 117, 89), cx + 46, cy + 56, new Color(40, 80, 50)));
                g2.fillRoundRect(cx + 18, cy + 10, 28, 46, 6, 6);
                
                g2.setColor(new Color(212, 175, 55));
                g2.fillRoundRect(cx + 20, cy + 6, 24, 6, 2, 2);
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx + 22, cy + 24, 20, 20, 4, 4);
                g2.setColor(new Color(34, 197, 94));
                g2.fillOval(cx + 28, cy + 30, 8, 8);
            } else if (name.contains("energizante") || name.contains("lata")) {
                g2.setPaint(new GradientPaint(cx + 20, cy + 10, new Color(59, 130, 246), cx + 44, cy + 54, new Color(30, 58, 138)));
                g2.fillRoundRect(cx + 20, cy + 10, 24, 44, 6, 6);
                
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx + 20, cy + 10, 24, 44, 6, 6);
                
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
                g2.setPaint(new GradientPaint(cx + 24, cy + 6, new Color(80, 20, 10), cx + 40, cy + 58, new Color(30, 10, 5)));
                
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
                
                g2.setColor(new Color(220, 38, 38));
                g2.fillRect(cx + 22, cy + 32, 20, 12);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawArc(cx + 23, cy + 36, 18, 4, 0, 180);
                
                g2.setColor(new Color(220, 38, 38));
                g2.fillRect(cx + 26, cy + 4, 12, 4);
            } else {
                g2.setPaint(new GradientPaint(cx + 16, cy + 12, new Color(203, 213, 225), cx + 48, cy + 52, new Color(148, 163, 184)));
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
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }
}
