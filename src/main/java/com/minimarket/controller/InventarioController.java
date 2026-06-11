package com.minimarket.controller;

import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.StockDAO;
import com.minimarket.model.Producto;
import com.minimarket.model.Stock;
import com.minimarket.view.InventarioView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class InventarioController {

    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);

    private final InventarioView view;
    private final StockDAO stockDAO;
    private final ProductoDAO productoDAO;

    public InventarioController(InventarioView view, StockDAO stockDAO, ProductoDAO productoDAO) {
        this.view = view;
        this.stockDAO = stockDAO;
        this.productoDAO = productoDAO;

        initListeners();
    }

    private void initListeners() {
        view.addBuscarStockMinimoListener(new BuscarStockMinimoListener());
        view.addVerTodoListener(new VerTodoListener());
        view.addExportarExcelListener(new ExportarExcelListener());
    }

    public InventarioView getView() {
        return view;
    }

    public com.minimarket.dao.StockDAO getStockDAO() {
        return stockDAO;
    }

    public com.minimarket.dao.ProductoDAO getProductoDAO() {
        return productoDAO;
    }

    public void cargarInventarioCompleto() {
        try {
            logger.info("Consultando inventario completo desde la base de datos.");
            List<Stock> stocks = stockDAO.findAll();
            List<Producto> productos = productoDAO.findAll();
            view.setStockTableData(stocks, productos);
        } catch (SQLException ex) {
            logger.error("Error al consultar el inventario completo: ", ex);
            view.mostrarMensaje("Error al cargar inventario: " + ex.getMessage(), true);
        }
    }

    private class BuscarStockMinimoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int limite = view.getLimite();
            logger.info("Buscando alertas de stock con límite menor o igual a: {}", limite);
            try {
                List<Stock> stocksAlert = stockDAO.alertaStockMinimo(limite);
                List<Producto> productos = productoDAO.findAll();

                view.setStockTableData(stocksAlert, productos);

                if (stocksAlert.isEmpty()) {
                    logger.info("No se encontraron alertas para límite: {}", limite);
                    view.mostrarMensaje("No hay productos con stock menor o igual a " + limite, false);
                } else {
                    logger.info("Alertas encontradas: {}", stocksAlert.size());
                    view.mostrarMensaje("Se encontraron " + stocksAlert.size() + " alertas de stock mínimo.", false);
                }
            } catch (SQLException ex) {
                logger.error("Error al buscar alertas de stock: ", ex);
                view.mostrarMensaje("Error consultando alertas: " + ex.getMessage(), true);
            }
        }
    }

    private class VerTodoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            cargarInventarioCompleto();
        }
    }

    private class ExportarExcelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("Iniciando proceso de exportación a Excel.");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Reporte de Inventario");
            fileChooser.setSelectedFile(new File("Inventario_Minimarket.xlsx"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Excel (*.xlsx)", "xlsx"));

            int userSelection = fileChooser.showSaveDialog(view);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                logger.info("Exportación cancelada por el usuario.");
                return;
            }

            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!StringUtils.endsWithIgnoreCase(path, ".xlsx")) {
                path += ".xlsx";
                fileToSave = new File(path);
            }

            try {
                logger.info("Obteniendo registros de inventario para exportar a: {}", fileToSave.getName());
                List<Stock> stocks = stockDAO.findAll();
                List<Producto> productos = productoDAO.findAll();

                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Inventario");

                    // Estilos de celda
                    Font headerFont = workbook.createFont();
                    headerFont.setBold(true);
                    headerFont.setColor(IndexedColors.WHITE.getIndex());
                    headerFont.setFontHeightInPoints((short) 11);

                    CellStyle headerStyle = workbook.createCellStyle();
                    headerStyle.setFont(headerFont);
                    headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);

                    Font alertFont = workbook.createFont();
                    alertFont.setColor(IndexedColors.RED.getIndex());
                    alertFont.setBold(true);
                    CellStyle alertStyle = workbook.createCellStyle();
                    alertStyle.setFont(alertFont);

                    // Crear cabecera
                    Row headerRow = sheet.createRow(0);
                    String[] headers = { "ID Stock", "ID Producto", "Producto", "Stock Actual", "Estado" };
                    for (int i = 0; i < headers.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    int rowNum = 1;
                    int limiteAlerta = view.getLimite();
                    for (Stock s : stocks) {
                        Row row = sheet.createRow(rowNum++);

                        // Buscar el nombre del producto
                        String nameProduct = "Producto #" + s.getIdProducto();
                        for (Producto p : productos) {
                            if (p.getIdProducto() == s.getIdProducto()) {
                                nameProduct = p.getNombreProducto();
                                break;
                            }
                        }

                        row.createCell(0).setCellValue(s.getIdStock());
                        row.createCell(1).setCellValue(s.getIdProducto());
                        row.createCell(2).setCellValue(nameProduct);
                        row.createCell(3).setCellValue(s.getCantidad());

                        Cell cellEstado = row.createCell(4);
                        if (s.getCantidad() <= limiteAlerta) {
                            cellEstado.setCellValue("¡STOCK CRÍTICO BAJO!");
                            cellEstado.setCellStyle(alertStyle);
                        } else {
                            cellEstado.setCellValue("SUFICIENTE");
                        }
                    }

                    // Autoajustar anchos de columnas
                    for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    // Escribir el archivo
                    try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                        workbook.write(fileOut);
                    }

                    logger.info("Exportación completada de manera exitosa en: {}", fileToSave.getAbsolutePath());
                    view.mostrarMensaje("Inventario exportado con éxito a:\n" + fileToSave.getAbsolutePath(), false);

                } catch (Exception ex) {
                    logger.error("Error al estructurar o guardar el archivo Excel: ", ex);
                    view.mostrarMensaje("Error al generar el Excel: " + ex.getMessage(), true);
                }

            } catch (SQLException ex) {
                logger.error("Error al cargar la información para la exportación: ", ex);
                view.mostrarMensaje("Error de Base de Datos: " + ex.getMessage(), true);
            }
        }
    }
}
