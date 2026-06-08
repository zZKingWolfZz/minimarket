package com.minimarket.view;

import com.minimarket.model.Cliente;
import com.minimarket.model.Producto;
import com.minimarket.model.Venta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class VentasView extends JPanel {

    // Main components
    private JTextField txtSearch;
    private JLabel lblCartItemCountBadge;
    private JTable tblCart;
    private CartTableModel cartTableModel;
    private List<CartItem> cartItems = new ArrayList<>();

    private JLabel lblGranTotal;
    private JLabel lblSubtotalVal;
    private JLabel lblIvaVal;
    private JLabel lblDescuentosVal;
    private JComboBox<Cliente> cbClientes;
    private JTextField txtDniCliente;
    private JTextField txtNombreCliente;
    private JTextField txtApellidoCliente;
    private JLabel lblClienteInfo;
    private JButton btnRegistrar;
    private JButton btnLimpiarCart;

    // Direct adding components (Kept invisible in the background to ensure 100%
    // controller compatibility)
    private JComboBox<Producto> cbProductos;
    private JSpinner spCantidad;
    private JLabel lblPrecioTotalVal;
    private JButton btnAgregarAlCarrito;

    // Autocomplete Suggestions Components
    private JPopupMenu suggestionPopup;
    private JList<Producto> suggestionList;
    private DefaultListModel<Producto> suggestionListModel;

    public VentasView() {
        setPreferredSize(new Dimension(850, 680));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252)); // Slate 50 background

        // Instantiate background variables to avoid NullPointerException in controller
        cbProductos = new JComboBox<>();
        spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        lblPrecioTotalVal = new JLabel("S/0.00");
        btnAgregarAlCarrito = new JButton();

        // 1. MAIN WORKSPACE (Center Panel)
        add(createMainContent(), BorderLayout.CENTER);

        // 2. Suggestions Popup setup
        initSuggestionsPopup();

        // Popular datos mock en el carrito para diseño visual en Eclipse sin colapsar
        cartItems.add(new CartItem(new Producto(1, "Leche Entera 1L", new BigDecimal("1.20"), 1, "PRD-001"), 2));
        cartItems.add(new CartItem(new Producto(25, "Galletas de Chocolate", new BigDecimal("0.85"), 2, "PRD-045"), 3));
        updateCartTable();
    }

    private void initSuggestionsPopup() {
        suggestionPopup = new JPopupMenu();
        suggestionPopup.setFocusable(false);

        suggestionListModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionListModel);
        suggestionList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        suggestionList.setSelectionBackground(new Color(241, 245, 249));
        suggestionList.setSelectionForeground(new Color(15, 23, 42));

        // Custom suggestion list item renderer
        suggestionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto) {
                    Producto p = (Producto) value;
                    setText("  " + p.getNombreProducto() + "   -   S/" + String.format("%.2f", p.getPrecioUnitario()));
                    setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                }
                return this;
            }
        });

        JScrollPane popScroll = new JScrollPane(suggestionList);
        popScroll.setBorder(null);
        popScroll.setPreferredSize(new Dimension(450, 180));
        suggestionPopup.add(popScroll);

        // Listen for suggestions selection
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectSuggestion();
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectSuggestion();
                }
            }
        });
    }

    private void showSuggestions() {
        String text = txtSearch.getText().trim().toLowerCase();
        if (text.isEmpty() || text.startsWith("escanee el")) {
            suggestionPopup.setVisible(false);
            return;
        }

        suggestionListModel.clear();
        for (int i = 0; i < cbProductos.getItemCount(); i++) {
            Producto p = cbProductos.getItemAt(i);
            if (p != null) {
                String name = p.getNombreProducto().toLowerCase();
                String code = "prd-" + String.format("%04d", p.getIdProducto());
                String barcode = p.getCodigoBarras() != null ? p.getCodigoBarras().toLowerCase() : "";
                if (name.contains(text) || code.contains(text) || (!barcode.isEmpty() && barcode.contains(text))) {
                    suggestionListModel.addElement(p);
                }
            }
        }

        if (!suggestionListModel.isEmpty()) {
            suggestionPopup.show(txtSearch, 0, txtSearch.getHeight());
            txtSearch.requestFocus(); // Keep keyboard focus in search box
        } else {
            suggestionPopup.setVisible(false);
        }
    }

    private void selectSuggestion() {
        Producto p = suggestionList.getSelectedValue();
        if (p != null) {
            boolean foundInCart = false;
            for (CartItem item : cartItems) {
                if (item.getProducto().getIdProducto() == p.getIdProducto()) {
                    item.setCantidad(item.getCantidad() + 1);
                    foundInCart = true;
                    break;
                }
            }
            if (!foundInCart) {
                cartItems.add(new CartItem(p, 1));
            }
            updateCartTable();
            txtSearch.setText("");
            suggestionPopup.setVisible(false);
        }
    }

    // --- MAIN CONTENT (Two Columns Split Layout) ---
    private JPanel createMainContent() {
        JPanel mainPos = new JPanel(new GridBagLayout());
        mainPos.setOpaque(false);
        mainPos.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.BOTH;
        gbcLeft.weighty = 1.0;

        // LEFT COLUMN (Buscador & Carrito)
        JPanel leftColumn = new JPanel(new BorderLayout(0, 20));
        leftColumn.setOpaque(false);

        // 1. Scanner Search Panel
        RoundedPanel searchPanel = new RoundedPanel(12);
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setLayout(new BorderLayout(15, 0));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(24, 119, 242), 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JPanel barcodeLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barcodeLabelPanel.setOpaque(false);
        JLabel lblBarcode = new JLabel(new BarcodeIcon());
        JLabel lblScannerText = new JLabel("SCANNER");
        lblScannerText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblScannerText.setForeground(new Color(24, 119, 242));
        barcodeLabelPanel.add(lblBarcode);
        barcodeLabelPanel.add(lblScannerText);

        txtSearch = new JTextField("Escanee el código de barras o escriba el nombre del producto...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(new Color(148, 163, 184)); // Placeholder grey
        txtSearch.setBorder(null);

        // Simulation key label
        JLabel lblF2 = new JLabel("F2", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblF2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblF2.setForeground(new Color(100, 116, 139));
        lblF2.setPreferredSize(new Dimension(30, 20));

        searchPanel.add(barcodeLabelPanel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(lblF2, BorderLayout.EAST);

        // Focus & Autocomplete Keyboard Listeners
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().startsWith("Escanee el")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(15, 23, 42));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText("Escanee el código de barras o escriba el nombre del producto...");
                    txtSearch.setForeground(new Color(148, 163, 184));
                }
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    return;
                }
                showSuggestions();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (suggestionPopup.isVisible()) {
                        suggestionList.requestFocus();
                        suggestionList.setSelectedIndex(0);
                    }
                }
            }
        });

        // Search Action (simulating scanner or enter press)
        txtSearch.addActionListener(e -> {
            String query = txtSearch.getText().trim().toLowerCase();
            if (!query.isEmpty() && !query.startsWith("escanee el")) {
                Producto found = null;
                for (int i = 0; i < cbProductos.getItemCount(); i++) {
                    Producto p = cbProductos.getItemAt(i);
                    if (p != null) {
                        String name = p.getNombreProducto().toLowerCase();
                        String code = "prd-" + String.format("%04d", p.getIdProducto());
                        String barcode = p.getCodigoBarras() != null ? p.getCodigoBarras().toLowerCase() : "";
                        if (name.contains(query) || code.equals(query)
                                || String.valueOf(p.getIdProducto()).equals(query)
                                || (!barcode.isEmpty() && barcode.equals(query))) {
                            found = p;
                            break;
                        }
                    }
                }

                if (found != null) {
                    boolean foundInCart = false;
                    for (CartItem item : cartItems) {
                        if (item.getProducto().getIdProducto() == found.getIdProducto()) {
                            item.setCantidad(item.getCantidad() + 1);
                            foundInCart = true;
                            break;
                        }
                    }
                    if (!foundInCart) {
                        cartItems.add(new CartItem(found, 1));
                    }
                    updateCartTable();
                    txtSearch.setText("");
                    suggestionPopup.setVisible(false);
                } else {
                    mostrarMensaje("Producto no encontrado.", true);
                }
            }
        });

        leftColumn.add(searchPanel, BorderLayout.NORTH);

        // 2. Shopping Cart Table Card
        RoundedPanel cartCard = new RoundedPanel(16);
        cartCard.setBackground(Color.WHITE);
        cartCard.setLayout(new BorderLayout());
        cartCard.setBorder(new EmptyBorder(22, 22, 18, 22));

        // Header inside Shopping Cart
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setOpaque(false);
        cartHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel cartTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cartTitlePanel.setOpaque(false);

        // Vertical blue decorator strip
        JPanel blueStrip = new JPanel();
        blueStrip.setPreferredSize(new Dimension(4, 20));
        blueStrip.setBackground(new Color(24, 119, 242));

        JLabel lblCartTitle = new JLabel("Carrito de Compras Actual");
        lblCartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCartTitle.setForeground(new Color(15, 23, 42));

        lblCartItemCountBadge = new JLabel("0 ítems", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249)); // Light gray badge background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblCartItemCountBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCartItemCountBadge.setForeground(new Color(71, 85, 105));
        lblCartItemCountBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        cartTitlePanel.add(blueStrip);
        cartTitlePanel.add(lblCartTitle);
        cartTitlePanel.add(lblCartItemCountBadge);

        JLabel lblTicket = new JLabel("Ticket: #TK-2024-0042");
        lblTicket.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTicket.setForeground(new Color(148, 163, 184));

        cartHeader.add(cartTitlePanel, BorderLayout.WEST);
        cartHeader.add(lblTicket, BorderLayout.EAST);

        // JTable Configuration
        cartTableModel = new CartTableModel();
        tblCart = new JTable(cartTableModel);
        tblCart.setRowHeight(64); // Spacious height for thumbnails
        tblCart.setShowGrid(false);
        tblCart.setBackground(Color.WHITE);
        tblCart.setIntercellSpacing(new Dimension(0, 0));
        tblCart.setSelectionBackground(new Color(248, 250, 252));
        tblCart.setSelectionForeground(new Color(15, 23, 42));

        // Configure Custom Column Renderers and Editors
        tblCart.getColumnModel().getColumn(0).setCellRenderer(new ProductCellRenderer());
        tblCart.getColumnModel().getColumn(1).setCellRenderer(new PaddedCellRenderer(false));
        tblCart.getColumnModel().getColumn(2).setCellRenderer(new QuantityCellRenderer());
        tblCart.getColumnModel().getColumn(2).setCellEditor(new QuantityCellEditor());
        tblCart.getColumnModel().getColumn(3).setCellRenderer(new PaddedCellRenderer(true));
        tblCart.getColumnModel().getColumn(4).setCellRenderer(new DeleteCellRenderer());
        tblCart.getColumnModel().getColumn(4).setCellEditor(new DeleteCellEditor());

        // Header Styling
        JTableHeader header = tblCart.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(new Color(148, 163, 184));
        header.setBackground(new Color(248, 250, 252));
        header.setDefaultRenderer(new CustomHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 38));

        JScrollPane scroll = new JScrollPane(tblCart);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(500, 360));

        // Cart Footer
        JPanel cartFooter = new JPanel(new BorderLayout());
        cartFooter.setOpaque(false);
        cartFooter.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel lblShortcuts = new JLabel("F1: Buscar   F5: Limpiar   F10: Pagar");
        lblShortcuts.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblShortcuts.setForeground(new Color(148, 163, 184));

        JLabel lblLastUpdate = new JLabel("Última actualización: 12:28:55 AM");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));

        cartFooter.add(lblShortcuts, BorderLayout.WEST);
        cartFooter.add(lblLastUpdate, BorderLayout.EAST);

        cartCard.add(cartHeader, BorderLayout.NORTH);
        cartCard.add(scroll, BorderLayout.CENTER);
        cartCard.add(cartFooter, BorderLayout.SOUTH);

        leftColumn.add(cartCard, BorderLayout.CENTER);

        // RIGHT COLUMN (Facturación, checkout, métodos de pago rápido, estado)
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

        // 1. Blue Grand Total Card
        JPanel grandTotalCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(24, 119, 242)); // Solid Vibrant Blue (#1877F2)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        grandTotalCard.setOpaque(false);
        grandTotalCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        grandTotalCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel lblGTTitle = new JLabel("GRAN TOTAL");
        lblGTTitle.setForeground(new Color(219, 234, 254)); // Light blue
        lblGTTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblGranTotal = new JLabel("S/0.00");
        lblGranTotal.setForeground(Color.WHITE);
        lblGranTotal.setFont(new Font("Segoe UI", Font.BOLD, 48)); // Large prominent font

        grandTotalCard.add(lblGTTitle, BorderLayout.NORTH);
        grandTotalCard.add(lblGranTotal, BorderLayout.CENTER);

        // 2. Billing details card
        RoundedPanel summaryCard = new RoundedPanel(16);
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        lblSubtotalVal = new JLabel("S/0.00");
        lblSubtotalVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSubtotalVal.setForeground(new Color(15, 23, 42));

        lblIvaVal = new JLabel("S/0.00");
        lblIvaVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIvaVal.setForeground(new Color(15, 23, 42));

        lblDescuentosVal = new JLabel("-S/0.00");
        lblDescuentosVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDescuentosVal.setForeground(new Color(100, 116, 139)); // Gray discount text matching mockup

        summaryCard.add(createSummaryRow("Subtotal", lblSubtotalVal));
        summaryCard.add(Box.createVerticalStrut(12));
        summaryCard.add(createSummaryRow("Descuentos", lblDescuentosVal));
        summaryCard.add(Box.createVerticalStrut(18));

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(241, 245, 249));
        summaryCard.add(separator);
        summaryCard.add(Box.createVerticalStrut(18));

        // Fast Payment Method Box
        JLabel lblPaymentHeader = new JLabel("MÉTODO DE PAGO RÁPIDO");
        lblPaymentHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPaymentHeader.setForeground(new Color(148, 163, 184));
        summaryCard.add(lblPaymentHeader);
        summaryCard.add(Box.createVerticalStrut(12));

        JPanel payMethodsGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        payMethodsGrid.setOpaque(false);
        payMethodsGrid.add(createPayMethodBtn("[ S/ ]\nEfectivo"));
        payMethodsGrid.add(createPayMethodBtn("[ █ ]\nTarjeta"));
        payMethodsGrid.add(createPayMethodBtn("[ QR ]\nQR / Transfer"));
        summaryCard.add(payMethodsGrid);
        summaryCard.add(Box.createVerticalStrut(20));

        // Client Rounded Panel (White background matching mockup)
        RoundedPanel clientPanel = new RoundedPanel(12);
        clientPanel.setBackground(Color.WHITE);
        clientPanel.setLayout(new BorderLayout());
        clientPanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        cbClientes = new JComboBox<>(); // Background invisible combobox for controller compatibility

        txtDniCliente = new JTextField("Ingrese DNI de Cliente...");
        txtDniCliente.setBackground(Color.WHITE);
        txtDniCliente.setBorder(null); // Borderless text field to match the card perfectly
        txtDniCliente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDniCliente.setForeground(new Color(148, 163, 184)); // Slate 400 placeholder color

        txtDniCliente.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtDniCliente.getText().equals("Ingrese DNI de Cliente...")) {
                    txtDniCliente.setText("");
                    txtDniCliente.setForeground(new Color(15, 23, 42)); // Slate 900
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtDniCliente.getText().trim().isEmpty()) {
                    txtDniCliente.setText("Ingrese DNI de Cliente...");
                    txtDniCliente.setForeground(new Color(148, 163, 184));
                }
            }
        });

        txtDniCliente.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateClienteInfo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateClienteInfo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateClienteInfo();
            }
        });

        JLabel lblUserIcon = new JLabel(" »  ");
        lblUserIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserIcon.setForeground(new Color(24, 119, 242));
        clientPanel.add(lblUserIcon, BorderLayout.WEST);
        clientPanel.add(txtDniCliente, BorderLayout.CENTER);

        // Nombre panel
        RoundedPanel namePanel = new RoundedPanel(12);
        namePanel.setBackground(Color.WHITE);
        namePanel.setLayout(new BorderLayout());
        namePanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        txtNombreCliente = new JTextField("Ingrese Nombre de Cliente...");
        txtNombreCliente.setBackground(Color.WHITE);
        txtNombreCliente.setBorder(null);
        txtNombreCliente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNombreCliente.setForeground(new Color(148, 163, 184));
        txtNombreCliente.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtNombreCliente.getText().equals("Ingrese Nombre de Cliente...")) {
                    txtNombreCliente.setText("");
                    txtNombreCliente.setForeground(new Color(15, 23, 42));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtNombreCliente.getText().trim().isEmpty()) {
                    txtNombreCliente.setText("Ingrese Nombre de Cliente...");
                    txtNombreCliente.setForeground(new Color(148, 163, 184));
                }
            }
        });

        JLabel lblNameIcon = new JLabel(" »  ");
        lblNameIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNameIcon.setForeground(new Color(24, 119, 242));
        namePanel.add(lblNameIcon, BorderLayout.WEST);
        namePanel.add(txtNombreCliente, BorderLayout.CENTER);

        // Apellidos panel
        RoundedPanel lastNamePanel = new RoundedPanel(12);
        lastNamePanel.setBackground(Color.WHITE);
        lastNamePanel.setLayout(new BorderLayout());
        lastNamePanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        txtApellidoCliente = new JTextField("Ingrese Apellidos de Cliente...");
        txtApellidoCliente.setBackground(Color.WHITE);
        txtApellidoCliente.setBorder(null);
        txtApellidoCliente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtApellidoCliente.setForeground(new Color(148, 163, 184));
        txtApellidoCliente.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtApellidoCliente.getText().equals("Ingrese Apellidos de Cliente...")) {
                    txtApellidoCliente.setText("");
                    txtApellidoCliente.setForeground(new Color(15, 23, 42));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtApellidoCliente.getText().trim().isEmpty()) {
                    txtApellidoCliente.setText("Ingrese Apellidos de Cliente...");
                    txtApellidoCliente.setForeground(new Color(148, 163, 184));
                }
            }
        });

        JLabel lblLastNameIcon = new JLabel(" »  ");
        lblLastNameIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLastNameIcon.setForeground(new Color(24, 119, 242));
        lastNamePanel.add(lblLastNameIcon, BorderLayout.WEST);
        lastNamePanel.add(txtApellidoCliente, BorderLayout.CENTER);

        lblClienteInfo = new JLabel("Ingrese DNI para buscar cliente");
        lblClienteInfo.setIcon(new InfoIcon());
        lblClienteInfo.setIconTextGap(6);
        lblClienteInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblClienteInfo.setForeground(new Color(148, 163, 184));
        lblClienteInfo.setBorder(new EmptyBorder(4, 4, 0, 4));

        JPanel clientContainer = new JPanel();
        clientContainer.setLayout(new BoxLayout(clientContainer, BoxLayout.Y_AXIS));
        clientContainer.setOpaque(false);
        clientContainer.add(clientPanel);
        clientContainer.add(Box.createVerticalStrut(8));
        clientContainer.add(namePanel);
        clientContainer.add(Box.createVerticalStrut(8));
        clientContainer.add(lastNamePanel);
        clientContainer.add(Box.createVerticalStrut(6));
        clientContainer.add(lblClienteInfo);

        summaryCard.add(clientContainer);
        summaryCard.add(Box.createVerticalStrut(18));

        // Checkout green button (Clean, rounded, no border label)
        btnRegistrar = new JButton("Registrar Venta (Cobrar)") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(21, 128, 61)); // Dark green
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(22, 163, 74));
                } else {
                    g2.setColor(new Color(34, 197, 94)); // Green-500 (#22C55E)
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setContentAreaFilled(false);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setBorder(new EmptyBorder(10, 0, 10, 0));
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Borderless Cancel sale flat button/link
        btnLimpiarCart = new JButton("Cancelar Venta", new CancelIcon()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(254, 226, 226)); // Red 100
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 241, 241)); // Red 50
                } else {
                    g2.setColor(new Color(255, 245, 245)); // Soft red tint
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(252, 165, 165)); // Red 200 soft border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLimpiarCart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLimpiarCart.setForeground(new Color(220, 38, 38)); // Red 600
        btnLimpiarCart.setContentAreaFilled(false);
        btnLimpiarCart.setFocusPainted(false);
        btnLimpiarCart.setBorder(new EmptyBorder(10, 0, 10, 0));
        btnLimpiarCart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarCart.setIconTextGap(8);
        btnLimpiarCart.addActionListener(e -> clearFields());

        // Place Cancel and Register buttons side-by-side
        JPanel checkoutActionsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        checkoutActionsPanel.setOpaque(false);
        checkoutActionsPanel.setPreferredSize(new Dimension(0, 44));
        checkoutActionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        checkoutActionsPanel.add(btnLimpiarCart);
        checkoutActionsPanel.add(btnRegistrar);

        summaryCard.add(checkoutActionsPanel);

        // 3. Status card (Terminal Info Card)
        RoundedPanel statusCard = new RoundedPanel(12);
        statusCard.setBackground(Color.WHITE);
        statusCard.setLayout(new GridLayout(3, 1, 0, 8));
        statusCard.setBorder(new EmptyBorder(15, 20, 15, 20));
        statusCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        statusCard.add(createTerminalRow("Terminal:", "POS-MAIN-01"));

        JLabel lblActiveVal = new JLabel("ABIERTA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 252, 231)); // Soft green badge background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblActiveVal.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblActiveVal.setForeground(new Color(21, 128, 61));
        lblActiveVal.setBorder(new EmptyBorder(3, 8, 3, 8));
        lblActiveVal.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel activeRow = new JPanel(new BorderLayout());
        activeRow.setOpaque(false);
        JLabel lblActiveLabel = new JLabel("Estado de Caja:");
        lblActiveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblActiveLabel.setForeground(new Color(100, 116, 139));
        activeRow.add(lblActiveLabel, BorderLayout.WEST);
        activeRow.add(lblActiveVal, BorderLayout.EAST);
        statusCard.add(activeRow);

        rightColumn.add(grandTotalCard);
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(summaryCard);
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(statusCard);

        // GridBag positions
        gbcLeft.gridx = 0;
        gbcLeft.weightx = 0.65;
        gbcLeft.insets = new Insets(0, 0, 0, 18);
        mainPos.add(leftColumn, gbcLeft);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.weighty = 1.0;
        gbcRight.gridx = 1;
        gbcRight.weightx = 0.35;
        gbcRight.insets = new Insets(0, 0, 0, 0);
        mainPos.add(rightColumn, gbcRight);

        return mainPos;
    }

    // --- RECALCULATIONS & CART MANAGEMENT ---
    private void updateCartTable() {
        cartTableModel.fireTableDataChanged();

        BigDecimal total = BigDecimal.ZERO;
        int totalItemsCount = 0;

        for (CartItem item : cartItems) {
            total = total.add(item.getSubtotal());
            totalItemsCount += item.getCantidad();
        }

        lblCartItemCountBadge.setText(totalItemsCount + " ítems");

        BigDecimal ivaVal = BigDecimal.ZERO;
        BigDecimal grandTotal = total;

        lblSubtotalVal.setText("S/" + String.format("%.2f", total));
        lblIvaVal.setText("S/" + String.format("%.2f", ivaVal));
        lblGranTotal.setText("S/" + String.format("%.2f", grandTotal));
    }

    // --- HELPER COMPONENT GENERATORS ---
    private JPanel createSummaryRow(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(100, 116, 139));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createTerminalRow(String labelText, String valText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(valText);
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(new Color(15, 23, 42));

        panel.add(lbl, BorderLayout.WEST);
        panel.add(val, BorderLayout.EAST);
        return panel;
    }

    private JButton createPayMethodBtn(String text) {
        JButton btn = new JButton("<html><center>" + text.replaceAll("\n", "<br>") + "</center></html>") {
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
        btn.setForeground(new Color(71, 85, 105));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 5, 10, 5));
        return btn;
    }

    // --- CONTROLLER API BINDINGS (100% COMPATIBLE) ---

    public void addRegistrarVentaListener(ActionListener l) {
        btnRegistrar.addActionListener(l);
    }

    public void addProductoSelectionListener(ActionListener l) {
        cbProductos.addActionListener(l);
    }

    public void addCantidadSelectionListener(ChangeListener l) {
        spCantidad.addChangeListener(l);
    }

    public void setProductos(List<Producto> productos) {
        cbProductos.removeAllItems();
        cbProductos.addItem(null);
        if (productos != null) {
            for (Producto p : productos) {
                cbProductos.addItem(p);
            }
        }
    }

    public void setClientes(List<Cliente> clientes) {
        cbClientes.removeAllItems();
        cbClientes.addItem(null);
        if (clientes != null) {
            for (Cliente c : clientes) {
                cbClientes.addItem(c);
            }
        }
        updateClienteInfo();
    }

    public void setSalesTableData(List<Venta> ventas, List<Producto> productos, List<Cliente> clientes) {
        // No-op
    }

    public Producto getSelectedProducto() {
        return (Producto) cbProductos.getSelectedItem();
    }

    public int getCantidad() {
        return (Integer) spCantidad.getValue();
    }

    public void setPrecioTotal(BigDecimal total) {
        lblPrecioTotalVal.setText("S/" + String.format("%.2f", total));
    }

    public String getDniCliente() {
        return txtDniCliente != null ? txtDniCliente.getText().trim() : "";
    }

    public Cliente getSelectedCliente() {
        String dni = txtDniCliente.getText().trim();
        if (dni.isEmpty() || dni.equals("Ingrese DNI de Cliente...") || dni.length() != 8) {
            return null;
        }

        // 1. Check if the client already exists in cbClientes
        for (int i = 0; i < cbClientes.getItemCount(); i++) {
            Cliente c = cbClientes.getItemAt(i);
            if (c != null && c.getDniRuc() != null && c.getDniRuc().equals(dni)) {
                return c;
            }
        }

        // 2. If it is a new client, extract the manually typed name and surname
        String nombre = txtNombreCliente.getText().trim();
        String apellido = txtApellidoCliente.getText().trim();

        if (nombre.isEmpty() || nombre.equals("Ingrese Nombre de Cliente...") || nombre.equals("Consultando...") ||
                apellido.isEmpty() || apellido.equals("Ingrese Apellidos de Cliente...")
                || apellido.equals("Consultando...")) {
            return null;
        }

        String apellidoPaterno = "";
        String apellidoMaterno = "";
        String[] parts = apellido.split("\\s+");
        if (parts.length > 0) {
            apellidoPaterno = parts[0].toUpperCase();
        }
        if (parts.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) sb.append(" ");
                sb.append(parts[i]);
            }
            apellidoMaterno = sb.toString().toUpperCase();
        }

        // Return a new Cliente object (with Id_cliente = 0, which the controller will auto-insert!)
        return new Cliente(nombre.toUpperCase(), apellidoPaterno, apellidoMaterno, dni);
    }

    private void updateClienteInfo() {
        if (txtDniCliente == null || lblClienteInfo == null || cbClientes == null) {
            return;
        }
        String dni = txtDniCliente.getText().trim();
        if (dni.isEmpty() || dni.equals("Ingrese DNI de Cliente...")) {
            lblClienteInfo.setText("Ingrese DNI para buscar cliente");
            lblClienteInfo.setIcon(new InfoIcon());
            lblClienteInfo.setForeground(new Color(148, 163, 184));

            txtNombreCliente.setText("Ingrese Nombre de Cliente...");
            txtNombreCliente.setForeground(new Color(148, 163, 184));
            txtApellidoCliente.setText("Ingrese Apellidos de Cliente...");
            txtApellidoCliente.setForeground(new Color(148, 163, 184));
            return;
        }

        // We check if the DNI is exactly 8 digits long
        if (dni.length() == 8) {
            // 1. Check if it already exists locally to show instant feedback and autofill
            Cliente locallyFound = null;
            for (int i = 0; i < cbClientes.getItemCount(); i++) {
                Cliente c = cbClientes.getItemAt(i);
                if (c != null && c.getDniRuc() != null && c.getDniRuc().equals(dni)) {
                    locallyFound = c;
                    break;
                }
            }

            if (locallyFound != null) {
                lblClienteInfo.setText("Cliente registrado: " + locallyFound.toString());
                lblClienteInfo.setIcon(new SuccessIcon());
                lblClienteInfo.setForeground(new Color(34, 197, 94)); // Green-500
                
                txtNombreCliente.setText(locallyFound.getNombre());
                txtNombreCliente.setForeground(new Color(15, 23, 42)); // Active Slate 900
                
                String apPaterno = locallyFound.getApellidoPaterno();
                String apMaterno = locallyFound.getApellidoMaterno();
                String apellidos = ((apPaterno != null ? apPaterno : "") + " " + (apMaterno != null ? apMaterno : "")).trim();
                txtApellidoCliente.setText(apellidos.isEmpty() ? "" : apellidos);
                txtApellidoCliente.setForeground(new Color(15, 23, 42)); // Active Slate 900
                return;
            }

            // 2. Otherwise, notify cashier they can register it manually
            lblClienteInfo.setText("Cliente nuevo. Ingrese nombre y apellido manual.");
            lblClienteInfo.setIcon(new InfoIcon());
            lblClienteInfo.setForeground(new Color(24, 119, 242)); // Blue-500

            // Clear input fields and leave them editable for manual registration
            txtNombreCliente.setText("Ingrese Nombre de Cliente...");
            txtNombreCliente.setForeground(new Color(148, 163, 184));
            txtApellidoCliente.setText("Ingrese Apellidos de Cliente...");
            txtApellidoCliente.setForeground(new Color(148, 163, 184));

        } else if (dni.length() > 8) {
            lblClienteInfo.setText("DNI inválido (máximo 8 dígitos)");
            lblClienteInfo.setIcon(new AlertIcon());
            lblClienteInfo.setForeground(new Color(239, 68, 68)); // Red-500
        } else {
            lblClienteInfo.setText("Ingrese DNI de 8 dígitos...");
            lblClienteInfo.setIcon(new InfoIcon());
            lblClienteInfo.setForeground(new Color(148, 163, 184));
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void mostrarMensaje(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message,
                isError ? "Error" : "Éxito",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFields() {
        cartItems.clear();
        updateCartTable();
        if (cbProductos.getItemCount() > 0) {
            cbProductos.setSelectedIndex(0);
        }
        spCantidad.setValue(1);
        if (cbClientes.getItemCount() > 0) {
            cbClientes.setSelectedIndex(0);
        }
        lblPrecioTotalVal.setText("S/0.00");
        if (txtDniCliente != null) {
            txtDniCliente.putClientProperty("lastQueriedDni", null);
            txtDniCliente.setText("Ingrese DNI de Cliente...");
            txtDniCliente.setForeground(new Color(148, 163, 184));
        }
        updateClienteInfo();
    }

    // --- TABLE MODEL DEFINITION ---
    private class CartTableModel extends AbstractTableModel {
        private final String[] headers = { "PRODUCTO", "PRECIO", "CANTIDAD", "SUBTOTAL", "" };

        @Override
        public int getRowCount() {
            return cartItems.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public String getColumnName(int column) {
            return headers[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= cartItems.size())
                return null;
            CartItem item = cartItems.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item; // Render custom product details
                case 1:
                    return "S/" + String.format("%.2f", item.getProducto().getPrecioUnitario());
                case 2:
                    return item.getCantidad();
                case 3:
                    return "S/" + String.format("%.2f", item.getSubtotal());
                case 4:
                    return "✕"; // Remove action
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2 || columnIndex == 4; // Interactive quantity and delete columns
        }
    }

    // --- CUSTOM SWING UI HELPERS ---
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
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // --- CUSTOM TABLE RENDERERS / EDITORS ---

    // Custom Table Header Renderer
    private static class CustomHeaderRenderer extends DefaultTableCellRenderer {
        public CustomHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(248, 250, 252)); // Slate 50 background
            setForeground(new Color(100, 116, 139)); // Slate 500 grey text
            setFont(new Font("Segoe UI", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            return this;
        }
    }

    // Thumbnail Vector Component for Pastels Letterbox Initials
    private static class ThumbnailComponent extends JComponent {
        private final String text;
        private final Color bgColor;

        public ThumbnailComponent(String text, int id) {
            this.text = text;
            this.bgColor = getPastelColor(id);
            setPreferredSize(new Dimension(42, 42));
        }

        private Color getPastelColor(int id) {
            int hash = id * 31;
            int r = (hash & 0xFF0000) >> 16;
            int g = (hash & 0x00FF00) >> 8;
            int b = (hash & 0x0000FF);
            r = (r % 80) + 120;
            g = (g % 80) + 120;
            b = (b % 80) + 120;
            return new Color(r, g, b);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded square background
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            // Draw white centered letter initials
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(text)) / 2;
            int ty = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    private class ProductCellRenderer extends DefaultTableCellRenderer {
        private JPanel panel;
        private JLabel lblName;
        private JLabel lblCode;
        private JPanel imgPlaceholder;

        public ProductCellRenderer() {
            panel = new JPanel(new BorderLayout(12, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            imgPlaceholder = new JPanel(new BorderLayout());
            imgPlaceholder.setOpaque(false);
            panel.add(imgPlaceholder, BorderLayout.WEST);

            JPanel textWrap = new JPanel(new GridLayout(2, 1, 0, 2));
            textWrap.setOpaque(false);

            lblName = new JLabel();
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(new Color(15, 23, 42));

            lblCode = new JLabel();
            lblCode.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblCode.setForeground(new Color(148, 163, 184));

            textWrap.add(lblName);
            textWrap.add(lblCode);

            panel.add(textWrap, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof CartItem) {
                CartItem item = (CartItem) value;
                lblName.setText(item.getProducto().getNombreProducto());
                lblCode.setText("PRD-" + String.format("%04d", item.getProducto().getIdProducto()));

                // Create initials for the thumbnail box
                String name = item.getProducto().getNombreProducto();
                String initials = name.length() >= 2 ? name.substring(0, 2).toUpperCase()
                        : name.substring(0, 1).toUpperCase();
                imgPlaceholder.removeAll();
                imgPlaceholder.add(new ThumbnailComponent(initials, item.getProducto().getIdProducto()));
            }
            if (isSelected) {
                panel.setBackground(new Color(248, 250, 252));
            } else {
                panel.setBackground(Color.WHITE);
            }
            return panel;
        }
    }

    private class PaddedCellRenderer extends DefaultTableCellRenderer {
        private final boolean bold;

        public PaddedCellRenderer(boolean bold) {
            this.bold = bold;
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
            setForeground(new Color(15, 23, 42));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (isSelected) {
                setBackground(new Color(248, 250, 252));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    // Pill Capsule Panel Layout
    private static class QuantityPanel extends JPanel {
        public QuantityPanel(JButton dec, JLabel qtyLabel, JButton inc, Color cellBg) {
            setLayout(new GridBagLayout());
            setOpaque(true);
            setBackground(cellBg);

            JPanel capsule = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249)); // Slate 100
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(new Color(226, 232, 240)); // Slate 200
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                    g2.dispose();
                }
            };
            capsule.setOpaque(false);
            capsule.setPreferredSize(new Dimension(76, 28));

            dec.setPreferredSize(new Dimension(18, 24));
            inc.setPreferredSize(new Dimension(18, 24));
            qtyLabel.setPreferredSize(new Dimension(20, 24));

            dec.setMargin(new Insets(0, 0, 0, 0));
            inc.setMargin(new Insets(0, 0, 0, 0));

            capsule.add(dec);
            capsule.add(qtyLabel);
            capsule.add(inc);

            add(capsule);
        }
    }

    private class QuantityCellRenderer extends DefaultTableCellRenderer {
        private QuantityPanel panel;
        private JButton btnDec;
        private JButton btnInc;
        private JLabel lblQty;

        public QuantityCellRenderer() {
            btnDec = new JButton("-");
            btnDec.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnDec.setFocusPainted(false);
            btnDec.setContentAreaFilled(false);
            btnDec.setBorderPainted(false);
            btnDec.setForeground(new Color(100, 116, 139));

            lblQty = new JLabel("1", SwingConstants.CENTER);
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblQty.setForeground(new Color(15, 23, 42));

            btnInc = new JButton("+");
            btnInc.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnInc.setFocusPainted(false);
            btnInc.setContentAreaFilled(false);
            btnInc.setBorderPainted(false);
            btnInc.setForeground(new Color(100, 116, 139));

            panel = new QuantityPanel(btnDec, lblQty, btnInc, Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            lblQty.setText(value != null ? value.toString() : "1");
            Color bg = isSelected ? new Color(248, 250, 252) : Color.WHITE;
            panel.setBackground(bg);
            return panel;
        }
    }

    private class QuantityCellEditor extends AbstractCellEditor implements TableCellEditor {
        private QuantityPanel panel;
        private JButton btnDec;
        private JButton btnInc;
        private JLabel lblQty;
        private int qty;
        private int editingRow;

        public QuantityCellEditor() {
            btnDec = new JButton("-");
            btnDec.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnDec.setFocusPainted(false);
            btnDec.setContentAreaFilled(false);
            btnDec.setBorderPainted(false);
            btnDec.setForeground(new Color(71, 85, 105));
            btnDec.setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblQty = new JLabel("1", SwingConstants.CENTER);
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblQty.setForeground(new Color(15, 23, 42));

            btnInc = new JButton("+");
            btnInc.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnInc.setFocusPainted(false);
            btnInc.setContentAreaFilled(false);
            btnInc.setBorderPainted(false);
            btnInc.setForeground(new Color(71, 85, 105));
            btnInc.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnDec.addActionListener(e -> {
                if (qty > 1) {
                    qty--;
                    lblQty.setText(String.valueOf(qty));
                    if (editingRow >= 0 && editingRow < cartItems.size()) {
                        cartItems.get(editingRow).setCantidad(qty);
                        updateCartTable();
                    }
                }
            });

            btnInc.addActionListener(e -> {
                qty++;
                lblQty.setText(String.valueOf(qty));
                if (editingRow >= 0 && editingRow < cartItems.size()) {
                    cartItems.get(editingRow).setCantidad(qty);
                    updateCartTable();
                }
            });

            panel = new QuantityPanel(btnDec, lblQty, btnInc, new Color(248, 250, 252));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            editingRow = row;
            qty = (Integer) value;
            lblQty.setText(String.valueOf(qty));
            panel.setBackground(new Color(248, 250, 252));
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return qty;
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            return true;
        }
    }

    private class DeleteCellRenderer extends DefaultTableCellRenderer {
        private JButton btnDelete;

        public DeleteCellRenderer() {
            btnDelete = new JButton(new TrashIcon());
            btnDelete.setContentAreaFilled(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (isSelected) {
                btnDelete.setBackground(new Color(248, 250, 252));
            } else {
                btnDelete.setBackground(Color.WHITE);
            }
            return btnDelete;
        }
    }

    private class DeleteCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton btnDelete;
        private int editingRow;

        public DeleteCellEditor() {
            btnDelete = new JButton(new TrashIcon());
            btnDelete.setContentAreaFilled(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setFocusPainted(false);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnDelete.addActionListener(e -> {
                if (editingRow >= 0 && editingRow < cartItems.size()) {
                    cartItems.remove(editingRow);
                    fireEditingCanceled();
                    updateCartTable();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            editingRow = row;
            return btnDelete;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // --- CARTITEM DATA CLASS ---
    public static class CartItem {
        private final Producto producto;
        private int cantidad;
        private BigDecimal subtotal;

        public CartItem(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
            recalculateSubtotal();
        }

        public Producto getProducto() {
            return producto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
            recalculateSubtotal();
        }

        private void recalculateSubtotal() {
            if (producto != null && producto.getPrecioUnitario() != null) {
                this.subtotal = producto.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
            } else {
                this.subtotal = BigDecimal.ZERO;
            }
        }
    }

    // --- Vector Icon Definitions ---
    private static class BarcodeIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            int h = getIconHeight();
            g2.fillRect(x + 2, y, 2, h);
            g2.fillRect(x + 5, y, 3, h);
            g2.fillRect(x + 9, y, 1, h);
            g2.fillRect(x + 12, y, 2, h);
            g2.fillRect(x + 15, y, 1, h);
            g2.fillRect(x + 18, y, 3, h);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class TrashIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(239, 68, 68));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Trash can lid
            g2.drawLine(x + 3, y + 3, x + 13, y + 3);
            g2.drawLine(x + 6, y + 3, x + 6, y + 1);
            g2.drawLine(x + 6, y + 1, x + 10, y + 1);
            g2.drawLine(x + 10, y + 1, x + 10, y + 3);

            // Trash can body
            g2.drawRect(x + 4, y + 4, 8, 10);

            // Vertical lines inside can body
            g2.drawLine(x + 6, y + 6, x + 6, y + 12);
            g2.drawLine(x + 8, y + 6, x + 8, y + 12);
            g2.drawLine(x + 10, y + 6, x + 10, y + 12);

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

    private static class CancelIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(239, 68, 68));
            g2.setStroke(new BasicStroke(2.0f));

            g2.drawOval(x + 1, y + 1, 14, 14);
            g2.drawLine(x + 4, y + 4, x + 12, y + 12);

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

    private static class InfoIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184));
            g2.fillOval(x + 4, y + 4, 8, 8);
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

    private static class AlertIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(239, 68, 68));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(x + 1, y + 1, 14, 14);
            g2.drawLine(x + 8, y + 4, x + 8, y + 8);
            g2.fillRect(x + 7, y + 10, 2, 2);
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

    private static class SearchIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(x + 2, y + 2, 8, 8);
            g2.drawLine(x + 9, y + 9, x + 14, y + 14);
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

    private static class SuccessIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(34, 197, 94));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x + 1, y + 1, 14, 14);
            g2.drawLine(x + 5, y + 8, x + 7, y + 10);
            g2.drawLine(x + 7, y + 10, x + 11, y + 5);
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
}
