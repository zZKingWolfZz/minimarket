package com.minimarket.view;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.StockDAO;
import com.minimarket.dao.impl.ProductoDAOImpl;
import com.minimarket.dao.impl.StockDAOImpl;
import com.minimarket.model.Producto;
import com.minimarket.model.Stock;
import com.minimarket.model.Categoria;
import com.minimarket.dao.CategoriaDAO;
import com.minimarket.dao.impl.CategoriaDAOImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventarioEditView extends JPanel {

    // Target product
    private final Producto producto;

    // Form fields
    private JTextField txtIdSistema;
    private JTextField txtSku;
    private JTextField txtNombre;
    private JComboBox<Categoria> cbCategorias;
    private JTextField txtPrecioVenta;
    private JTextField txtStockInicial;
    // Real-time Preview components
    private JLabel lblPreviewName;
    private JLabel lblPreviewPrice;
    private JLabel lblPreviewSku;
    private JLabel lblPreviewStock;
    private JProgressBar previewProgressBar;

    // Validation checklist components
    private ValidationCheckItem checkBasicInfo;
    private ValidationCheckItem checkSkuFormat;

    // Buttons
    private JButton btnCancelar;
    private JButton btnGuardar;

    public InventarioEditView(Producto producto) {
        this.producto = producto;

        setPreferredSize(new Dimension(1141, 880));
        initComponents();
        populateFields();
        setupRealTimeListeners();
        validateForm(); // Run initial validation
        updatePreview();
    }

    private void populateFields() {
        if (producto == null)
            return;

        txtIdSistema.setText("PRD-" + producto.getIdProducto());
        txtSku.setText(producto.getCodigoBarras());
        txtNombre.setText(producto.getNombreProducto());
        txtPrecioVenta.setText(producto.getPrecioUnitario().toPlainString());

        // Select category
        int idCat = producto.getIdCategoria();
        for (int i = 0; i < cbCategorias.getItemCount(); i++) {
            Categoria cat = cbCategorias.getItemAt(i);
            if (cat != null && cat.getIdCategoria() == idCat) {
                cbCategorias.setSelectedIndex(i);
                break;
            }
        }

        // Fetch stock quantity from local DB
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection != null) {
                String sqlStock = "SELECT Cantidad FROM stock WHERE Id_Producto = ?";
                try (PreparedStatement ps = connection.prepareStatement(sqlStock)) {
                    ps.setInt(1, producto.getIdProducto());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            txtStockInicial.setText(String.valueOf(rs.getInt("Cantidad")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(248, 250, 252)); // Slate 50 background
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // --- 1. PANEL CABECERA (Header Panel) ---
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel lblBreadcrumbs = new JLabel("Gestión de Stock  >  Editar Producto");
        lblBreadcrumbs.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBreadcrumbs.setForeground(new Color(148, 163, 184)); // Slate 400

        JLabel lblTitle = new JLabel("Editar Ficha de Producto");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42)); // Slate 900

        JLabel lblSubtitle = new JLabel(
                "Modifique la ficha técnica del producto seleccionado en el inventario global.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139)); // Slate 500

        headerLeft.add(lblBreadcrumbs);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(lblSubtitle);

        // Header Action Buttons
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        btnCancelar = new OutlineButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(85, 32));

        btnGuardar = new AccentButton("Guardar Cambios", new SaveIcon());
        btnGuardar.setPreferredSize(new Dimension(160, 32));

        headerRight.add(btnCancelar);
        headerRight.add(btnGuardar);

        headerWrapper.add(headerLeft, BorderLayout.WEST);
        headerWrapper.add(headerRight, BorderLayout.EAST);

        add(headerWrapper, BorderLayout.NORTH);

        // --- 2. CORE split LAYOUT (Center Panels) ---
        JPanel mainSplit = new JPanel(new GridBagLayout());
        mainSplit.setOpaque(false);

        // --- LEFT COLUMN: Form Cards Scroll Pane ---
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setOpaque(false);

        // Card 1: Ficha de Información / Información Básica
        RoundedPanel cardBasic = new RoundedPanel(16);
        cardBasic.setBackground(Color.WHITE);
        cardBasic.setLayout(new GridBagLayout());
        cardBasic.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Card section title
        JLabel lblSection1 = new JLabel("Ficha de Información");
        lblSection1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSection1.setForeground(new Color(15, 23, 42));
        GridBagConstraints gbc_lblSection1 = new GridBagConstraints();
        gbc_lblSection1.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblSection1.insets = new Insets(6, 6, 6, 6);
        gbc_lblSection1.weightx = 1.0;
        gbc_lblSection1.gridx = 0;
        gbc_lblSection1.gridy = 0;
        gbc_lblSection1.gridwidth = 2;
        cardBasic.add(lblSection1, gbc_lblSection1);

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(new Color(241, 245, 249));
        GridBagConstraints gbc_sep1 = new GridBagConstraints();
        gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
        gbc_sep1.insets = new Insets(6, 6, 6, 6);
        gbc_sep1.weightx = 1.0;
        gbc_sep1.gridx = 0;
        gbc_sep1.gridy = 1;
        gbc_sep1.gridwidth = 2;
        cardBasic.add(sep1, gbc_sep1);

        JLabel lblBasicHeader = new JLabel("■ INFORMACIÓN BÁSICA");
        lblBasicHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBasicHeader.setForeground(new Color(24, 119, 242));
        GridBagConstraints gbc_lblBasicHeader = new GridBagConstraints();
        gbc_lblBasicHeader.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblBasicHeader.insets = new Insets(6, 6, 6, 6);
        gbc_lblBasicHeader.weightx = 1.0;
        gbc_lblBasicHeader.gridx = 0;
        gbc_lblBasicHeader.gridy = 2;
        gbc_lblBasicHeader.gridwidth = 2;
        cardBasic.add(lblBasicHeader, gbc_lblBasicHeader);

        // Row 1: ID de Sistema & SKU
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);

        JPanel fieldIdPanel = new JPanel(new BorderLayout(0, 4));
        fieldIdPanel.setOpaque(false);
        JLabel lblIdSystem = new JLabel("ID DE SISTEMA");
        lblIdSystem.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblIdSystem.setForeground(new Color(100, 116, 139));
        txtIdSistema = new JTextField("");
        txtIdSistema.setPreferredSize(new Dimension(0, 32));
        txtIdSistema.setBackground(new Color(241, 245, 249)); // light grey read-only
        txtIdSistema.setEditable(false);
        txtIdSistema.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtIdSistema.setForeground(new Color(100, 116, 139));
        txtIdSistema.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        fieldIdPanel.add(lblIdSystem, BorderLayout.NORTH);
        fieldIdPanel.add(txtIdSistema, BorderLayout.CENTER);

        JPanel fieldSkuPanel = new JPanel(new BorderLayout(0, 4));
        fieldSkuPanel.setOpaque(false);
        JLabel lblSku = new JLabel("SKU / CÓDIGO DE BARRAS *");
        lblSku.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSku.setForeground(new Color(100, 116, 139));
        txtSku = new JTextField("");
        txtSku.setPreferredSize(new Dimension(0, 32));
        txtSku.setBackground(Color.WHITE);
        txtSku.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSku.setForeground(new Color(15, 23, 42));
        txtSku.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        fieldSkuPanel.add(lblSku, BorderLayout.NORTH);
        fieldSkuPanel.add(txtSku, BorderLayout.CENTER);

        row1.add(fieldIdPanel);
        row1.add(fieldSkuPanel);
        GridBagConstraints gbc_row1 = new GridBagConstraints();
        gbc_row1.fill = GridBagConstraints.HORIZONTAL;
        gbc_row1.insets = new Insets(6, 6, 6, 6);
        gbc_row1.weightx = 1.0;
        gbc_row1.gridx = 0;
        gbc_row1.gridy = 3;
        gbc_row1.gridwidth = 2;
        cardBasic.add(row1, gbc_row1);

        // Row 2: Nombre del Producto
        JPanel fieldNombrePanel = new JPanel(new BorderLayout(0, 4));
        fieldNombrePanel.setOpaque(false);
        JLabel lblNombre = new JLabel("NOMBRE DEL PRODUCTO *");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblNombre.setForeground(new Color(100, 116, 139));
        txtNombre = new JTextField("");
        txtNombre.setPreferredSize(new Dimension(0, 32));
        txtNombre.setBackground(Color.WHITE);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNombre.setForeground(new Color(15, 23, 42));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        fieldNombrePanel.add(lblNombre, BorderLayout.NORTH);
        fieldNombrePanel.add(txtNombre, BorderLayout.CENTER);

        GridBagConstraints gbc_fieldNombrePanel = new GridBagConstraints();
        gbc_fieldNombrePanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_fieldNombrePanel.insets = new Insets(6, 6, 6, 6);
        gbc_fieldNombrePanel.weightx = 1.0;
        gbc_fieldNombrePanel.gridx = 0;
        gbc_fieldNombrePanel.gridy = 4;
        gbc_fieldNombrePanel.gridwidth = 2;
        cardBasic.add(fieldNombrePanel, gbc_fieldNombrePanel);

        // Row 3: Categoría
        JPanel row3 = new JPanel(new GridLayout(1, 2, 16, 0));
        row3.setOpaque(false);

        JPanel fieldCatPanel = new JPanel(new BorderLayout(0, 4));
        fieldCatPanel.setOpaque(false);
        JLabel lblCat = new JLabel("CATEGORÍA");
        lblCat.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblCat.setForeground(new Color(100, 116, 139));
        java.util.List<Categoria> listCategorias = new java.util.ArrayList<>();
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection != null) {
                CategoriaDAO catDAO = new CategoriaDAOImpl(connection);
                listCategorias = catDAO.findAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (listCategorias.isEmpty()) {
            listCategorias.add(new Categoria(1, "Abarrotes"));
            listCategorias.add(new Categoria(2, "Bebidas"));
            listCategorias.add(new Categoria(3, "Lácteos"));
            listCategorias.add(new Categoria(4, "Limpieza"));
            listCategorias.add(new Categoria(5, "Cuidado Personal"));
            listCategorias.add(new Categoria(6, "Snacks y Golosinas"));
            listCategorias.add(new Categoria(7, "Panadería"));
        }
        cbCategorias = new JComboBox<>(listCategorias.toArray(new Categoria[0]));
        styleComboBox(cbCategorias);
        fieldCatPanel.add(lblCat, BorderLayout.NORTH);
        fieldCatPanel.add(cbCategorias, BorderLayout.CENTER);

        row3.add(fieldCatPanel);
        GridBagConstraints gbc_row3 = new GridBagConstraints();
        gbc_row3.fill = GridBagConstraints.HORIZONTAL;
        gbc_row3.insets = new Insets(6, 6, 6, 6);
        gbc_row3.weightx = 1.0;
        gbc_row3.gridx = 0;
        gbc_row3.gridy = 5;
        gbc_row3.gridwidth = 2;
        cardBasic.add(row3, gbc_row3);

        // Card 2: Costos e Inventario
        RoundedPanel cardCosts = new RoundedPanel(16);
        cardCosts.setBackground(Color.WHITE);
        cardCosts.setLayout(new GridBagLayout());
        cardCosts.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblCostsHeader = new JLabel("■ COSTOS E INVENTARIO");
        lblCostsHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCostsHeader.setForeground(new Color(24, 119, 242));
        GridBagConstraints gbc_lblCostsHeader = new GridBagConstraints();
        gbc_lblCostsHeader.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblCostsHeader.insets = new Insets(6, 6, 6, 6);
        gbc_lblCostsHeader.weightx = 1.0;
        gbc_lblCostsHeader.gridx = 0;
        gbc_lblCostsHeader.gridy = 0;
        gbc_lblCostsHeader.gridwidth = 2;
        cardCosts.add(lblCostsHeader, gbc_lblCostsHeader);

        // Row 4: Precio Venta (Costo removido)
        JPanel row4 = new JPanel(new GridLayout(1, 1, 0, 0));
        row4.setOpaque(false);

        JPanel fieldVentaPanel = new JPanel(new BorderLayout(0, 4));
        fieldVentaPanel.setOpaque(false);
        JLabel lblVenta = new JLabel("PRECIO DE VENTA (S/) *");
        lblVenta.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblVenta.setForeground(new Color(100, 116, 139));
        txtPrecioVenta = new JTextField("");
        txtPrecioVenta.setPreferredSize(new Dimension(0, 32));
        txtPrecioVenta.setBackground(Color.WHITE);
        txtPrecioVenta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPrecioVenta.setForeground(new Color(15, 23, 42));
        txtPrecioVenta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(24, 119, 242), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        fieldVentaPanel.add(lblVenta, BorderLayout.NORTH);
        fieldVentaPanel.add(txtPrecioVenta, BorderLayout.CENTER);

        row4.add(fieldVentaPanel);
        GridBagConstraints gbc_row4 = new GridBagConstraints();
        gbc_row4.fill = GridBagConstraints.HORIZONTAL;
        gbc_row4.insets = new Insets(6, 6, 6, 6);
        gbc_row4.weightx = 1.0;
        gbc_row4.gridx = 0;
        gbc_row4.gridy = 1;
        gbc_row4.gridwidth = 2;
        cardCosts.add(row4, gbc_row4);

        // Row 5: Stock Actual (Nivel crítico de stock siempre es 5 de forma automática)
        JPanel row5 = new JPanel(new GridLayout(1, 1, 0, 0));
        row5.setOpaque(false);

        JPanel fieldStockPanel = new JPanel(new BorderLayout(0, 4));
        fieldStockPanel.setOpaque(false);
        JLabel lblStock = new JLabel("STOCK ACTUAL *");
        lblStock.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStock.setForeground(new Color(100, 116, 139));
        txtStockInicial = new JTextField("");
        txtStockInicial.setPreferredSize(new Dimension(0, 32));
        txtStockInicial.setBackground(Color.WHITE);
        txtStockInicial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtStockInicial.setForeground(new Color(15, 23, 42));
        txtStockInicial.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        fieldStockPanel.add(lblStock, BorderLayout.NORTH);
        fieldStockPanel.add(txtStockInicial, BorderLayout.CENTER);

        row5.add(fieldStockPanel);
        GridBagConstraints gbc_row5 = new GridBagConstraints();
        gbc_row5.fill = GridBagConstraints.HORIZONTAL;
        gbc_row5.insets = new Insets(6, 6, 6, 6);
        gbc_row5.weightx = 1.0;
        gbc_row5.gridx = 0;
        gbc_row5.gridy = 2;
        gbc_row5.gridwidth = 2;
        cardCosts.add(row5, gbc_row5);

        leftContainer.add(cardBasic);
        leftContainer.add(Box.createVerticalStrut(15));
        leftContainer.add(cardCosts);
        leftContainer.add(Box.createVerticalStrut(15));

        JScrollPane formScroll = new JScrollPane(leftContainer);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);

        GridBagConstraints gbc_formScroll = new GridBagConstraints();
        gbc_formScroll.fill = GridBagConstraints.BOTH;
        gbc_formScroll.weighty = 1.0;
        gbc_formScroll.gridx = 0;
        gbc_formScroll.gridy = 0;
        gbc_formScroll.weightx = 0.65;
        gbc_formScroll.insets = new Insets(0, 0, 0, 12);
        mainSplit.add(formScroll, gbc_formScroll);

        // --- RIGHT COLUMN: Real-Time Preview & Validation ---
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setOpaque(false);

        // 1. Vista Previa Catálogo Card
        RoundedPanel cardPreview = new RoundedPanel(16);
        cardPreview.setBackground(Color.WHITE);
        cardPreview.setLayout(new BorderLayout(0, 12));
        cardPreview.setBorder(new EmptyBorder(16, 16, 16, 16));
        cardPreview.setMaximumSize(new Dimension(32767, 300));
        cardPreview.setPreferredSize(new Dimension(0, 300));

        JLabel lblPreviewTitleHeader = new JLabel("VISTA PREVIA CATÁLOGO");
        lblPreviewTitleHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPreviewTitleHeader.setForeground(new Color(24, 119, 242));
        cardPreview.add(lblPreviewTitleHeader, BorderLayout.NORTH);

        // Catalog Card box
        RoundedPanel itemPreviewBox = new RoundedPanel(12);
        itemPreviewBox.setBackground(new Color(248, 250, 252));
        itemPreviewBox.setLayout(new GridBagLayout());
        itemPreviewBox.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Card Labels full width
        lblPreviewSku = new JLabel("SKU: 7701234567890");
        lblPreviewSku.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPreviewSku.setForeground(new Color(148, 163, 184)); // Slate 400
        GridBagConstraints gbc_lblPreviewSku = new GridBagConstraints();
        gbc_lblPreviewSku.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblPreviewSku.gridx = 0;
        gbc_lblPreviewSku.gridy = 0;
        gbc_lblPreviewSku.gridheight = 1;
        gbc_lblPreviewSku.weightx = 1.0;
        gbc_lblPreviewSku.insets = new Insets(0, 0, 4, 0);
        itemPreviewBox.add(lblPreviewSku, gbc_lblPreviewSku);

        lblPreviewName = new JLabel("Café Premium Molido - 500g");
        lblPreviewName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPreviewName.setForeground(new Color(15, 23, 42)); // Slate 900
        GridBagConstraints gbc_lblPreviewName = new GridBagConstraints();
        gbc_lblPreviewName.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblPreviewName.gridx = 0;
        gbc_lblPreviewName.gridy = 1;
        gbc_lblPreviewName.gridheight = 1;
        gbc_lblPreviewName.weightx = 1.0;
        gbc_lblPreviewName.insets = new Insets(0, 0, 4, 0);
        itemPreviewBox.add(lblPreviewName, gbc_lblPreviewName);

        lblPreviewPrice = new JLabel("S/12.50 / und");
        lblPreviewPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPreviewPrice.setForeground(new Color(24, 119, 242)); // Accent Blue
        GridBagConstraints gbc_lblPreviewPrice = new GridBagConstraints();
        gbc_lblPreviewPrice.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblPreviewPrice.gridx = 0;
        gbc_lblPreviewPrice.gridy = 2;
        gbc_lblPreviewPrice.gridheight = 1;
        gbc_lblPreviewPrice.weightx = 1.0;
        gbc_lblPreviewPrice.insets = new Insets(0, 0, 4, 0);
        itemPreviewBox.add(lblPreviewPrice, gbc_lblPreviewPrice);

        lblPreviewStock = new JLabel("STOCK ACTUAL: 50 unidades");
        lblPreviewStock.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPreviewStock.setForeground(new Color(71, 85, 105)); // Slate 600
        GridBagConstraints gbc_lblPreviewStock = new GridBagConstraints();
        gbc_lblPreviewStock.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblPreviewStock.gridx = 0;
        gbc_lblPreviewStock.gridy = 3;
        gbc_lblPreviewStock.gridheight = 1;
        gbc_lblPreviewStock.weightx = 1.0;
        gbc_lblPreviewStock.insets = new Insets(0, 0, 4, 0);
        itemPreviewBox.add(lblPreviewStock, gbc_lblPreviewStock);

        // Circular progress bar inside card
        previewProgressBar = new JProgressBar(0, 100);
        previewProgressBar.setValue(50);
        previewProgressBar.setPreferredSize(new Dimension(0, 6));
        previewProgressBar.setForeground(new Color(24, 119, 242));
        previewProgressBar.setBackground(new Color(226, 232, 240));
        previewProgressBar.setBorder(null);
        GridBagConstraints gbc_previewProgressBar = new GridBagConstraints();
        gbc_previewProgressBar.fill = GridBagConstraints.HORIZONTAL;
        gbc_previewProgressBar.gridx = 0;
        gbc_previewProgressBar.gridy = 4;
        gbc_previewProgressBar.gridwidth = 1;
        gbc_previewProgressBar.weightx = 1.0;
        gbc_previewProgressBar.insets = new Insets(8, 0, 4, 0);
        itemPreviewBox.add(previewProgressBar, gbc_previewProgressBar);

        cardPreview.add(itemPreviewBox, BorderLayout.CENTER);

        // Preview Footer Alert Note
        JPanel alertFootPanel = new RoundedBlueAlertPanel();

        JLabel lblInfoIcon = new JLabel(new InfoAlertIcon());
        JLabel lblInfoText = new JLabel(
                "<html>Esta es una representación fiel de cómo aparecerá el producto en la terminal de ventas para los cajeros.</html>");
        lblInfoText.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfoText.setForeground(new Color(3, 105, 161)); // Sky 700
        alertFootPanel.add(lblInfoIcon, BorderLayout.WEST);
        alertFootPanel.add(lblInfoText, BorderLayout.CENTER);

        cardPreview.add(alertFootPanel, BorderLayout.SOUTH);

        // 2. Validación de Formulario Card
        RoundedPanel cardValidation = new RoundedPanel(16);
        cardValidation.setBackground(Color.WHITE);
        cardValidation.setLayout(new GridBagLayout());
        cardValidation.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblValHeader = new JLabel("VALIDACIÓN DEL FORMULARIO");
        lblValHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblValHeader.setForeground(new Color(100, 116, 139));
        GridBagConstraints gbc_lblValHeader = new GridBagConstraints();
        gbc_lblValHeader.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblValHeader.weightx = 1.0;
        gbc_lblValHeader.gridx = 0;
        gbc_lblValHeader.gridy = 0;
        gbc_lblValHeader.insets = new Insets(0, 0, 12, 0);
        cardValidation.add(lblValHeader, gbc_lblValHeader);

        checkBasicInfo = new ValidationCheckItem("Información básica completa");
        GridBagConstraints gbc_checkBasicInfo = new GridBagConstraints();
        gbc_checkBasicInfo.fill = GridBagConstraints.HORIZONTAL;
        gbc_checkBasicInfo.weightx = 1.0;
        gbc_checkBasicInfo.gridx = 0;
        gbc_checkBasicInfo.gridy = 1;
        gbc_checkBasicInfo.insets = new Insets(4, 0, 4, 0);
        cardValidation.add(checkBasicInfo, gbc_checkBasicInfo);

        checkSkuFormat = new ValidationCheckItem("Código SKU / Barras válido");
        GridBagConstraints gbc_checkSkuFormat = new GridBagConstraints();
        gbc_checkSkuFormat.fill = GridBagConstraints.HORIZONTAL;
        gbc_checkSkuFormat.weightx = 1.0;
        gbc_checkSkuFormat.gridx = 0;
        gbc_checkSkuFormat.gridy = 2;
        gbc_checkSkuFormat.insets = new Insets(4, 0, 4, 0);
        cardValidation.add(checkSkuFormat, gbc_checkSkuFormat);

        rightContainer.add(cardPreview);
        rightContainer.add(Box.createVerticalStrut(15));
        rightContainer.add(cardValidation);

        GridBagConstraints gbc_rightContainer = new GridBagConstraints();
        gbc_rightContainer.fill = GridBagConstraints.BOTH;
        gbc_rightContainer.weighty = 1.0;
        gbc_rightContainer.gridx = 1;
        gbc_rightContainer.gridy = 0;
        gbc_rightContainer.weightx = 0.35;
        gbc_rightContainer.insets = new Insets(0, 12, 0, 0);
        mainSplit.add(rightContainer, gbc_rightContainer);

        add(mainSplit, BorderLayout.CENTER);

        // Configure Save/Cancel Action Listeners
        btnCancelar.addActionListener(e -> navigateBackToCatalog());

        btnGuardar.addActionListener(e -> {
            if (saveProductToDatabase()) {
                navigateBackToCatalog();
            }
        });
    }

    private void navigateBackToCatalog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof DashboardView) {
            DashboardView dash = (DashboardView) parentWindow;
            dash.navigateToInventario();
        }
    }

    private boolean saveProductToDatabase() {
        String name = txtNombre.getText().trim();
        String barcode = txtSku.getText().trim();
        String ventaStr = txtPrecioVenta.getText().trim();
        String stockStr = txtStockInicial.getText().trim();

        if (name.isEmpty() || barcode.isEmpty() || ventaStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los campos obligatorios (*).",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(ventaStr);
            int stockQty = Integer.parseInt(stockStr);
            int critico = 5; // Stock mínimo siempre es 5

            Categoria selectedCat = (Categoria) cbCategorias.getSelectedItem();
            int categoryId = (selectedCat != null) ? selectedCat.getIdCategoria() : 1;

            // 1. Establish DB Connection
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null) {
                // Offline demo mode popup
                JOptionPane.showMessageDialog(this,
                        "Se ha actualizado el producto localmente (Modo Demo Offline).\n\n" +
                                "Nombre: " + name + "\nSKU: " + barcode + "\nPrecio: S/" + price,
                        "Éxito (Modo Demo)",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            }

            ProductoDAO productoDAO = new ProductoDAOImpl(connection);
            StockDAO stockDAO = new StockDAOImpl(connection);

            // Update existing product details
            producto.setNombreProducto(name);
            producto.setPrecioUnitario(price);
            producto.setIdCategoria(categoryId);
            producto.setCodigoBarras(barcode);
            boolean pUpdated = productoDAO.update(producto);

            if (pUpdated) {
                // Update or insert stock for the existing product
                Stock existingStock = stockDAO.findByProductoId(producto.getIdProducto());
                if (existingStock != null) {
                    existingStock.setCantidad(stockQty);
                    boolean sUpdated = stockDAO.update(existingStock);
                    if (sUpdated) {
                        JOptionPane.showMessageDialog(this,
                                "¡Producto y Stock actualizados exitosamente en la base de datos local!\n" +
                                        "ID: PRD-" + producto.getIdProducto() + "\n" +
                                        "Nuevo Stock: " + stockQty + " unidades.",
                                "Actualización exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    }
                } else {
                    Stock s = new Stock(stockQty, producto.getIdProducto());
                    boolean sInserted = stockDAO.insert(s);
                    if (sInserted) {
                        JOptionPane.showMessageDialog(this,
                                "¡Stock registrado para producto existente en la base de datos local!\n" +
                                        "ID: PRD-" + producto.getIdProducto() + "\n" +
                                        "Stock: " + stockQty + " unidades.",
                                "Registro exitoso",
                                JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    }
                }
            }
            throw new SQLException("Fallo al actualizar el producto existente.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Verifique que los precios y el stock contengan formatos numéricos válidos.",
                    "Formato de número inválido",
                    JOptionPane.ERROR_MESSAGE);
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos al guardar: " + e.getMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    private void setupRealTimeListeners() {
        DocumentListener previewUpdater = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        };

        txtNombre.getDocument().addDocumentListener(previewUpdater);
        txtPrecioVenta.getDocument().addDocumentListener(previewUpdater);
        txtSku.getDocument().addDocumentListener(previewUpdater);
        txtStockInicial.getDocument().addDocumentListener(previewUpdater);

        cbCategorias.addActionListener(e -> updatePreview());
    }

    private void updatePreview() {
        // Name
        String name = txtNombre.getText().trim();
        lblPreviewName.setText(name.isEmpty() ? "Nombre de Producto" : name);

        // Price
        String price = txtPrecioVenta.getText().trim();
        lblPreviewPrice.setText(price.isEmpty() ? "S/0.00 / und" : "S/" + price + " / und");

        // SKU
        String sku = txtSku.getText().trim();
        lblPreviewSku.setText("SKU: " + (sku.isEmpty() ? "-----------" : sku));

        // Stock
        String stock = txtStockInicial.getText().trim();
        if (!stock.isEmpty()) {
            try {
                int qty = Integer.parseInt(stock);
                lblPreviewStock.setText("STOCK ACTUAL: " + qty + " unidades");
                previewProgressBar.setValue(Math.min(qty, 100));
            } catch (NumberFormatException e) {
                lblPreviewStock.setText("STOCK ACTUAL: 0 unidades");
                previewProgressBar.setValue(0);
            }
        } else {
            lblPreviewStock.setText("STOCK ACTUAL: 0 unidades");
            previewProgressBar.setValue(0);
        }

        // Run validation checklist
        validateForm();
    }

    private void validateForm() {
        String name = txtNombre.getText().trim();
        String sku = txtSku.getText().trim();
        String venta = txtPrecioVenta.getText().trim();

        // Basic Info
        boolean isBasicOk = !name.isEmpty() && !sku.isEmpty() && !venta.isEmpty();
        checkBasicInfo.setChecked(isBasicOk);

        // SKU code
        boolean isSkuOk = !sku.isEmpty() && sku.matches("\\d+");
        checkSkuFormat.setChecked(isSkuOk);
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(0, 32));
        combo.setBackground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
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

    // --- CHECKLIST COMPONENT ---
    private static class ValidationCheckItem extends JPanel {
        private final JLabel lblText;
        private final JLabel lblIndicator;
        private boolean checked = false;

        public ValidationCheckItem(String text) {
            setLayout(new BorderLayout(8, 0));
            setOpaque(false);

            lblIndicator = new JLabel(new UncheckIcon());
            lblText = new JLabel(text);
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblText.setForeground(new Color(100, 116, 139));

            add(lblIndicator, BorderLayout.WEST);
            add(lblText, BorderLayout.CENTER);
        }

        public void setChecked(boolean check) {
            if (this.checked == check)
                return;
            this.checked = check;
            lblIndicator.setIcon(check ? new CheckIcon() : new UncheckIcon());
            lblText.setForeground(check ? new Color(15, 23, 42) : new Color(100, 116, 139));
        }
    }

    // --- VECTOR ICONS FOR FORM ---
    private static class SaveIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));

            // Floppy disk outline
            g2.drawRoundRect(x + 2, y + 2, 12, 12, 2, 2);
            g2.drawLine(x + 11, y + 2, x + 14, y + 5);
            g2.fillRect(x + 5, y + 2, 6, 4);
            g2.drawRect(x + 5, y + 9, 6, 5);

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

    private static class InfoAlertIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.fillOval(x, y + 2, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.drawString("i", x + 5, y + 11);
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

    private static class CheckIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Green filled circle
            g2.setColor(new Color(34, 197, 94)); // Green 500
            g2.fillOval(x, y + 1, 14, 14);

            // White checkmark
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 4, y + 8, x + 6, y + 10);
            g2.drawLine(x + 6, y + 10, x + 10, y + 5);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class UncheckIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(226, 232, 240)); // light slate border
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y + 1, 13, 13);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class UploadIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(1.8f));

            // Upload Arrow
            g2.drawLine(x + 8, y + 2, x + 8, y + 11);
            g2.drawLine(x + 8, y + 2, x + 4, y + 6);
            g2.drawLine(x + 8, y + 2, x + 12, y + 6);

            // Base line
            g2.drawLine(x + 2, y + 14, x + 14, y + 14);
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

    // --- BUTTON STYLING HELPERS ---
    private static class OutlineButton extends JButton {
        public OutlineButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(100, 116, 139)); // Slate 500
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

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
            g2.setColor(new Color(226, 232, 240)); // Slate 200 border
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class BlueOutlineButton extends JButton {
        public BlueOutlineButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(24, 119, 242)); // Blue text
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(new Color(239, 246, 255));
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(248, 250, 252));
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(new Color(191, 219, 254)); // light blue border
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class AccentButton extends JButton {
        public AccentButton(String text, Icon icon) {
            super(text, icon);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(8, 16, 8, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setIconTextGap(8);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(new Color(21, 128, 61));
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(22, 163, 74));
            } else {
                g2.setColor(new Color(24, 119, 242)); // Accent Blue
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
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

    private static class DashedUploadPanel extends JPanel {
        public DashedUploadPanel() {
            super(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(0, 90));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                    new float[] { 5.0f }, 0.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
        }
    }

    private static class RoundedBlueAlertPanel extends JPanel {
        public RoundedBlueAlertPanel() {
            super(new BorderLayout(8, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(239, 246, 255)); // Blue 50 background
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
        }
    }
}
