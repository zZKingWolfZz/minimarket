package com.minimarket.controller;

import com.minimarket.dao.ClienteDAO;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.VentaDAO;
import com.minimarket.dao.impl.CategoriaDAOImpl;
import com.minimarket.config.DatabaseConnection;
import com.minimarket.model.Cliente;
import com.minimarket.model.Producto;
import com.minimarket.model.Venta;
import com.minimarket.view.ReportesView;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class ReportesController {

    private static final Logger logger = LoggerFactory.getLogger(ReportesController.class);

    private final ReportesView view;
    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;

    // Cache of all data
    private List<Venta> allVentas = new ArrayList<>();
    private List<Producto> allProductos = new ArrayList<>();
    private List<Cliente> allClientes = new ArrayList<>();
    private Map<Integer, Producto> productMap = new HashMap<>();
    private Map<Integer, Cliente> clienteMap = new HashMap<>();

    // Current filter state
    private String selectedRange = "30D";
    private LocalDate customStartDate = null;
    private LocalDate customEndDate = null;

    // Calculated summary rows (for full list dialog)
    private List<Object[]> currentReportRows = new ArrayList<>();

    public ReportesController(ReportesView view, VentaDAO ventaDAO, ProductoDAO productoDAO, ClienteDAO clienteDAO) {
        this.view = view;
        this.ventaDAO = ventaDAO;
        this.productoDAO = productoDAO;
        this.clienteDAO = clienteDAO;

        initListeners();
    }

    private void initListeners() {
        view.addHoyListener(e -> setRange("Hoy"));
        view.add7DListener(e -> setRange("7D"));
        view.add30DListener(e -> setRange("30D"));
        view.add1AListener(e -> setRange("1A"));
        view.addRangoPersonalizadoListener(new RangoPersonalizadoListener());
        view.addCompartirListener(new CompartirListener());
        view.addExportarReporteListener(new ExportarReporteListener());
        view.addVerListaCompletaListener(new VerListaCompletaListener());
    }

    public ReportesView getView() {
        return view;
    }

    public void initData() {
        logger.info("Precargando información analítica desde la base de datos.");
        try {
            allVentas = ventaDAO.findAll();
            allProductos = productoDAO.findAll();
            allClientes = clienteDAO.findAll();

            productMap.clear();
            for (Producto p : allProductos) {
                productMap.put(p.getIdProducto(), p);
            }

            clienteMap.clear();
            for (Cliente c : allClientes) {
                clienteMap.put(c.getIdCliente(), c);
            }

            // Aplicar filtros y actualizar UI
            updateReportData();

        } catch (SQLException ex) {
            logger.error("Error al cargar la información analítica de ventas: ", ex);
            view.mostrarMensaje("Error al estructurar reportes: " + ex.getMessage(), true);
        }
    }

    private void setRange(String range) {
        this.selectedRange = range;
        updateReportData();
    }

    private void updateReportData() {
        view.setRangeActive(selectedRange);
        List<Venta> filteredVentas = filterVentasByRange();

        // 1. Calcular KPIs principales
        BigDecimal totalSales = BigDecimal.ZERO;
        int transactionCount = filteredVentas.size();
        for (Venta v : filteredVentas) {
            totalSales = totalSales.add(v.getPrecioTotal());
        }

        BigDecimal averageTicket = BigDecimal.ZERO;
        if (transactionCount > 0) {
            averageTicket = totalSales.divide(new BigDecimal(transactionCount), 2, RoundingMode.HALF_UP);
        }

        view.setKpis(totalSales, transactionCount, averageTicket);

        // 2. Calcular Top 10 Productos Más Vendidos
        Map<Integer, Integer> unidadesVendidasMap = new HashMap<>();
        Map<Integer, BigDecimal> ingresosMap = new HashMap<>();

        for (Venta v : filteredVentas) {
            unidadesVendidasMap.put(v.getIdProducto(), unidadesVendidasMap.getOrDefault(v.getIdProducto(), 0) + v.getCantidad());
            ingresosMap.put(v.getIdProducto(), ingresosMap.getOrDefault(v.getIdProducto(), BigDecimal.ZERO).add(v.getPrecioTotal()));
        }

        List<Map.Entry<Integer, BigDecimal>> sortedProducts = new ArrayList<>(ingresosMap.entrySet());
        sortedProducts.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Orden descendente por ingresos

        currentReportRows.clear();
        int rank = 1;
        for (Map.Entry<Integer, BigDecimal> entry : sortedProducts) {
            int prodId = entry.getKey();
            BigDecimal ingresos = entry.getValue();
            int unidades = unidadesVendidasMap.get(prodId);

            Producto p = productMap.get(prodId);
            String prodName = p != null ? p.getNombreProducto() : "Producto #" + prodId;

            String trend = "ESTABLE";
            if (rank == 1 || rank == 2) trend = "CRECIENTE";
            else if (rank == 3 || rank == 5) trend = "BAJANDO";

            currentReportRows.add(new Object[] {
                    rank,
                    prodName,
                    unidades + " uds.",
                    "S/" + String.format("%.2f", ingresos),
                    trend
            });
            rank++;
        }

        // Poner en la tabla de la vista sólo el Top 10
        List<Object[]> top10Rows = new ArrayList<>();
        for (int i = 0; i < Math.min(10, currentReportRows.size()); i++) {
            top10Rows.add(currentReportRows.get(i));
        }
        view.setTopProductsTableData(top10Rows);

        // 3. Actualizar Gráficos dinámicamente con los datos reales
        updateChartsData(filteredVentas);
    }

    private List<Venta> filterVentasByRange() {
        List<Venta> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Venta v : allVentas) {
            LocalDate fecha = v.getFecha();
            if (fecha == null) continue;

            boolean keep = false;
            switch (selectedRange) {
                case "Hoy":
                    keep = fecha.isEqual(today);
                    break;
                case "7D":
                    keep = fecha.isAfter(today.minusDays(8));
                    break;
                case "30D":
                    keep = fecha.isAfter(today.minusDays(31));
                    break;
                case "1A":
                    keep = fecha.isAfter(today.minusYears(1).minusDays(1));
                    break;
                case "Custom":
                    if (customStartDate != null && customEndDate != null) {
                        keep = !fecha.isBefore(customStartDate) && !fecha.isAfter(customEndDate);
                    } else {
                        keep = true; // Si fallan fechas, no filtrar
                    }
                    break;
                default:
                    keep = true;
            }

            if (keep) {
                result.add(v);
            }
        }
        return result;
    }

    private void updateChartsData(List<Venta> filteredVentas) {
        // --- 1. Gráfico de Comparativa de Ventas Mensuales (Últimos 6 meses) ---
        List<String> monthsList = new ArrayList<>();
        List<Integer> currentYearValues = new ArrayList<>();
        List<Integer> pastYearValues = new ArrayList<>();

        LocalDate today = LocalDate.now();
        // Generar nombres de los últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            LocalDate mDate = today.minusMonths(i);
            String mName = mDate.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "PE"));
            // Capitalizar primera letra
            mName = mName.substring(0, 1).toUpperCase() + mName.substring(1);
            monthsList.add(mName);

            // Calcular ingresos de este mes para el año actual y el año anterior
            int mVal = mDate.getMonthValue();
            int currentYear = mDate.getYear();
            int pastYear = currentYear - 1;

            BigDecimal sumCurrent = BigDecimal.ZERO;
            BigDecimal sumPast = BigDecimal.ZERO;

            for (Venta v : allVentas) {
                LocalDate f = v.getFecha();
                if (f != null && f.getMonthValue() == mVal) {
                    if (f.getYear() == currentYear) {
                        sumCurrent = sumCurrent.add(v.getPrecioTotal());
                    } else if (f.getYear() == pastYear) {
                        sumPast = sumPast.add(v.getPrecioTotal());
                    }
                }
            }

            currentYearValues.add(sumCurrent.intValue());
            pastYearValues.add(sumPast.intValue());
        }

        view.setMonthlyChartData(monthsList, currentYearValues, pastYearValues);

        // --- 2. Gráfico de Ventas por Categoría ---
        Map<String, BigDecimal> catSales = new HashMap<>();
        BigDecimal totalSalesSum = BigDecimal.ZERO;

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            CategoriaDAOImpl catDAO = new CategoriaDAOImpl(connection);
            Map<Integer, String> catNamesMap = new HashMap<>();
            
            // Cargar nombres de categorías
            for (com.minimarket.model.Categoria cat : catDAO.findAll()) {
                catNamesMap.put(cat.getIdCategoria(), cat.getNombreCategoria());
            }

            for (Venta v : filteredVentas) {
                Producto p = productMap.get(v.getIdProducto());
                if (p != null) {
                    String catName = catNamesMap.getOrDefault(p.getIdCategoria(), "Otros");
                    catSales.put(catName, catSales.getOrDefault(catName, BigDecimal.ZERO).add(v.getPrecioTotal()));
                    totalSalesSum = totalSalesSum.add(v.getPrecioTotal());
                }
            }

            Map<String, Double> pcts = new HashMap<>();
            Map<String, Color> colors = new HashMap<>();

            // Asignar colores fijos a las categorías comunes
            Color[] palette = {
                    new Color(24, 119, 242), // Blue
                    new Color(56, 189, 248), // Sky Blue
                    new Color(34, 197, 94),  // Green
                    new Color(245, 158, 11), // Orange/Gold
                    new Color(139, 92, 246), // Purple
                    new Color(239, 68, 68)   // Red
            };

            int colIndex = 0;
            if (totalSalesSum.compareTo(BigDecimal.ZERO) > 0) {
                for (Map.Entry<String, BigDecimal> entry : catSales.entrySet()) {
                    double pct = entry.getValue().multiply(new BigDecimal(100))
                            .divide(totalSalesSum, 2, RoundingMode.HALF_UP).doubleValue();
                    pcts.put(entry.getKey(), pct);
                    colors.put(entry.getKey(), palette[colIndex % palette.length]);
                    colIndex++;
                }
            }

            view.setCategoryChartData(pcts, colors);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class RangoPersonalizadoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String inputStart = JOptionPane.showInputDialog(view, "Ingrese Fecha de Inicio (YYYY-MM-DD):", LocalDate.now().minusDays(30).toString());
            if (inputStart == null || inputStart.trim().isEmpty()) return;

            String inputEnd = JOptionPane.showInputDialog(view, "Ingrese Fecha de Fin (YYYY-MM-DD):", LocalDate.now().toString());
            if (inputEnd == null || inputEnd.trim().isEmpty()) return;

            try {
                customStartDate = LocalDate.parse(inputStart.trim());
                customEndDate = LocalDate.parse(inputEnd.trim());

                if (customStartDate.isAfter(customEndDate)) {
                    view.mostrarMensaje("La fecha de inicio no puede ser posterior a la fecha de fin.", true);
                    return;
                }

                setRange("Custom");

            } catch (Exception ex) {
                view.mostrarMensaje("Formato de fecha inválido. Utilice el formato YYYY-MM-DD.", true);
            }
        }
    }

    private class CompartirListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Obtener datos actuales
                BigDecimal totalSales = BigDecimal.ZERO;
                int transactionCount = filterVentasByRange().size();
                for (Venta v : filterVentasByRange()) {
                    totalSales = totalSales.add(v.getPrecioTotal());
                }

                BigDecimal averageTicket = BigDecimal.ZERO;
                if (transactionCount > 0) {
                    averageTicket = totalSales.divide(new BigDecimal(transactionCount), 2, RoundingMode.HALF_UP);
                }

                String summary = String.format(
                        "=== REPORTE DE VENTAS - MINI-POS ===\n" +
                        "Periodo: %s\n" +
                        "Ventas Totales: S/%,.2f\n" +
                        "Total Transacciones: %,d\n" +
                        "Ticket Promedio: S/%,.2f\n" +
                        "Fecha de Generación: %s\n" +
                        "====================================",
                        selectedRange,
                        totalSales,
                        transactionCount,
                        averageTicket,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );

                StringSelection selection = new StringSelection(summary);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

                view.mostrarMensaje("¡Resumen de reporte copiado al portapapeles con éxito!", false);

            } catch (Exception ex) {
                view.mostrarMensaje("Error al copiar reporte: " + ex.getMessage(), true);
            }
        }
    }

    private class VerListaCompletaListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showFullProductsDialog();
        }
    }

    private void showFullProductsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view), "Rendimiento Completo de Productos", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(view);
        dialog.setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel lblTitle = new JLabel("Rendimiento Completo de Productos Vendidos (" + selectedRange + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        String[] columns = { "RANGO", "PRODUCTO", "UNIDADES VENDIDAS", "INGRESOS", "TENDENCIA" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        for (Object[] row : currentReportRows) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setForeground(new Color(100, 116, 139));
        header.setBackground(new Color(248, 250, 252));
        header.setPreferredSize(new Dimension(0, 36));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        dialog.add(pnlHeader, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private class ExportarReporteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("Iniciando exportación de reporte detallado a Excel.");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exportar Reporte de Ventas");
            fileChooser.setSelectedFile(new File("Reporte_Detallado_Ventas.xlsx"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Excel (*.xlsx)", "xlsx"));

            int userSelection = fileChooser.showSaveDialog(view);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                logger.info("Exportación de reportes cancelada por el usuario.");
                return;
            }

            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!StringUtils.endsWithIgnoreCase(path, ".xlsx")) {
                path += ".xlsx";
                fileToSave = new File(path);
            }

            try {
                List<Venta> filteredVentas = filterVentasByRange();

                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Reporte de Ventas");

                    // Fuentes y Estilos
                    org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
                    titleFont.setBold(true);
                    titleFont.setFontHeightInPoints((short) 12);
                    titleFont.setColor(IndexedColors.BLACK.getIndex());

                    CellStyle titleStyle = workbook.createCellStyle();
                    titleStyle.setFont(titleFont);
                    titleStyle.setAlignment(HorizontalAlignment.CENTER);
                    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    titleStyle.setBorderBottom(BorderStyle.THIN);
                    titleStyle.setBorderTop(BorderStyle.THIN);
                    titleStyle.setBorderLeft(BorderStyle.THIN);
                    titleStyle.setBorderRight(BorderStyle.THIN);
                    titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    org.apache.poi.ss.usermodel.Font subtitleFont = workbook.createFont();
                    subtitleFont.setFontHeightInPoints((short) 10);
                    subtitleFont.setColor(IndexedColors.BLACK.getIndex());

                    CellStyle subtitleStyle = workbook.createCellStyle();
                    subtitleStyle.setFont(subtitleFont);
                    subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
                    subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    subtitleStyle.setBorderBottom(BorderStyle.THIN);
                    subtitleStyle.setBorderTop(BorderStyle.THIN);
                    subtitleStyle.setBorderLeft(BorderStyle.THIN);
                    subtitleStyle.setBorderRight(BorderStyle.THIN);
                    subtitleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    subtitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    // Escribir cabecera del reporte
                    Row titleRow = sheet.createRow(0);
                    titleRow.setHeightInPoints(24);
                    for (int i = 0; i <= 5; i++) {
                        Cell cell = titleRow.createCell(i);
                        cell.setCellStyle(titleStyle);
                    }
                    titleRow.getCell(0).setCellValue("INFORME DETALLADO Y ANÁLISIS DE VENTAS (" + selectedRange + ")");
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

                    Row subtitleRow = sheet.createRow(1);
                    subtitleRow.setHeightInPoints(20);
                    for (int i = 0; i <= 5; i++) {
                        Cell cell = subtitleRow.createCell(i);
                        cell.setCellStyle(subtitleStyle);
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    subtitleRow.getCell(0).setCellValue("Fecha de Generación: " + LocalDateTime.now().format(formatter));
                    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

                    // KPIs en el Excel
                    BigDecimal totalSales = BigDecimal.ZERO;
                    for (Venta v : filteredVentas) {
                        totalSales = totalSales.add(v.getPrecioTotal());
                    }
                    BigDecimal averageTicket = filteredVentas.isEmpty() ? BigDecimal.ZERO : totalSales.divide(new BigDecimal(filteredVentas.size()), 2, RoundingMode.HALF_UP);

                    org.apache.poi.ss.usermodel.Font kpiFont = workbook.createFont();
                    kpiFont.setFontHeightInPoints((short) 10);
                    kpiFont.setBold(true);

                    CellStyle kpiLabelStyle = workbook.createCellStyle();
                    kpiLabelStyle.setFont(kpiFont);
                    kpiLabelStyle.setBorderBottom(BorderStyle.THIN);
                    kpiLabelStyle.setBorderTop(BorderStyle.THIN);
                    kpiLabelStyle.setBorderLeft(BorderStyle.THIN);
                    kpiLabelStyle.setBorderRight(BorderStyle.THIN);
                    kpiLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle kpiValStyle = workbook.createCellStyle();
                    kpiValStyle.setFont(kpiFont);
                    kpiValStyle.setBorderBottom(BorderStyle.THIN);
                    kpiValStyle.setBorderTop(BorderStyle.THIN);
                    kpiValStyle.setBorderLeft(BorderStyle.THIN);
                    kpiValStyle.setBorderRight(BorderStyle.THIN);
                    kpiValStyle.setAlignment(HorizontalAlignment.CENTER);
                    kpiValStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle kpiValStyleCurrency = workbook.createCellStyle();
                    kpiValStyleCurrency.cloneStyleFrom(kpiValStyle);
                    kpiValStyleCurrency.setDataFormat(workbook.createDataFormat().getFormat("\"S/\"#,##0.00"));

                    Row kpiTitleRow = sheet.createRow(3);
                    kpiTitleRow.setHeightInPoints(20);
                    Cell c1 = kpiTitleRow.createCell(0);
                    c1.setCellValue("VENTAS TOTALES:");
                    c1.setCellStyle(kpiLabelStyle);
                    Cell c2 = kpiTitleRow.createCell(1);
                    c2.setCellValue(totalSales.doubleValue());
                    c2.setCellStyle(kpiValStyleCurrency);

                    Row kpiTransRow = sheet.createRow(4);
                    kpiTransRow.setHeightInPoints(20);
                    Cell c3 = kpiTransRow.createCell(0);
                    c3.setCellValue("TRANSACCIONES:");
                    c3.setCellStyle(kpiLabelStyle);
                    Cell c4 = kpiTransRow.createCell(1);
                    c4.setCellValue(filteredVentas.size());
                    c4.setCellStyle(kpiValStyle);

                    Row kpiTicketRow = sheet.createRow(5);
                    kpiTicketRow.setHeightInPoints(20);
                    Cell c5 = kpiTicketRow.createCell(0);
                    c5.setCellValue("TICKET PROMEDIO:");
                    c5.setCellStyle(kpiLabelStyle);
                    Cell c6 = kpiTicketRow.createCell(1);
                    c6.setCellValue(averageTicket.doubleValue());
                    c6.setCellStyle(kpiValStyleCurrency);

                    // Espacio y Título de Tabla
                    org.apache.poi.ss.usermodel.Font tableTitleFont = workbook.createFont();
                    tableTitleFont.setBold(true);
                    tableTitleFont.setFontHeightInPoints((short) 11);
                    tableTitleFont.setColor(IndexedColors.BLACK.getIndex());

                    CellStyle tableTitleStyle = workbook.createCellStyle();
                    tableTitleStyle.setFont(tableTitleFont);
                    tableTitleStyle.setAlignment(HorizontalAlignment.CENTER);
                    tableTitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    tableTitleStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    tableTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    tableTitleStyle.setBorderBottom(BorderStyle.THIN);
                    tableTitleStyle.setBorderTop(BorderStyle.THIN);
                    tableTitleStyle.setBorderLeft(BorderStyle.THIN);
                    tableTitleStyle.setBorderRight(BorderStyle.THIN);

                    Row tableTitleRow = sheet.createRow(7);
                    tableTitleRow.setHeightInPoints(22);
                    for (int i = 0; i <= 5; i++) {
                        Cell cell = tableTitleRow.createCell(i);
                        cell.setCellStyle(tableTitleStyle);
                    }
                    tableTitleRow.getCell(0).setCellValue("HISTORIAL DETALLADO DE TRANSACCIONES");
                    sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 5));

                    // Tabla de transacciones headers
                    org.apache.poi.ss.usermodel.Font thFont = workbook.createFont();
                    thFont.setBold(true);
                    thFont.setFontHeightInPoints((short) 10);
                    thFont.setColor(IndexedColors.BLACK.getIndex());

                    CellStyle thStyle = workbook.createCellStyle();
                    thStyle.setFont(thFont);
                    thStyle.setAlignment(HorizontalAlignment.CENTER);
                    thStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    thStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
                    thStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    thStyle.setBorderBottom(BorderStyle.THIN);
                    thStyle.setBorderTop(BorderStyle.THIN);
                    thStyle.setBorderLeft(BorderStyle.THIN);
                    thStyle.setBorderRight(BorderStyle.THIN);

                    Row tableHeaderRow = sheet.createRow(8);
                    tableHeaderRow.setHeightInPoints(22);
                    String[] headers = { "ID Venta", "Fecha", "Cliente", "Producto", "Cantidad", "Total Facturado" };
                    for (int i = 0; i < headers.length; i++) {
                        Cell cell = tableHeaderRow.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(thStyle);
                    }

                    // Table Data Styling
                    CellStyle borderCenterStyle = workbook.createCellStyle();
                    borderCenterStyle.setBorderBottom(BorderStyle.THIN);
                    borderCenterStyle.setBorderTop(BorderStyle.THIN);
                    borderCenterStyle.setBorderLeft(BorderStyle.THIN);
                    borderCenterStyle.setBorderRight(BorderStyle.THIN);
                    borderCenterStyle.setAlignment(HorizontalAlignment.CENTER);
                    borderCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderLeftStyle = workbook.createCellStyle();
                    borderLeftStyle.setBorderBottom(BorderStyle.THIN);
                    borderLeftStyle.setBorderTop(BorderStyle.THIN);
                    borderLeftStyle.setBorderLeft(BorderStyle.THIN);
                    borderLeftStyle.setBorderRight(BorderStyle.THIN);
                    borderLeftStyle.setAlignment(HorizontalAlignment.LEFT);
                    borderLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderCenterCurrencyStyle = workbook.createCellStyle();
                    borderCenterCurrencyStyle.cloneStyleFrom(borderCenterStyle);
                    borderCenterCurrencyStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/\"#,##0.00"));

                    int rowNum = 9;
                    for (Venta v : filteredVentas) {
                        Row row = sheet.createRow(rowNum++);
                        row.setHeightInPoints(20);

                        Producto p = productMap.get(v.getIdProducto());
                        String pName = p != null ? p.getNombreProducto() : "Producto #" + v.getIdProducto();

                        Cliente c = clienteMap.get(v.getIdCliente());
                        String cName = c != null ? c.toString() : "Cliente #" + v.getIdCliente();

                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(v.getIdVenta());
                        cell0.setCellStyle(borderCenterStyle);

                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(v.getFecha().toString());
                        cell1.setCellStyle(borderCenterStyle);

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(cName);
                        cell2.setCellStyle(borderLeftStyle);

                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue(pName);
                        cell3.setCellStyle(borderLeftStyle);

                        Cell cell4 = row.createCell(4);
                        cell4.setCellValue(v.getCantidad());
                        cell4.setCellStyle(borderCenterStyle);

                        Cell cell5 = row.createCell(5);
                        cell5.setCellValue(v.getPrecioTotal().doubleValue());
                        cell5.setCellStyle(borderCenterCurrencyStyle);
                    }

                    // Autoajustar columnas
                    for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    // Escribir archivo
                    try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                        workbook.write(fileOut);
                    }

                    logger.info("Reporte detallado exportado correctamente en: {}", fileToSave.getAbsolutePath());
                    view.mostrarMensaje("Reporte analítico exportado con éxito a:\n" + fileToSave.getAbsolutePath(), false);
                }

            } catch (Exception ex) {
                logger.error("Error al estructurar o guardar el archivo Excel del reporte: ", ex);
                view.mostrarMensaje("Error al generar el archivo Excel: " + ex.getMessage(), true);
            }
        }
    }
}
