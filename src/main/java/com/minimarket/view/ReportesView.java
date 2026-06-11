package com.minimarket.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportesView extends JPanel {

    // Header buttons
    private RangeButton btnHoy;
    private RangeButton btn7D;
    private RangeButton btn30D;
    private RangeButton btn1A;
    private JButton btnRangoPersonalizado;
    private JButton btnCompartir;
    private JButton btnExportarReporte;
    private JButton btnVerLista;
    private boolean customRangeActive = false;

    // KPI Values
    private JLabel lblVentasTotalesVal;
    private JLabel lblTransaccionesVal;
    private JLabel lblTicketPromedioVal;
    private JLabel lblMargenVal;

    // Charts
    private ComparativaVentasChart chartVentasMensuales;
    private VentasCategoriaChart chartVentasCategoria;

    // Top Products Table
    private JTable tblTopProducts;
    private DefaultTableModel tableModel;

    public ReportesView() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 4, 0, 4));

        // 1. TOP HEADER ACTIONS PANEL
        JPanel pnlHeaderActions = new JPanel(new BorderLayout());
        pnlHeaderActions.setOpaque(false);

        // Time Range Buttons (Left)
        JPanel pnlTimeRange = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlTimeRange.setOpaque(false);

        btnHoy = new RangeButton("Hoy", false);
        btn7D = new RangeButton("7D", false);
        btn30D = new RangeButton("30D", true); // Default selected
        btn1A = new RangeButton("1A", false);

        btnRangoPersonalizado = new JButton("📅 Rango Personalizado") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (customRangeActive) {
                    g2.setColor(new Color(239, 246, 255)); // Light Blue select
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(59, 130, 246)); // Blue border
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
        btnRangoPersonalizado.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRangoPersonalizado.setForeground(new Color(71, 85, 105));
        btnRangoPersonalizado.setContentAreaFilled(false);
        btnRangoPersonalizado.setFocusPainted(false);
        btnRangoPersonalizado.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnRangoPersonalizado.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlTimeRange.add(btnHoy);
        pnlTimeRange.add(btn7D);
        pnlTimeRange.add(btn30D);
        pnlTimeRange.add(btn1A);
        pnlTimeRange.add(Box.createHorizontalStrut(6));
        pnlTimeRange.add(btnRangoPersonalizado);

        // Export/Share Buttons (Right)
        JPanel pnlShareExport = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlShareExport.setOpaque(false);

        btnCompartir = new JButton("Compartir", new ShareIcon()) {
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
        btnCompartir.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCompartir.setForeground(new Color(71, 85, 105));
        btnCompartir.setContentAreaFilled(false);
        btnCompartir.setFocusPainted(false);
        btnCompartir.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnCompartir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCompartir.setIconTextGap(6);

        btnExportarReporte = new JButton("Exportar Reporte", new ExportIcon()) {
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
        btnExportarReporte.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnExportarReporte.setForeground(Color.WHITE);
        btnExportarReporte.setContentAreaFilled(false);
        btnExportarReporte.setFocusPainted(false);
        btnExportarReporte.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnExportarReporte.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportarReporte.setIconTextGap(6);

        pnlShareExport.add(btnCompartir);
        pnlShareExport.add(btnExportarReporte);

        pnlHeaderActions.add(pnlTimeRange, BorderLayout.WEST);
        pnlHeaderActions.add(pnlShareExport, BorderLayout.EAST);

        // 2. KPI CARDS PANEL (4 Columns)
        JPanel pnlKpis = new JPanel(new GridLayout(1, 4, 16, 0));
        pnlKpis.setOpaque(false);
        pnlKpis.setPreferredSize(new Dimension(0, 110));

        lblVentasTotalesVal = new JLabel("S/0.00");
        lblTransaccionesVal = new JLabel("0");
        lblTicketPromedioVal = new JLabel("S/0.00");
        lblMargenVal = new JLabel("24.8%");

        pnlKpis.add(createKpiCard("VENTAS TOTALES", lblVentasTotalesVal, "+12.5% vs mes anterior", true, new DollarIcon()));
        pnlKpis.add(createKpiCard("TRANSACCIONES", lblTransaccionesVal, "+4.2% vs mes anterior", true, new BoxIcon()));
        pnlKpis.add(createKpiCard("TICKET PROMEDIO", lblTicketPromedioVal, "-1.2% vs mes anterior", false, new UsersIcon()));
        pnlKpis.add(createKpiCard("MARGEN OPERATIVO", lblMargenVal, "+0.8% vs mes anterior", true, new PercentIcon()));

        // 3. CHARTS PANEL (Center Row)
        JPanel pnlChartsRow = new JPanel(new GridBagLayout());
        pnlChartsRow.setOpaque(false);
        pnlChartsRow.setPreferredSize(new Dimension(0, 360));

        chartVentasMensuales = new ComparativaVentasChart();
        chartVentasCategoria = new VentasCategoriaChart();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Chart: Monthly Sales Comparison (65%)
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 16);
        pnlChartsRow.add(chartVentasMensuales, gbc);

        // Right Chart: Category Distribution (35%)
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        pnlChartsRow.add(chartVentasCategoria, gbc);

        // 4. BOTTOM TOP PRODUCTS PANEL
        RoundedPanel pnlTopProducts = new RoundedPanel(12);
        pnlTopProducts.setBackground(Color.WHITE);
        pnlTopProducts.setLayout(new BorderLayout(0, 14));
        pnlTopProducts.setBorder(new EmptyBorder(20, 24, 20, 24));
        pnlTopProducts.setPreferredSize(new Dimension(0, 340));

        JPanel pnlTableHead = new JPanel(new BorderLayout());
        pnlTableHead.setOpaque(false);

        JPanel pnlTableTitleWrapper = new JPanel();
        pnlTableTitleWrapper.setLayout(new BoxLayout(pnlTableTitleWrapper, BoxLayout.Y_AXIS));
        pnlTableTitleWrapper.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Top 10 Productos Más Vendidos");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(new Color(15, 23, 42));

        JLabel lblTableSubtitle = new JLabel("Análisis de rendimiento individual por volumen e ingresos");
        lblTableSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTableSubtitle.setForeground(new Color(100, 116, 139));

        pnlTableTitleWrapper.add(lblTableTitle);
        pnlTableTitleWrapper.add(Box.createVerticalStrut(2));
        pnlTableTitleWrapper.add(lblTableSubtitle);

        btnVerLista = new JButton("Ver Lista Completa") {
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
        btnVerLista.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnVerLista.setForeground(new Color(71, 85, 105));
        btnVerLista.setContentAreaFilled(false);
        btnVerLista.setFocusPainted(false);
        btnVerLista.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnVerLista.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlTableHead.add(pnlTableTitleWrapper, BorderLayout.WEST);
        pnlTableHead.add(btnVerLista, BorderLayout.EAST);

        // Tabla Top Products
        String[] columns = { "RANGO", "PRODUCTO", "UNIDADES VENDIDAS", "INGRESOS", "TENDENCIA" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTopProducts = new JTable(tableModel);
        tblTopProducts.setRowHeight(42);
        tblTopProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTopProducts.setBorder(null);
        tblTopProducts.setShowGrid(false);
        tblTopProducts.setOpaque(false);
        tblTopProducts.setBackground(Color.WHITE);

        // Header Styling
        JTableHeader header = tblTopProducts.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setForeground(new Color(148, 163, 184));
        header.setBackground(new Color(248, 250, 252));
        header.setDefaultRenderer(new CustomHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 36));

        // Column widths and custom cell renderers
        tblTopProducts.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblTopProducts.getColumnModel().getColumn(0).setCellRenderer(new RangeCellRenderer());
        tblTopProducts.getColumnModel().getColumn(1).setPreferredWidth(250);
        tblTopProducts.getColumnModel().getColumn(1).setCellRenderer(new BoldTextCellRenderer());
        tblTopProducts.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblTopProducts.getColumnModel().getColumn(2).setCellRenderer(new GreyTextCellRenderer(false));
        tblTopProducts.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblTopProducts.getColumnModel().getColumn(3).setCellRenderer(new BoldTextCellRenderer());
        tblTopProducts.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblTopProducts.getColumnModel().getColumn(4).setCellRenderer(new TendenciaCellRenderer());

        JScrollPane scrollTable = new JScrollPane(tblTopProducts);
        scrollTable.setBorder(null);
        scrollTable.getViewport().setBackground(Color.WHITE);

        pnlTopProducts.add(pnlTableHead, BorderLayout.NORTH);
        pnlTopProducts.add(scrollTable, BorderLayout.CENTER);

        // Add main components to the panel
        JPanel pnlScrollableContent = new JPanel();
        pnlScrollableContent.setLayout(new BoxLayout(pnlScrollableContent, BoxLayout.Y_AXIS));
        pnlScrollableContent.setOpaque(false);
        
        pnlScrollableContent.add(pnlHeaderActions);
        pnlScrollableContent.add(Box.createVerticalStrut(14));
        pnlScrollableContent.add(pnlKpis);
        pnlScrollableContent.add(Box.createVerticalStrut(16));
        pnlScrollableContent.add(pnlChartsRow);
        pnlScrollableContent.add(Box.createVerticalStrut(16));
        pnlScrollableContent.add(pnlTopProducts);

        JScrollPane mainScroll = new JScrollPane(pnlScrollableContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);

    }

    public void setRangeActive(String rangeName) {
        btnHoy.setActive("Hoy".equals(rangeName));
        btn7D.setActive("7D".equals(rangeName));
        btn30D.setActive("30D".equals(rangeName));
        btn1A.setActive("1A".equals(rangeName));
        customRangeActive = "Custom".equals(rangeName);
        btnRangoPersonalizado.setForeground(customRangeActive ? new Color(24, 119, 242) : new Color(71, 85, 105));
        btnRangoPersonalizado.repaint();
    }

    private RoundedPanel createKpiCard(String titleText, JLabel valueLabel, String trendText, boolean trendUp, Icon icon) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setOpaque(false);

        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTitle.setForeground(new Color(148, 163, 184));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(15, 23, 42));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTrend = new JLabel((trendUp ? "↗ " : "↘ ") + trendText);
        lblTrend.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTrend.setForeground(trendUp ? new Color(34, 197, 94) : new Color(239, 68, 68));
        lblTrend.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlLeft.add(lblTitle);
        pnlLeft.add(Box.createVerticalStrut(6));
        pnlLeft.add(valueLabel);
        pnlLeft.add(Box.createVerticalStrut(6));
        pnlLeft.add(lblTrend);

        // Icon Box wrapper (rounded circle background)
        JPanel pnlIconWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // Very light blue circle
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlIconWrapper.setPreferredSize(new Dimension(38, 38));
        pnlIconWrapper.setOpaque(false);
        pnlIconWrapper.add(new JLabel(icon));

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        pnlRight.setOpaque(false);
        pnlRight.add(pnlIconWrapper);

        card.add(pnlLeft, BorderLayout.CENTER);
        card.add(pnlRight, BorderLayout.EAST);

        return card;
    }

    // Setters for dynamic data
    public void setKpis(BigDecimal totalSales, int transactionCount, BigDecimal averageTicket) {
        lblVentasTotalesVal.setText("S/" + String.format("%,.2f", totalSales));
        lblTransaccionesVal.setText(String.format("%,d", transactionCount));
        lblTicketPromedioVal.setText("S/" + String.format("%,.2f", averageTicket));
    }

    public void setTopProductsTableData(List<Object[]> rows) {
        tableModel.setRowCount(0);
        for (Object[] r : rows) {
            tableModel.addRow(r);
        }
    }

    public void setMonthlyChartData(List<String> months, List<Integer> current, List<Integer> past) {
        chartVentasMensuales.setData(months, current, past);
    }

    public void setCategoryChartData(Map<String, Double> pcts, Map<String, Color> colors) {
        chartVentasCategoria.setData(pcts, colors);
    }

    public void addHoyListener(ActionListener l) { btnHoy.addActionListener(l); }
    public void add7DListener(ActionListener l) { btn7D.addActionListener(l); }
    public void add30DListener(ActionListener l) { btn30D.addActionListener(l); }
    public void add1AListener(ActionListener l) { btn1A.addActionListener(l); }
    public void addRangoPersonalizadoListener(ActionListener l) { btnRangoPersonalizado.addActionListener(l); }
    public void addCompartirListener(ActionListener l) { btnCompartir.addActionListener(l); }
    public void addExportarReporteListener(ActionListener l) { btnExportarReporte.addActionListener(l); }
    public void addVerListaCompletaListener(ActionListener l) { btnVerLista.addActionListener(l); }

    public void mostrarMensaje(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message,
                isError ? "Error" : "Éxito",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // --- CUSTOM CHART COMPONENTS ---

    private static class ComparativaVentasChart extends RoundedPanel {
        private final List<String> months = new ArrayList<>();
        private final List<Integer> currentYearValues = new ArrayList<>();
        private final List<Integer> pastYearValues = new ArrayList<>();

        public ComparativaVentasChart() {
            super(16);
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(22, 24, 22, 24));
        }

        public void setData(List<String> months, List<Integer> current, List<Integer> past) {
            this.months.clear();
            this.months.addAll(months);
            this.currentYearValues.clear();
            this.currentYearValues.addAll(current);
            this.pastYearValues.clear();
            this.pastYearValues.addAll(past);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Header titles
            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Comparativa de Ventas Mensuales", 24, 30);

            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString("Rendimiento actual comparado con el mismo período del año anterior", 24, 46);

            // Chart Legends
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(new Color(24, 119, 242));
            g2.fillOval(w - 180, 24, 8, 8);
            g2.drawString("Ventas 2026", w - 166, 31);

            g2.setColor(new Color(96, 165, 250));
            g2.fillOval(w - 94, 24, 8, 8);
            g2.drawString("Ventas 2025", w - 80, 31);

            // Chart drawing boundaries
            int chartX = 50;
            int chartY = 70;
            int chartW = w - 80;
            int chartH = h - 110;

            // Draw Y-Axis scale grids
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(new Color(241, 245, 249));
            int maxVal = 0;
            for (int val : currentYearValues) {
                if (val > maxVal) maxVal = val;
            }
            for (int val : pastYearValues) {
                if (val > maxVal) maxVal = val;
            }
            maxVal = Math.max(1000, (int) (Math.ceil(maxVal / 1000.0) * 1000.0));
            int step = maxVal / 4;

            for (int i = 0; i <= maxVal; i += step) {
                int yPos = chartY + chartH - (i * chartH / maxVal);
                g2.drawLine(chartX, yPos, chartX + chartW, yPos);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(i), chartX - 35, yPos + 4);
                g2.setColor(new Color(241, 245, 249));
            }

            // Draw monthly bars
            if (!months.isEmpty()) {
                int colWidth = chartW / months.size();
                int barWidth = Math.max(6, Math.min(18, colWidth / 4));

                for (int i = 0; i < months.size(); i++) {
                    int xCenter = chartX + i * colWidth + colWidth / 2;

                    // Past year bar (Ventas 2025)
                    int valPast = pastYearValues.get(i);
                    int hPast = valPast * chartH / maxVal;
                    g2.setColor(new Color(96, 165, 250)); // Soft light blue
                    g2.fillRoundRect(xCenter - barWidth - 2, chartY + chartH - hPast, barWidth, hPast, 4, 4);

                    // Current year bar (Ventas 2026)
                    int valCurrent = currentYearValues.get(i);
                    int hCurrent = valCurrent * chartH / maxVal;
                    g2.setColor(new Color(24, 119, 242)); // Solid vibrant blue
                    g2.fillRoundRect(xCenter + 2, chartY + chartH - hCurrent, barWidth, hCurrent, 4, 4);

                    // Month label
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(months.get(i), xCenter - 10, chartY + chartH + 18);
                }
            }

            g2.dispose();
        }
    }

    private static class VentasCategoriaChart extends RoundedPanel {
        private final List<String> categories = new ArrayList<>();
        private final List<Double> percentages = new ArrayList<>();
        private final List<Color> colors = new ArrayList<>();

        public VentasCategoriaChart() {
            super(16);
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(22, 24, 22, 24));
        }

        public void setData(Map<String, Double> pcts, Map<String, Color> colorsMap) {
            this.categories.clear();
            this.percentages.clear();
            this.colors.clear();

            for (Map.Entry<String, Double> entry : pcts.entrySet()) {
                this.categories.add(entry.getKey());
                this.percentages.add(entry.getValue());
                this.colors.add(colorsMap.getOrDefault(entry.getKey(), Color.GRAY));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Header titles
            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Ventas por Categoría", 24, 30);

            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString("Distribución porcentual de ingresos", 24, 46);

            // Donut position
            int donutSize = 140;
            int donutX = (w - donutSize) / 2;
            int donutY = 80;

            if (percentages.isEmpty()) {
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(18.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                g2.draw(new Arc2D.Double(donutX + 9, donutY + 9, donutSize - 18, donutSize - 18, 0, 360, Arc2D.OPEN));
                
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("Sin datos", donutX + (donutSize - g2.getFontMetrics().stringWidth("Sin datos")) / 2, donutY + (donutSize / 2) + 4);
            } else {
                // Draw Arc sections
                double currentAngle = 90.0; // Start at top center
                for (int i = 0; i < percentages.size(); i++) {
                    double pct = percentages.get(i);
                    double angleExtent = -(pct * 3.6); // 360 degrees total

                    g2.setColor(colors.get(i));
                    g2.setStroke(new BasicStroke(18.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                    g2.draw(new Arc2D.Double(donutX + 9, donutY + 9, donutSize - 18, donutSize - 18, currentAngle, angleExtent, Arc2D.OPEN));

                    currentAngle += angleExtent;
                }

                // Center donut sum text
                g2.setColor(new Color(15, 23, 42));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.drawString("100%", donutX + (donutSize - g2.getFontMetrics().stringWidth("100%")) / 2, donutY + (donutSize / 2) + 4);
            }

            // Draw legend entries at the bottom
            int legendY = 246;
            int colWidth = Math.max(1, w / Math.max(1, categories.size()));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));

            for (int i = 0; i < categories.size(); i++) {
                int legendX = i * colWidth + 8;
                g2.setColor(colors.get(i));
                g2.fillOval(legendX, legendY, 6, 6);
                g2.setColor(new Color(71, 85, 105));
                g2.drawString(categories.get(i) + " (" + String.format("%.0f", percentages.get(i)) + "%)", legendX + 10, legendY + 6);
            }

            g2.dispose();
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

    private static class RangeButton extends JButton {
        private boolean active;
        public RangeButton(String text, boolean active) {
            super(text);
            this.active = active;
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(active ? new Color(24, 119, 242) : new Color(148, 163, 184));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? new Color(24, 119, 242) : new Color(148, 163, 184));
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(new Color(239, 246, 255)); // Light Blue select
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(59, 130, 246)); // Blue border
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
    }

    // --- CUSTOM TABLE RENDERERS ---

    private static class CustomHeaderRenderer extends DefaultTableCellRenderer {
        public CustomHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(248, 250, 252));
            setForeground(new Color(100, 116, 139));
            setFont(new Font("Segoe UI", Font.BOLD, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            return this;
        }
    }

    private static class RangeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final int rank = (value instanceof Number) ? ((Number) value).intValue() : 0;
            JPanel pnl = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (rank > 0 && rank <= 3) {
                        g2.setColor(new Color(219, 234, 254)); // Soft blue circle badge
                        g2.fillOval((getWidth() - 20) / 2, (getHeight() - 20) / 2, 20, 20);
                        g2.setColor(new Color(24, 119, 242));
                        g2.drawOval((getWidth() - 20) / 2, (getHeight() - 20) / 2, 20, 20);
                    } else {
                        g2.setColor(new Color(241, 245, 249)); // Soft gray circle badge
                        g2.fillOval((getWidth() - 20) / 2, (getHeight() - 20) / 2, 20, 20);
                        g2.setColor(new Color(203, 213, 225));
                        g2.drawOval((getWidth() - 20) / 2, (getHeight() - 20) / 2, 20, 20);
                    }
                    g2.dispose();
                }
            };
            pnl.setOpaque(false);
            JLabel lbl = new JLabel(rank > 0 ? String.valueOf(rank) : "");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(rank > 0 && rank <= 3 ? new Color(24, 119, 242) : new Color(100, 116, 139));
            pnl.add(lbl);

            pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
            return pnl;
        }
    }

    private static class BoldTextCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(new Color(15, 23, 42));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class GreyTextCellRenderer extends DefaultTableCellRenderer {
        private final boolean rightAlign;

        public GreyTextCellRenderer(boolean rightAlign) {
            this.rightAlign = rightAlign;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(new Color(100, 116, 139));
            if (rightAlign) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class TendenciaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final String trend = value != null ? value.toString() : "";
            JPanel pnl = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if ("CRECIENTE".equalsIgnoreCase(trend)) {
                        g2.setColor(new Color(220, 252, 231)); // Soft green badge background
                        g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 6, 6);
                    } else if ("BAJANDO".equalsIgnoreCase(trend)) {
                        g2.setColor(new Color(254, 226, 226)); // Soft red badge background
                        g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 6, 6);
                    } else {
                        g2.setColor(new Color(241, 245, 249)); // Soft gray badge background
                        g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 6, 6);
                    }
                    g2.dispose();
                }
            };
            pnl.setOpaque(false);

            JLabel lbl = new JLabel();
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));

            if ("CRECIENTE".equalsIgnoreCase(trend)) {
                lbl.setText("↗ CRECIENTE");
                lbl.setForeground(new Color(21, 128, 61));
            } else if ("BAJANDO".equalsIgnoreCase(trend)) {
                lbl.setText("↘ BAJANDO");
                lbl.setForeground(new Color(220, 38, 38));
            } else {
                lbl.setText("● ESTABLE");
                lbl.setForeground(new Color(100, 116, 139));
            }

            pnl.add(lbl);
            pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
            return pnl;
        }
    }

    // --- MOCK SVG VECTOR DRAWING ICONS ---

    private static class ShareIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(1.5f));

            g2.drawOval(x + 11, y + 2, 4, 4);
            g2.drawOval(x + 2, y + 8, 4, 4);
            g2.drawOval(x + 11, y + 14, 4, 4);

            g2.drawLine(x + 5, y + 9, x + 12, y + 5);
            g2.drawLine(x + 5, y + 11, x + 12, y + 15);

            g2.dispose();
        }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
    }

    private static class ExportIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f));

            g2.drawLine(x + 8, y + 2, x + 8, y + 11);
            g2.drawLine(x + 4, y + 7, x + 8, y + 11);
            g2.drawLine(x + 12, y + 7, x + 8, y + 11);

            g2.drawLine(x + 3, y + 14, x + 13, y + 14);

            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    private static class DollarIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString("$", x + 6, y + 17);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    private static class BoxIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(1.8f));

            g2.drawRect(x + 2, y + 2, 14, 14);
            g2.drawLine(x + 2, y + 2, x + 16, y + 16);
            g2.drawLine(x + 16, y + 2, x + 2, y + 16);

            g2.dispose();
        }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
    }

    private static class UsersIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(1.5f));

            g2.drawOval(x + 6, y + 2, 6, 6);
            g2.drawArc(x + 2, y + 10, 14, 12, 0, 180);

            g2.drawOval(x + 1, y + 5, 4, 4);
            g2.drawArc(x - 2, y + 12, 10, 8, 0, 110);

            g2.dispose();
        }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
    }

    private static class PercentIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 119, 242));
            g2.setStroke(new BasicStroke(1.8f));

            g2.drawLine(x + 14, y + 2, x + 4, y + 14);
            g2.drawOval(x + 3, y + 2, 4, 4);
            g2.drawOval(x + 11, y + 10, 4, 4);

            g2.dispose();
        }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
    }
}
