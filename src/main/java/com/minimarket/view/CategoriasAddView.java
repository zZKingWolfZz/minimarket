package com.minimarket.view;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.CategoriaDAO;
import com.minimarket.dao.impl.CategoriaDAOImpl;
import com.minimarket.model.Categoria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.sql.Connection;
import java.sql.SQLException;

public class CategoriasAddView extends JPanel {

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

    public CategoriasAddView() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(248, 250, 252));

        // Columnas
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

        JLabel lblEditorTitle = new JLabel("Crear Nueva Categoría");
        lblEditorTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEditorTitle.setForeground(new Color(15, 23, 42));

        JLabel lblEditorSubtitle = new JLabel("Complete los datos para crear una nueva categoría");
        lblEditorSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEditorSubtitle.setForeground(new Color(148, 163, 184));

        pnlEdTitleLabel.add(lblEditorTitle);
        pnlEdTitleLabel.add(Box.createVerticalStrut(2));
        pnlEdTitleLabel.add(lblEditorSubtitle);

        JPanel pnlEdActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlEdActionsPanel.setOpaque(false);

        btnCancelarEditor = new JButton("Cancelar") {
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

        pnlEditorForm.add(pnlEdHeader, BorderLayout.NORTH);
        pnlEditorForm.add(pnlFormContainer, BorderLayout.CENTER);

        // Columna Derecha: Estadísticas Vacías (Nueva Categoría)
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

        JLabel lblRightTotalProducts = new JLabel("0");
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

        JLabel lblRightAvgPrice = new JLabel("S/ 0.00");
        lblRightAvgPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRightAvgPrice.setForeground(new Color(15, 23, 42));

        pnlRightAvgPriceCard.add(lblAvgPriceTitle);
        pnlRightAvgPriceCard.add(Box.createVerticalStrut(2));
        pnlRightAvgPriceCard.add(lblRightAvgPrice);

        RoundedPanel pnlRightCriticalStockCard = new RoundedPanel(12);
        pnlRightCriticalStockCard.setBackground(Color.WHITE);
        pnlRightCriticalStockCard.setLayout(new BoxLayout(pnlRightCriticalStockCard, BoxLayout.Y_AXIS));
        pnlRightCriticalStockCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblCriticalStockTitle = new JLabel("Stock Crítico");
        lblCriticalStockTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCriticalStockTitle.setForeground(new Color(100, 116, 139));

        JLabel lblRightCriticalStock = new JLabel("0 Items");
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
        JLabel lblRightProductsTitle = new JLabel("Productos en la Nueva Categoría");
        lblRightProductsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRightProductsTitle.setForeground(new Color(15, 23, 42));
        pnlProductsHeader.add(lblRightProductsTitle, BorderLayout.WEST);

        JPanel pnlEditorProductsList = new JPanel();
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
        if (btnIconBox.isSelected())
            return "Box";
        if (btnIconTag.isSelected())
            return "Tag";
        if (btnIconCup.isSelected())
            return "Cup";
        if (btnIconApple.isSelected())
            return "Apple";
        if (btnIconMeat.isSelected())
            return "Meat";
        if (btnIconCookie.isSelected())
            return "Cookie";
        if (btnIconWine.isSelected())
            return "Wine";
        return "Tag";
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
            JOptionPane.showMessageDialog(this, "El nombre de la categoría no puede estar vacío.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null) {
                // Modo offline
                JOptionPane.showMessageDialog(this, "Categoría creada con éxito (Modo Demo Offline).", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                navigateBackToCategories();
                return;
            }

            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(connection);
            Categoria nueva = new Categoria(nombre);
            boolean ok = categoriaDAO.insert(nueva);
            if (ok) {
                // Sincronizar en memoria en cache
                int newId = nueva.getIdCategoria();
                if (newId > 0) {
                    CategoriasView.customSkuPrefixes.put(newId, txtSkuPrefix.getText().trim());
                    CategoriasView.customDescriptions.put(newId, txtDescripcion.getText().trim());
                    CategoriasView.customTagColors.put(newId, txtTagColor.getText().trim());
                    CategoriasView.customIcons.put(newId, getSelectedIconName());
                    CategoriasView.customPosActive.put(newId, chkActivaPOS.isSelected());
                } else {
                    // Si no cargó el auto-generado, buscar el maximo ID para registrar el cache
                    try {
                        java.sql.Statement st = connection.createStatement();
                        java.sql.ResultSet rs = st.executeQuery("SELECT MAX(Id_categoria) FROM categoria");
                        if (rs.next()) {
                            int mid = rs.getInt(1);
                            CategoriasView.customSkuPrefixes.put(mid, txtSkuPrefix.getText().trim());
                            CategoriasView.customDescriptions.put(mid, txtDescripcion.getText().trim());
                            CategoriasView.customTagColors.put(mid, txtTagColor.getText().trim());
                            CategoriasView.customIcons.put(mid, getSelectedIconName());
                            CategoriasView.customPosActive.put(mid, chkActivaPOS.isSelected());
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }

                JOptionPane.showMessageDialog(this, "Categoría creada con éxito.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                navigateBackToCategories();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear la categoría.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar la categoría: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
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
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
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
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
    }
}
