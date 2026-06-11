package com.minimarket.controller;

import com.minimarket.model.Rol;
import com.minimarket.model.Usuario;
import com.minimarket.model.Venta;
import com.minimarket.dao.VentaDAO;
import com.minimarket.view.DashboardView;
import com.minimarket.view.LoginView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    private final DashboardView view;
    private final Usuario loggedUser;
    private final VentasController ventasController;
    private final InventarioController inventarioController;
    private final CategoriasController categoriasController;
    private final ReportesController reportesController;
    private final LoginView loginView;
    private final VentaDAO ventaDAO;

    public DashboardController(DashboardView view, Usuario loggedUser,
            VentasController ventasController,
            InventarioController inventarioController,
            CategoriasController categoriasController,
            ReportesController reportesController,
            LoginView loginView,
            VentaDAO ventaDAO) {
        this.view = view;
        this.loggedUser = loggedUser;
        this.ventasController = ventasController;
        this.inventarioController = inventarioController;
        this.categoriasController = categoriasController;
        this.reportesController = reportesController;
        this.loginView = loginView;
        this.ventaDAO = ventaDAO;

        initSessionInfo();
        initListeners();
    }

    private void initSessionInfo() {
        StringBuilder sb = new StringBuilder();
        if (loggedUser.getRoles() != null) {
            for (Rol r : loggedUser.getRoles()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(r.getNombreRol());
            }
        }
        String rolesStr = sb.toString();
        if (rolesStr.isEmpty()) {
            rolesStr = "Sin Rol Asignado";
        }
        view.setLoggedUser(loggedUser.getNombreCompletos(), rolesStr);
    }

    private void initListeners() {
        view.addDashboardMenuListener(new DashboardMenuListener());
        if (tieneRol("Administrador") || tieneRol("Cajero")) {
            view.addVentasMenuListener(new VentasMenuListener());
            view.addInventarioMenuListener(new InventarioMenuListener());
            view.addCategoriasMenuListener(new CategoriasMenuListener());
            view.addReportsMenuListener(new ReportsMenuListener());
        }
        view.addLogoutMenuListener(new LogoutListener());
    }

    public void showView() {
        cargarDatosDashboard();
        view.setVisible(true);
    }

    public void cargarDatosDashboard() {
        try {
            List<Venta> ventas = ventaDAO.findAll();
            List<com.minimarket.model.Stock> stocks = null;
            List<com.minimarket.model.Producto> productos = null;

            if (inventarioController != null) {
                try {
                    stocks = inventarioController.getStockDAO().findAll();
                    productos = inventarioController.getProductoDAO().findAll();
                } catch (Exception e) {
                    System.err.println("Error al cargar inventario para el dashboard: " + e.getMessage());
                }
            }

            view.setViewPanel(view.createDashboardStatsPanel(ventas, stocks, productos));
        } catch (SQLException ex) {
            System.err.println("Error al cargar ventas en el dashboard: " + ex.getMessage());
            ex.printStackTrace();
            view.setViewPanel(view.createDashboardStatsPanel(new ArrayList<>(), null, null));
        }
    }

    private boolean tieneRol(String nombre) {
        if (loggedUser.getRoles() == null) {
            return false;
        }
        for (Rol r : loggedUser.getRoles()) {
            if (nombre.equals(r.getNombreRol())) {
                return true;
            }
        }
        return false;
    }

    private class DashboardMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            cargarDatosDashboard();
            view.setHeaderTitle("Panel de Control del Sistema");
        }
    }

    private class VentasMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ventasController.initData();
            view.setViewPanel(ventasController.getView());
            view.setHeaderTitle("Terminal de Ventas");
        }
    }

    private class InventarioMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            inventarioController.cargarInventarioCompleto();
            view.setViewPanel(inventarioController.getView());
        }
    }

    private class CategoriasMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            categoriasController.initData();
            view.setViewPanel(categoriasController.getView());
            view.setHeaderTitle("Gestión de Categorías");
        }
    }

    private class ReportsMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            reportesController.initData();
            view.setViewPanel(reportesController.getView());
            view.setHeaderTitle("Informes Detallados y Análisis");
        }
    }

    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose();
            loginView.clearFields();
            loginView.showStatusMessage("Sesion cerrada correctamente.", false);
            loginView.setVisible(true);
        }
    }
}
