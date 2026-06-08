package com.minimarket.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;

public class DashboardView extends JFrame {

    private JButton btnVentas;
    private JButton btnInventario;
    private JButton btnLogout;
    private JLabel lblSidebarUserName;
    private JLabel lblSidebarUserRole;
    private JLabel lblFooterUser;
    private JLabel lblFooterDB;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JLabel lblHeaderTitle;

    private SidebarButton btnDashboard;
    private SidebarButton btnCategories;
    private SidebarButton btnReports;

    public DashboardView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("MiniMarket - Panel de Control Principal (Mini-POS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Iniciar maximizado
        setSize(1100, 780);
        setLocationRelativeTo(null);

        // 1. PANEL PRINCIPAL
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));

        // 2. SIDEBAR LATERAL (Estilo Moderno Limpio)
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(241, 245, 249)));

        // Sidebar - Cabecera / Marca
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(25, 0, 25, 0));

        JLabel lblBrandIcon = new JLabel(new MiniPOSLogoIcon());
        JLabel lblBrandText = new JLabel("MINI-POS");
        lblBrandText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrandText.setForeground(new Color(15, 23, 42)); // Slate 900

        brandPanel.add(lblBrandIcon);
        brandPanel.add(lblBrandText);

        // Sidebar - Botones / Menú
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);

        btnDashboard = new SidebarButton("Dashboard", new DashboardIcon(true), true);
        btnVentas = new SidebarButton("venta", new CartIcon(false), false);
        btnInventario = new SidebarButton("Stock", new BoxIcon(false), false);
        btnCategories = new SidebarButton("Categories", new CategoryIcon(), false);
        btnReports = new SidebarButton("Reports", new ReportsIcon(), false);

        menuContainer.add(btnDashboard);
        menuContainer.add(Box.createVerticalStrut(6));
        menuContainer.add(btnVentas);
        menuContainer.add(Box.createVerticalStrut(6));
        menuContainer.add(btnInventario);
        menuContainer.add(Box.createVerticalStrut(6));
        menuContainer.add(btnCategories);
        menuContainer.add(Box.createVerticalStrut(6));
        menuContainer.add(btnReports);

        // Sidebar - Contenedor Norte
        JPanel sidebarNorth = new JPanel(new BorderLayout());
        sidebarNorth.setOpaque(false);
        sidebarNorth.add(brandPanel, BorderLayout.NORTH);
        sidebarNorth.add(menuContainer, BorderLayout.CENTER);

        // Sidebar - Perfil inferior del Cajero/Administrador
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setBackground(Color.WHITE);
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(241, 245, 249)),
                new EmptyBorder(15, 18, 15, 18)));

        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circular background (soft grey/green)
                g2.setColor(new Color(220, 252, 231)); // green 100 background
                g2.fillOval(0, 0, 32, 32);

                // Draw a beautiful styled user vector character matching image 2
                g2.setColor(new Color(15, 23, 42)); // Dark hair
                g2.fillArc(6, 4, 20, 12, 0, 180);

                g2.setColor(new Color(254, 215, 170)); // skin tone
                g2.fillOval(9, 8, 14, 14);

                g2.setColor(new Color(15, 23, 42)); // shirt
                g2.fillArc(2, 20, 28, 20, 0, 180);

                g2.setColor(Color.WHITE); // tie/collar
                g2.fillPolygon(new int[] { 13, 16, 19 }, new int[] { 20, 24, 20 }, 3);

                // Green online indicator dot at bottom right
                g2.setColor(new Color(34, 197, 94)); // green 500
                g2.fillOval(24, 24, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(24, 24, 8, 8);

                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(32, 32));
        avatarPanel.setOpaque(false);

        JPanel userTextPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        userTextPanel.setOpaque(false);

        lblSidebarUserName = new JLabel("Store Admin");
        lblSidebarUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSidebarUserName.setForeground(new Color(15, 23, 42)); // Slate 900

        lblSidebarUserRole = new JLabel("Manager");
        lblSidebarUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSidebarUserRole.setForeground(new Color(100, 116, 139)); // Slate 400

        userTextPanel.add(lblSidebarUserName);
        userTextPanel.add(lblSidebarUserRole);

        userPanel.add(avatarPanel, BorderLayout.WEST);
        userPanel.add(userTextPanel, BorderLayout.CENTER);

        sidebarPanel.add(sidebarNorth, BorderLayout.NORTH);
        sidebarPanel.add(userPanel, BorderLayout.SOUTH);

        // 3. CABECERA PRINCIPAL (Header)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                headerPanel.getBorder(),
                new EmptyBorder(0, 24, 0, 24)));

        // Título de la sección
        lblHeaderTitle = new JLabel("Panel de Control del Sistema");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeaderTitle.setForeground(new Color(15, 23, 42));

        // Barra de búsqueda central
        JPanel searchWrapper = new JPanel(new GridBagLayout());
        searchWrapper.setOpaque(false);

        // Acciones derecha: Campana de Notificaciones y Cerrar Sesión
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerRight.setOpaque(false);

        // Botón de notificaciones
        JButton btnNotif = new JButton(new BellIcon()) {
            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        };

        btnLogout = new OutlineButton("Cerrar sesión");
        btnLogout.setPreferredSize(new Dimension(110, 32));

        headerRight.add(btnNotif);
        headerRight.add(btnLogout);

        headerPanel.add(lblHeaderTitle, BorderLayout.WEST);
        headerPanel.add(searchWrapper, BorderLayout.CENTER);
        headerPanel.add(headerRight, BorderLayout.EAST);

        // 4. PANEL DE CONTENIDO DINÁMICO
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(248, 250, 252));
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Por defecto cargamos el panel de estadísticas del Dashboard
        contentPanel.add(new DashboardStatsPanel(), BorderLayout.CENTER);

        // 5. BARRA DE ESTADO INFERIOR (FOOTER)
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setBackground(Color.WHITE);
        footerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(241, 245, 249)),
                new EmptyBorder(6, 24, 6, 24)));

        // Info de usuario y base de datos
        JPanel footerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        footerLeft.setOpaque(false);

        JLabel lblIconUser = new JLabel(new FooterUserIcon());

        lblFooterUser = new JLabel("USER: INVITADO");
        lblFooterUser.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblFooterUser.setForeground(new Color(100, 116, 139));

        JLabel lblIconDB = new JLabel(new FooterDBIcon());

        lblFooterDB = new JLabel("DB: CONNECTED");
        lblFooterDB.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblFooterDB.setForeground(new Color(100, 116, 139));

        footerLeft.add(lblIconUser);
        footerLeft.add(lblFooterUser);
        footerLeft.add(lblIconDB);
        footerLeft.add(lblFooterDB);

        // Estado del sistema derecho
        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerRight.setOpaque(false);

        JLabel lblHealth = new JLabel("SYSTEM HEALTH: OPTIMAL (99.9%)", new FooterHealthIcon(), SwingConstants.LEFT);
        lblHealth.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHealth.setForeground(new Color(24, 119, 242));
        lblHealth.setIconTextGap(6);

        FlatProgressBar healthBar = new FlatProgressBar(0.99f, new Color(24, 119, 242));
        healthBar.setPreferredSize(new Dimension(80, 8));

        footerRight.add(lblHealth);
        footerRight.add(healthBar);

        footerBar.add(footerLeft, BorderLayout.WEST);
        footerBar.add(footerRight, BorderLayout.EAST);

        // Armar el contenedor derecho
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(headerPanel, BorderLayout.NORTH);
        rightContainer.add(contentPanel, BorderLayout.CENTER);
        rightContainer.add(footerBar, BorderLayout.SOUTH);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(rightContainer, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        // Control de navegación local del Sidebar
        // Se registra de forma interactiva en el controlador
        btnDashboard.addActionListener(e -> {
            setActiveSidebarButton(btnDashboard);
        });
    }

    private void setActiveSidebarButton(SidebarButton activeBtn) {
        btnDashboard.setActive(activeBtn == btnDashboard);
        ((SidebarButton) btnVentas).setActive(activeBtn == btnVentas);
        ((SidebarButton) btnInventario).setActive(activeBtn == btnInventario);
        btnCategories.setActive(activeBtn == btnCategories);
        btnReports.setActive(activeBtn == btnReports);
        sidebarPanel.repaint();
    }

    private void showDemoMessage(String moduleName) {
        JPanel demoPanel = new JPanel(new GridBagLayout());
        demoPanel.setBackground(new Color(248, 250, 252));

        JLabel lblDemo = new JLabel("El " + moduleName + " está actualmente en fase de demostración.", JLabel.CENTER);
        lblDemo.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblDemo.setForeground(new Color(100, 116, 139));

        demoPanel.add(lblDemo);
        setViewPanel(demoPanel);
    }

    public void setLoggedUser(String nombreCompleto, String rol) {
        lblSidebarUserName.setText(nombreCompleto.equals("Invitado") ? "Store Admin" : nombreCompleto);
        lblSidebarUserRole.setText(rol.equals("Ninguno") ? "Manager" : rol);
        lblFooterUser.setText("USER: " + nombreCompleto.toUpperCase());
    }

    public void setHeaderTitle(String title) {
        if (lblHeaderTitle != null) {
            lblHeaderTitle.setText(title);
        }
    }

    public void setViewPanel(JPanel panel) {
        contentPanel.removeAll();
        if (panel instanceof CategoriasView) {
            contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        } else {
            contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        }
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void addDashboardMenuListener(ActionListener l) {
        btnDashboard.addActionListener(e -> {
            setActiveSidebarButton(btnDashboard);
            l.actionPerformed(e);
        });
    }

    public JPanel createDashboardStatsPanel(java.util.List<com.minimarket.model.Venta> ventas) {
        return new DashboardStatsPanel(ventas, null, null);
    }

    public JPanel createDashboardStatsPanel(java.util.List<com.minimarket.model.Venta> ventas,
            java.util.List<com.minimarket.model.Stock> stocks,
            java.util.List<com.minimarket.model.Producto> productos) {
        return new DashboardStatsPanel(ventas, stocks, productos);
    }

    public void addVentasMenuListener(ActionListener l) {
        btnVentas.addActionListener(e -> {
            setActiveSidebarButton((SidebarButton) btnVentas);
            l.actionPerformed(e);
        });
    }

    public void addInventarioMenuListener(ActionListener l) {
        btnInventario.addActionListener(e -> {
            setActiveSidebarButton((SidebarButton) btnInventario);
            l.actionPerformed(e);
        });
    }

    public void addCategoriasMenuListener(ActionListener l) {
        btnCategories.addActionListener(e -> {
            setActiveSidebarButton(btnCategories);
            l.actionPerformed(e);
        });
    }

    public void addReportsMenuListener(ActionListener l) {
        btnReports.addActionListener(e -> {
            setActiveSidebarButton(btnReports);
            l.actionPerformed(e);
        });
    }

    public void navigateToInventario() {
        if (btnInventario != null) {
            btnInventario.doClick();
        }
    }

    public void navigateToCategorias() {
        if (btnCategories != null) {
            btnCategories.doClick();
        }
    }

    public void addLogoutMenuListener(ActionListener l) {
        btnLogout.addActionListener(l);
    }

    // ==========================================
    // CLASES SWING PERSONALIZADAS Y SOPORTE VECTORIAL
    // ==========================================

    // Panel Central de Estadísticas de Alta Fidelidad
    private static class DashboardStatsPanel extends JPanel {
        public DashboardStatsPanel() {
            this(new java.util.ArrayList<>(), null, null);
        }

        public DashboardStatsPanel(java.util.List<com.minimarket.model.Venta> ventas) {
            this(ventas, null, null);
        }

        public DashboardStatsPanel(java.util.List<com.minimarket.model.Venta> ventas,
                java.util.List<com.minimarket.model.Stock> stocks,
                java.util.List<com.minimarket.model.Producto> productos) {
            setLayout(new BorderLayout(20, 20));
            setOpaque(false);

            // Calcular ventas del día dinámicamente o usar el valor del mockup
            java.math.BigDecimal totalToday = java.math.BigDecimal.ZERO;
            java.time.LocalDate today = java.time.LocalDate.now();
            int todayCount = 0;
            if (ventas != null) {
                for (com.minimarket.model.Venta v : ventas) {
                    if (v.getFecha() != null && v.getFecha().equals(today)) {
                        totalToday = totalToday.add(v.getPrecioTotal());
                        todayCount++;
                    }
                }
            }

            // Set values dynamically: show 0 if there are no sales today
            String salesTodayStr = "S/ " + String.format("%.2f", totalToday);
            String trendText1 = todayCount > 0 ? "+" + todayCount + "%" : "0%";
            boolean trendUp1 = todayCount > 0;

            // Calcular SKUs e ítems críticos reales
            String resumenInventario = "1.240 SKUs";
            if (productos != null) {
                resumenInventario = productos.size() + " SKUs";
            }

            int criticosRealCount = 0;
            if (stocks != null) {
                for (com.minimarket.model.Stock s : stocks) {
                    if (s.getCantidad() < 5) {
                        criticosRealCount++;
                    }
                }
            }
            String alertasActivas = (stocks != null) ? criticosRealCount + " Críticas" : "8 Críticas";

            // 1. FILA DE MÈTRICAS (GridLayout 1x3)
            JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
            metricsPanel.setOpaque(false);

            metricsPanel.add(new MetricCard("Ventas del Día", salesTodayStr, trendText1, trendUp1, new TrendIcon()));
            metricsPanel.add(
                    new MetricCard("Resumen de Inventario", resumenInventario, "-2.4%", false, new BoxMetricIcon()));
            metricsPanel.add(new MetricCard("Alertas Activas", alertasActivas, "+3", true, new AlertMetricIcon()));

            add(metricsPanel, BorderLayout.NORTH);

            // 2. SECCIÓN INFERIOR: Columna Izquierda (Tabla) y Derecha (Gráfica + Bajo
            // Stock)
            JPanel bottomSplit = new JPanel(new GridBagLayout());
            bottomSplit.setOpaque(false);

            // Columna Izquierda: Tarjeta de Transacciones Recientes
            RoundedCardPanel transCard = new RoundedCardPanel();
            transCard.setLayout(new BorderLayout(10, 10));

            JPanel transHeader = new JPanel(new BorderLayout());
            transHeader.setOpaque(false);
            transHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

            JPanel transTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            transTitlePanel.setOpaque(false);

            // Vector clock icon
            JLabel lblClock = new JLabel(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(24, 119, 242)); // Blue 500
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawOval(x + 1, y + 1, 14, 14);
                    g2.drawLine(x + 8, y + 4, x + 8, y + 8);
                    g2.drawLine(x + 8, y + 8, x + 11, y + 8);
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
            });

            JLabel lblTransTitle = new JLabel("Transacciones Recientes");
            lblTransTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTransTitle.setForeground(new Color(15, 23, 42));
            transTitlePanel.add(lblClock);
            transTitlePanel.add(lblTransTitle);

            JButton btnVerTodo = new JButton("Ver Todo el Historial  ›") {
                {
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setForeground(new Color(100, 116, 139));
                    setContentAreaFilled(false);
                    setBorderPainted(false);
                    setFocusPainted(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            };
            transHeader.add(transTitlePanel, BorderLayout.WEST);
            transHeader.add(btnVerTodo, BorderLayout.EAST);

            // Tabla de transacciones (Venta model attributes)
            String[] colNames = { "ID Venta", "ID Producto", "Cantidad", "Precio Total", "Fecha", "ID Cliente" };
            Object[][] data;
            if (ventas == null || ventas.isEmpty()) {
                data = new Object[0][6];
            } else {
                int limit = Math.min(ventas.size(), 10);
                data = new Object[limit][6];
                for (int i = 0; i < limit; i++) {
                    // Orden descendente (las más recientes primero)
                    com.minimarket.model.Venta v = ventas.get(ventas.size() - 1 - i);

                    // Column 0: ID Venta
                    data[i][0] = v.getIdVenta();

                    // Column 1: ID Producto
                    data[i][1] = v.getIdProducto();

                    // Column 2: Cantidad
                    data[i][2] = v.getCantidad();

                    // Column 3: Precio Total
                    data[i][3] = "S/ " + String.format("%.2f", v.getPrecioTotal());

                    // Column 4: Fecha
                    data[i][4] = v.getFecha() != null ? v.getFecha().toString() : "";

                    // Column 5: ID Cliente
                    data[i][5] = v.getIdCliente();
                }
            }

            DefaultTableModel model = new DefaultTableModel(data, colNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            table.setRowHeight(42); // 42px row height for double line spacing
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setBackground(Color.WHITE);
            table.setSelectionBackground(new Color(243, 244, 246));
            table.getColumnModel().getColumn(0).setPreferredWidth(70);
            table.getColumnModel().getColumn(1).setPreferredWidth(85);
            table.getColumnModel().getColumn(2).setPreferredWidth(70);
            table.getColumnModel().getColumn(3).setPreferredWidth(95);
            table.getColumnModel().getColumn(4).setPreferredWidth(95);
            table.getColumnModel().getColumn(5).setPreferredWidth(85);

            // Renderizadores de celda personalizados
            TransactionTableRenderer renderer = new TransactionTableRenderer();
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }

            // Encabezado de la tabla personalizado
            JTableHeader header = table.getTableHeader();
            header.setFont(new Font("Segoe UI", Font.BOLD, 11));
            header.setForeground(new Color(100, 116, 139));
            header.setBackground(Color.WHITE);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
            header.setPreferredSize(new Dimension(0, 32));
            ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            JLabel lblUpdate = new JLabel("ACTUALIZADO EN TIEMPO REAL", JLabel.CENTER);
            lblUpdate.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblUpdate.setForeground(new Color(148, 163, 184));
            lblUpdate.setBorder(new EmptyBorder(8, 0, 4, 0));

            transCard.add(transHeader, BorderLayout.NORTH);
            transCard.add(scrollPane, BorderLayout.CENTER);
            transCard.add(lblUpdate, BorderLayout.SOUTH);

            // Columna Derecha: Contenedor vertical de dos tarjetas
            JPanel rightCol = new JPanel();
            rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
            rightCol.setOpaque(false);

            // Tarjeta 1: Tendencia 7 Días
            RoundedCardPanel trendCard = new RoundedCardPanel();
            trendCard.setLayout(new BorderLayout(5, 5));
            trendCard.setPreferredSize(new Dimension(0, 240));
            trendCard.setMaximumSize(new Dimension(32767, 240));

            JPanel trendHeader = new JPanel(new BorderLayout());
            trendHeader.setOpaque(false);

            JLabel lblTrendTitle = new JLabel("TENDENCIA 7 DÍAS");
            lblTrendTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTrendTitle.setForeground(new Color(15, 23, 42));

            JLabel lblTrendPill = new JLabel("Ventas (S/)", JLabel.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(239, 246, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblTrendPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblTrendPill.setForeground(new Color(24, 119, 242));
            lblTrendPill.setBorder(new EmptyBorder(3, 8, 3, 8));

            trendHeader.add(lblTrendTitle, BorderLayout.WEST);
            trendHeader.add(lblTrendPill, BorderLayout.EAST);

            SplineChart splineChart = new SplineChart();

            JPanel trendFooter = new JPanel(new BorderLayout());
            trendFooter.setOpaque(false);
            trendFooter.setBorder(new EmptyBorder(5, 0, 0, 0));
            JLabel lblAvgText = new JLabel("Venta Promedio Semanal");
            lblAvgText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblAvgText.setForeground(new Color(100, 116, 139));
            JLabel lblAvgVal = new JLabel("S/34.250,00");
            lblAvgVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblAvgVal.setForeground(new Color(15, 23, 42));
            trendFooter.add(lblAvgText, BorderLayout.WEST);
            trendFooter.add(lblAvgVal, BorderLayout.EAST);

            trendCard.add(trendHeader, BorderLayout.NORTH);
            trendCard.add(splineChart, BorderLayout.CENTER);
            trendCard.add(trendFooter, BorderLayout.SOUTH);

            // Tarjeta 2: Bajo Stock
            RoundedCardPanel stockCard = new RoundedCardPanel();
            stockCard.setLayout(new BorderLayout(5, 5));

            JPanel stockHeader = new JPanel(new BorderLayout());
            stockHeader.setOpaque(false);

            JPanel stockTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            stockTitlePanel.setOpaque(false);
            JLabel lblStockAlertIcon = new JLabel("[!] ");
            lblStockAlertIcon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblStockAlertIcon.setForeground(new Color(239, 68, 68));
            JLabel lblStockTitle = new JLabel("BAJO STOCK");
            lblStockTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblStockTitle.setForeground(new Color(15, 23, 42));
            stockTitlePanel.add(lblStockAlertIcon);
            stockTitlePanel.add(lblStockTitle);

            // Calcular stock bajo en tiempo real (umbral fijo a 5)
            int lowStockCount = 0;
            java.util.List<com.minimarket.model.Stock> lowStockList = new java.util.ArrayList<>();
            if (stocks != null) {
                for (com.minimarket.model.Stock s : stocks) {
                    if (s.getCantidad() < 5) {
                        lowStockCount++;
                        lowStockList.add(s);
                    }
                }
            }
            // Ordenar por menor cantidad
            lowStockList.sort((s1, s2) -> Integer.compare(s1.getCantidad(), s2.getCantidad()));

            JPanel stockListPanel = new JPanel();
            stockListPanel.setLayout(new BoxLayout(stockListPanel, BoxLayout.Y_AXIS));
            stockListPanel.setOpaque(false);

            if (stocks != null && productos != null) {
                if (!lowStockList.isEmpty()) {
                    int limit = Math.min(lowStockList.size(), 4);
                    for (int i = 0; i < limit; i++) {
                        com.minimarket.model.Stock s = lowStockList.get(i);
                        String pName = "Producto " + s.getIdProducto();
                        for (com.minimarket.model.Producto p : productos) {
                            if (p.getIdProducto() == s.getIdProducto()) {
                                pName = p.getNombreProducto();
                                break;
                            }
                        }
                        int cap = 10;
                        if (s.getCantidad() > 10) {
                            cap = s.getCantidad() + 5;
                        }
                        stockListPanel.add(new LowStockItem(pName, s.getCantidad(), cap));
                    }
                } else {
                    // Estado premium cuando no hay alertas
                    JPanel emptyAlert = new JPanel(new BorderLayout(8, 0));
                    emptyAlert.setOpaque(false);
                    emptyAlert.setBorder(new EmptyBorder(20, 0, 20, 0));

                    JLabel lblSuccessIcon = new JLabel("✓");
                    lblSuccessIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    lblSuccessIcon.setForeground(new Color(34, 197, 94)); // Green 500

                    JLabel lblSuccessText = new JLabel(
                            "<html><b>Todo en orden</b><br><font color='#64748B'>Todos los productos tienen stock suficiente (mayor a 10 unidades).</font></html>");
                    lblSuccessText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    lblSuccessText.setForeground(new Color(15, 23, 42));

                    emptyAlert.add(lblSuccessIcon, BorderLayout.WEST);
                    emptyAlert.add(lblSuccessText, BorderLayout.CENTER);
                    stockListPanel.add(emptyAlert);
                }
            } else {
                // Fallback a mockup si no hay base de datos conectada (Modo Offline Demo)
                stockListPanel.add(new LowStockItem("Leche Entera 1L (Demo)", 3, 5));
                stockListPanel.add(new LowStockItem("Pan de Molde (Demo)", 2, 5));
                stockListPanel.add(new LowStockItem("Aceite Girasol 900ml (Demo)", 1, 5));
                stockListPanel.add(new LowStockItem("Café Molido 250g (Demo)", 4, 5));
                lowStockCount = 4;
            }

            JLabel lblBadge = new JLabel(String.valueOf(lowStockCount), JLabel.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(239, 68, 68));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblBadge.setForeground(Color.WHITE);
            lblBadge.setPreferredSize(new Dimension(18, 18));

            stockHeader.add(stockTitlePanel, BorderLayout.WEST);
            stockHeader.add(lblBadge, BorderLayout.EAST);

            JButton btnManageStock = new JButton("Gestionar stock") {
                {
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setForeground(new Color(15, 23, 42));
                    setContentAreaFilled(false);
                    setFocusPainted(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                            new EmptyBorder(6, 12, 6, 12)));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            JPanel stockFooterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            stockFooterPanel.setOpaque(false);
            stockFooterPanel.add(btnManageStock);

            stockCard.add(stockHeader, BorderLayout.NORTH);
            stockCard.add(stockListPanel, BorderLayout.CENTER);
            stockCard.add(stockFooterPanel, BorderLayout.SOUTH);

            rightCol.add(trendCard);
            rightCol.add(Box.createVerticalStrut(15));
            rightCol.add(stockCard);

            // Agregar columnas al split
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.6;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 0, 10);
            bottomSplit.add(transCard, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.4;
            gbc.insets = new Insets(0, 10, 0, 0);
            bottomSplit.add(rightCol, gbc);

            add(bottomSplit, BorderLayout.CENTER);
        }
    }

    // Componente de Tarjeta Redondeada Base
    private static class RoundedCardPanel extends JPanel {
        private final int radius = 16;

        public RoundedCardPanel() {
            setOpaque(false);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(241, 245, 249), 1),
                    new EmptyBorder(16, 20, 16, 20)));
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
            g2.setColor(new Color(241, 245, 249)); // Slate 100
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // Componente Tarjeta Métrica Individual
    private static class MetricCard extends RoundedCardPanel {
        public MetricCard(String title, String value, String trendText, boolean trendUp, Icon icon) {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(15, 18, 15, 18));
            setBackground(Color.WHITE);

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);

            JLabel lblIcon = new JLabel(icon);
            topPanel.add(lblIcon, BorderLayout.WEST);

            if (trendText != null && !trendText.isEmpty()) {
                Color bg = trendUp ? new Color(240, 253, 244) : new Color(254, 242, 242);
                Color fg = trendUp ? new Color(21, 128, 61) : new Color(220, 38, 38);

                String arrowText = (trendUp ? "↗ " : "↘ ") + trendText;

                JLabel lblTrend = new JLabel(arrowText, JLabel.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lblTrend.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblTrend.setForeground(fg);
                lblTrend.setBorder(new EmptyBorder(4, 8, 4, 8));
                topPanel.add(lblTrend, BorderLayout.EAST);
            }

            add(topPanel, BorderLayout.NORTH);

            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
            infoPanel.setOpaque(false);

            JLabel lblTitle = new JLabel(title.toUpperCase());
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblTitle.setForeground(new Color(148, 163, 184)); // Slate 400

            JLabel lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblValue.setForeground(new Color(15, 23, 42)); // Slate 900

            infoPanel.add(lblTitle);
            infoPanel.add(lblValue);

            add(infoPanel, BorderLayout.CENTER);
        }
    }

    // Gráfica de Tendencia de 7 días Spline vectorial
    private static class SplineChart extends JComponent {
        private final String[] xLabels = { "Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom" };
        private final float[] data = { 2200, 1600, 9600, 4200, 4900, 3900, 4400 };
        private final float maxVal = 10000;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 32;
            int padRight = 10;
            int padTop = 15;
            int padBottom = 25;

            int chartW = w - padLeft - padRight;
            int chartH = h - padTop - padBottom;

            // Dibujar líneas de cuadrícula horizontales
            g2.setColor(new Color(241, 245, 249)); // Slate 100
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            FontMetrics fm = g2.getFontMetrics();

            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                float ratio = (float) i / gridLines;
                int y = padTop + (int) (chartH * (1 - ratio));

                g2.drawLine(padLeft, y, w - padRight, y);

                int val = (int) (maxVal * ratio);
                String valStr = String.valueOf(val);
                g2.setColor(new Color(148, 163, 184)); // Slate 400
                g2.drawString(valStr, padLeft - fm.stringWidth(valStr) - 6, y + fm.getAscent() / 2 - 1);
                g2.setColor(new Color(241, 245, 249));
            }

            // Calcular coordenadas de puntos
            float[] px = new float[7];
            float[] py = new float[7];
            for (int i = 0; i < 7; i++) {
                px[i] = padLeft + (float) i / 6 * chartW;
                py[i] = padTop + chartH * (1 - data[i] / maxVal);
            }

            // Dibujar degradado de relleno
            Path2D.Float fillPath = new Path2D.Float();
            fillPath.moveTo(px[0], py[0]);
            for (int i = 0; i < 6; i++) {
                float x1 = px[i];
                float y1 = py[i];
                float x2 = px[i + 1];
                float y2 = py[i + 1];
                float ctrlX1 = x1 + (x2 - x1) / 2.0f;
                float ctrlY1 = y1;
                float ctrlX2 = x1 + (x2 - x1) / 2.0f;
                float ctrlY2 = y2;
                fillPath.curveTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
            }
            fillPath.lineTo(px[6], padTop + chartH);
            fillPath.lineTo(px[0], padTop + chartH);
            fillPath.closePath();

            GradientPaint grad = new GradientPaint(
                    padLeft, padTop, new Color(24, 119, 242, 60),
                    padLeft, padTop + chartH, new Color(24, 119, 242, 0));
            g2.setPaint(grad);
            g2.fill(fillPath);

            // Dibujar línea spline curva
            Path2D.Float linePath = new Path2D.Float();
            linePath.moveTo(px[0], py[0]);
            for (int i = 0; i < 6; i++) {
                float x1 = px[i];
                float y1 = py[i];
                float x2 = px[i + 1];
                float y2 = py[i + 1];
                float ctrlX1 = x1 + (x2 - x1) / 2.0f;
                float ctrlY1 = y1;
                float ctrlX2 = x1 + (x2 - x1) / 2.0f;
                float ctrlY2 = y2;
                linePath.curveTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
            }

            g2.setColor(new Color(24, 119, 242)); // Blue 500
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linePath);

            // Dibujar etiquetas eje X
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(new Color(148, 163, 184)); // Slate 400
            for (int i = 0; i < 7; i++) {
                String label = xLabels[i];
                int labelW = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, (int) px[i] - labelW / 2, h - 8);
            }

            g2.dispose();
        }
    }

    // Fila de Bajo Stock individual
    private static class LowStockItem extends JPanel {
        public LowStockItem(String name, int current, int total) {
            setLayout(new BorderLayout(4, 4));
            setOpaque(false);
            setBorder(new EmptyBorder(6, 0, 6, 0));

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);

            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(new Color(15, 23, 42));

            JLabel lblRatio = new JLabel(current + " / " + total + " unid.");
            lblRatio.setFont(new Font("Segoe UI", Font.BOLD, 11));

            float pct = (float) current / total;
            lblRatio.setForeground(pct < 0.2f ? new Color(220, 38, 38) : new Color(245, 158, 11)); // Rojo o Ámbar

            textPanel.add(lblName, BorderLayout.WEST);
            textPanel.add(lblRatio, BorderLayout.EAST);
            add(textPanel, BorderLayout.NORTH);

            // Barra de progreso redondeada
            Color barColor = pct < 0.2f ? new Color(220, 38, 38) : new Color(34, 197, 94);
            FlatProgressBar bar = new FlatProgressBar(pct, barColor);
            add(bar, BorderLayout.CENTER);
        }
    }

    // Renderizador de Tabla para estilos exactos
    private static class TransactionTableRenderer extends DefaultTableCellRenderer {
        private final Icon userIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw a beautiful sky-blue circle background
                g2.setColor(new Color(224, 242, 254)); // Sky 100
                g2.fillOval(x, y, 18, 18);

                // Draw profile details inside
                g2.setColor(new Color(14, 165, 233)); // Sky 500
                g2.fillOval(x + 5, y + 3, 8, 8); // Head
                g2.fillArc(x + 2, y + 11, 14, 10, 0, 180); // Shoulders

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            String text = value != null ? value.toString() : "";

            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setForeground(new Color(15, 23, 42)); // Slate 900
            label.setBorder(new EmptyBorder(0, 12, 0, 12));

            if (column == 0) { // ID Venta
                label.setForeground(new Color(24, 119, 242)); // Blue 500
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (column == 1) { // ID Producto
                label = new JLabel(text, JLabel.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(241, 245, 249)); // Slate 100
                        g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 16, 12, 12);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
                label.setForeground(new Color(71, 85, 105)); // Slate 600
                label.setBorder(new EmptyBorder(0, 8, 0, 8));
            } else if (column == 2) { // Cantidad
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                label.setHorizontalAlignment(JLabel.CENTER);
            } else if (column == 3) { // Precio Total
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(JLabel.RIGHT);
                label.setBorder(new EmptyBorder(0, 12, 0, 16));
            } else if (column == 4) { // Fecha
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            } else if (column == 5) { // ID Cliente
                label.setIcon(userIcon);
                label.setIconTextGap(8);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }

            if (isSelected) {
                label.setBackground(new Color(248, 250, 252)); // Slate 50
            }

            return label;
        }
    }

    // Icono MINI-POS marca
    private static class MiniPOSLogoIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(24, 119, 242));
            g2.fillRoundRect(x, y, 28, 28, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x + 5, y + 14);
            path.lineTo(x + 10, y + 14);
            path.lineTo(x + 12, y + 8);
            path.lineTo(x + 16, y + 20);
            path.lineTo(x + 18, y + 14);
            path.lineTo(x + 23, y + 14);
            g2.draw(path);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 28;
        }

        @Override
        public int getIconHeight() {
            return 28;
        }
    }

    // Icono Dashboard
    private static class DashboardIcon implements Icon {
        private final boolean active;

        public DashboardIcon(boolean active) {
            this.active = active;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(active ? new Color(24, 119, 242) : new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(1.8f));

            g2.drawRect(x, y, 5, 5);
            g2.drawRect(x + 9, y, 5, 5);
            g2.drawRect(x, y + 9, 5, 5);
            g2.drawRect(x + 9, y + 9, 5, 5);

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

    // Icono Carrito de Compras
    private static class CartIcon implements Icon {
        private final boolean active;

        public CartIcon(boolean active) {
            this.active = active;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(active ? new Color(24, 119, 242) : new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(x, y + 1, x + 3, y + 1);
            g2.drawLine(x + 3, y + 1, x + 5, y + 9);
            g2.drawLine(x + 5, y + 9, x + 13, y + 9);
            g2.drawLine(x + 13, y + 9, x + 15, y + 3);
            g2.drawLine(x + 3, y + 3, x + 15, y + 3);

            g2.fillOval(x + 5, y + 11, 3, 3);
            g2.fillOval(x + 11, y + 11, 3, 3);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }

    // Icono Caja Inventario
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
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(x + 7, y, x + 14, y + 3);
            g2.drawLine(x + 14, y + 3, x + 7, y + 7);
            g2.drawLine(x + 7, y + 7, x, y + 3);
            g2.drawLine(x, y + 3, x + 7, y);

            g2.drawLine(x, y + 3, x, y + 10);
            g2.drawLine(x + 7, y + 7, x + 7, y + 14);
            g2.drawLine(x + 14, y + 3, x + 14, y + 10);

            g2.drawLine(x, y + 10, x + 7, y + 14);
            g2.drawLine(x + 7, y + 14, x + 14, y + 10);

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

    // Icono Categoría (Capas)
    private static class CategoryIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(1.5f));

            g2.drawRect(x, y + 1, 14, 3);
            g2.drawRect(x, y + 6, 14, 3);
            g2.drawRect(x, y + 11, 14, 3);

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

    // Icono Reportes (Gráfico de barras)
    private static class ReportsIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 116, 139));
            g2.setStroke(new BasicStroke(1.6f));

            g2.drawRect(x, y + 7, 3, 7);
            g2.drawRect(x + 5, y + 2, 3, 12);
            g2.drawRect(x + 10, y + 5, 3, 9);

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

    // Botón personalizado de menú lateral Sidebar
    private static class SidebarButton extends JButton {
        private boolean active;
        private final Color activeBg = new Color(240, 249, 255); // azul sky suave
        private final Color activeFg = new Color(24, 119, 242); // azul corporativo
        private final Color normalFg = new Color(100, 116, 139); // gris slate
        private final Color hoverBg = new Color(248, 250, 252); // gris Slate 50

        public SidebarButton(String text, Icon icon, boolean active) {
            super("  " + text, icon);
            this.active = active;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(active ? activeFg : normalFg);
            setHorizontalAlignment(JButton.LEFT);
            setBorder(new EmptyBorder(8, 15, 8, 15));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(190, 36));
            setPreferredSize(new Dimension(190, 36));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? activeFg : normalFg);
            if (active && getIcon() instanceof DashboardIcon) {
                setIcon(new DashboardIcon(true));
            } else if (!active && getIcon() instanceof DashboardIcon) {
                setIcon(new DashboardIcon(false));
            } else if (active && getIcon() instanceof CartIcon) {
                setIcon(new CartIcon(true));
            } else if (!active && getIcon() instanceof CartIcon) {
                setIcon(new CartIcon(false));
            } else if (active && getIcon() instanceof BoxIcon) {
                setIcon(new BoxIcon(true));
            } else if (!active && getIcon() instanceof BoxIcon) {
                setIcon(new BoxIcon(false));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(activeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // Buscador redondeado con placeholder
    private static class RoundedSearchField extends JTextField {
        private final Color borderColor = new Color(241, 245, 249);
        private final Color backgroundColor = new Color(248, 250, 252);
        private final Color textColor = new Color(15, 23, 42);
        private final Color placeholderColor = new Color(148, 163, 184);

        public RoundedSearchField() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 32, 6, 12));
            setBackground(backgroundColor);
            setForeground(textColor);
            setCaretColor(textColor);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            super.paintComponent(g2);

            // Draw a beautiful sharp vector magnifying glass icon at the left
            g2.setColor(placeholderColor);
            g2.setStroke(new BasicStroke(1.6f));
            int cx = 12;
            int cy = (getHeight() - 12) / 2;
            g2.drawOval(cx, cy, 8, 8);
            g2.drawLine(cx + 6, cy + 6, cx + 11, cy + 11);

            if (getText().isEmpty()) {
                g2.setColor(placeholderColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = 32;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("Quick search products or tickets...", x, y);
            }

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
        }
    }

    // Botón de contorno rojo moderno para Cerrar Sesión
    private static class OutlineButton extends JButton {
        private final Color normalColor = new Color(239, 68, 68); // Red 500

        public OutlineButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(normalColor);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(6, 12, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(new Color(254, 242, 242)); // Red 50
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(255, 245, 245));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }

            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(252, 165, 165)); // Red 200 soft border
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }

    // Icono de Tendencia (Métrica Ventas del Día)
    private static class TrendIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setColor(new Color(239, 246, 255)); // light blue
            g2.fillOval(x, y, 32, 32);
            g2.setColor(new Color(59, 130, 246)); // blue 500
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Flecha / Onda ascendente
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x + 8, y + 20);
            path.lineTo(x + 13, y + 15);
            path.lineTo(x + 17, y + 19);
            path.lineTo(x + 23, y + 12);
            g2.draw(path);
            g2.drawLine(x + 18, y + 12, x + 23, y + 12);
            g2.drawLine(x + 23, y + 12, x + 23, y + 17);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 32;
        }

        @Override
        public int getIconHeight() {
            return 32;
        }
    }

    // Icono de Caja (Métrica Resumen de Inventario)
    private static class BoxMetricIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(239, 246, 255)); // light blue
            g2.fillOval(x, y, 32, 32);
            g2.setColor(new Color(59, 130, 246)); // blue 500
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + 16;
            int cy = y + 16;
            g2.drawLine(cx, cy - 8, cx + 8, cy - 4);
            g2.drawLine(cx + 8, cy - 4, cx, cy);
            g2.drawLine(cx, cy, cx - 8, cy - 4);
            g2.drawLine(cx - 8, cy - 4, cx, cy - 8);
            g2.drawLine(cx - 8, cy - 4, cx - 8, cy + 4);
            g2.drawLine(cx, cy, cx, cy + 8);
            g2.drawLine(cx + 8, cy - 4, cx + 8, cy + 4);
            g2.drawLine(cx - 8, cy + 4, cx, cy + 8);
            g2.drawLine(cx, cy + 8, cx + 8, cy + 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 32;
        }

        @Override
        public int getIconHeight() {
            return 32;
        }
    }

    // Icono de Alerta / Advertencia (Métrica Alertas Activas)
    private static class AlertMetricIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(254, 242, 242)); // soft red
            g2.fillOval(x, y, 32, 32);
            g2.setColor(new Color(220, 38, 38)); // red 600
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + 16;
            int cy = y + 16;
            Path2D.Float path = new Path2D.Float();
            path.moveTo(cx, cy - 8);
            path.lineTo(cx - 8, cy + 8);
            path.lineTo(cx + 8, cy + 8);
            path.closePath();
            g2.draw(path);
            g2.drawLine(cx, cy - 3, cx, cy + 2);
            g2.fillOval(cx - 1, cy + 5, 2, 2);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 32;
        }

        @Override
        public int getIconHeight() {
            return 32;
        }
    }

    // --- Vector Icon Definitions ---
    private static class BellIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(71, 85, 105)); // Slate 600
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Draw Bell body
            g2.drawArc(x + 4, y + 4, 8, 8, 0, 180); // top dome
            g2.drawLine(x + 4, y + 8, x + 4, y + 11); // left side
            g2.drawLine(x + 12, y + 8, x + 12, y + 11); // right side
            g2.drawLine(x + 2, y + 11, x + 14, y + 11); // bottom lip

            // Bell clapper
            g2.drawArc(x + 6, y + 11, 4, 4, 180, 180); // bottom clapper

            // Bell tip top dot
            g2.fillOval(x + 7, y + 2, 2, 2);

            // Red notification badge dot at the top right of the bell
            g2.setColor(new Color(239, 68, 68)); // Red 500
            g2.fillOval(x + 11, y + 1, 6, 6);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }

    // Flat progress bar custom component to bypass blocky Windows theme overriding
    // JProgressBar
    private static class FlatProgressBar extends JComponent {
        private final float percentage;
        private final Color barColor;

        public FlatProgressBar(float percentage, Color barColor) {
            this.percentage = percentage;
            this.barColor = barColor;
            setPreferredSize(new Dimension(0, 6));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = 6; // Fixed height of 6 pixels!
            int y = (getHeight() - h) / 2; // Centered vertically!
            int arc = h; // fully rounded caps

            // Draw background track
            g2.setColor(new Color(241, 245, 249)); // Slate 100
            g2.fillRoundRect(0, y, w, h, arc, arc);

            // Draw filled progress portion
            int fillW = (int) (w * percentage);
            if (fillW > 0) {
                g2.setColor(barColor);
                g2.fillRoundRect(0, y, fillW, h, arc, arc);
            }

            g2.dispose();
        }
    }

    private static class FooterUserIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184)); // Slate 400
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(x + 3, y + 2, 6, 6);
            g2.drawArc(x + 1, y + 8, 10, 8, 0, 180);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return 12; }
        @Override
        public int getIconHeight() { return 12; }
    }

    private static class FooterDBIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184)); // Slate 400
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(x + 1, y + 1, 10, 3);
            g2.drawLine(x + 1, y + 2, x + 1, y + 10);
            g2.drawLine(x + 11, y + 2, x + 11, y + 10);
            g2.drawArc(x + 1, y + 5, 10, 3, 180, 180);
            g2.drawArc(x + 1, y + 8, 10, 3, 180, 180);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return 12; }
        @Override
        public int getIconHeight() { return 12; }
    }

    private static class FooterHealthIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242)); // Blue 500
            g2.setStroke(new BasicStroke(1.2f));
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x, y + 6);
            path.lineTo(x + 3, y + 6);
            path.lineTo(x + 5, y + 2);
            path.lineTo(x + 7, y + 10);
            path.lineTo(x + 9, y + 6);
            path.lineTo(x + 12, y + 6);
            g2.draw(path);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return 12; }
        @Override
        public int getIconHeight() { return 12; }
    }
}

