package com.minimarket;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.controller.LoginController;
import com.minimarket.controller.VentasController;
import com.minimarket.controller.InventarioController;
import com.minimarket.controller.DashboardController;
import com.minimarket.controller.CategoriasController;
import com.minimarket.controller.ReportesController;
import com.minimarket.dao.impl.UsuarioDAOImpl;
import com.minimarket.dao.impl.VentaDAOImpl;
import com.minimarket.dao.impl.ProductoDAOImpl;
import com.minimarket.dao.impl.ClienteDAOImpl;
import com.minimarket.dao.impl.StockDAOImpl;
import com.minimarket.dao.impl.CategoriaDAOImpl;
import com.minimarket.model.Usuario;
import com.minimarket.view.LoginView;
import com.minimarket.view.DashboardView;
import com.minimarket.view.VentasView;
import com.minimarket.view.InventarioView;
import com.minimarket.view.CategoriasView;
import com.minimarket.view.ReportesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.sql.Connection;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Unable to set system Look and Feel: {}", e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();

            try {
                Connection connection = DatabaseConnection.getInstance().getConnection();

                String autoLogin = DatabaseConnection.getInstance().getProperty("db.autologin");
                if ("true".equalsIgnoreCase(autoLogin)) {
                    Usuario loggedUser = null;
                    try {
                        UsuarioDAOImpl userDAO = new UsuarioDAOImpl(connection);
                        java.util.List<Usuario> users = userDAO.findAll();
                        if (users != null && !users.isEmpty()) {
                            loggedUser = users.get(0);
                        }
                    } catch (Exception ex) {
                        logger.error("Failed to load auto-login user: ", ex);
                    }
                    if (loggedUser == null) {
                        loggedUser = new Usuario(1, "admin", "admin", "Administrador", 1);
                    }

                    DashboardView dashboardView = new DashboardView();
                    VentasView ventasView = new VentasView();
                    InventarioView inventarioView = new InventarioView();
                    CategoriasView categoriasView = new CategoriasView();
                    ReportesView reportesView = new ReportesView();

                    VentasController ventasController = new VentasController(
                            ventasView,
                            new VentaDAOImpl(connection),
                            new ProductoDAOImpl(connection),
                            new ClienteDAOImpl(connection));

                    InventarioController inventarioController = new InventarioController(
                            inventarioView,
                            new StockDAOImpl(connection),
                            new ProductoDAOImpl(connection));

                    CategoriasController categoriasController = new CategoriasController(
                            categoriasView,
                            new CategoriaDAOImpl(connection),
                            new ProductoDAOImpl(connection),
                            new StockDAOImpl(connection));

                    ReportesController reportesController = new ReportesController(
                            reportesView,
                            new VentaDAOImpl(connection),
                            new ProductoDAOImpl(connection),
                            new ClienteDAOImpl(connection));

                    DashboardController dashboardController = new DashboardController(
                            dashboardView,
                            loggedUser,
                            ventasController,
                            inventarioController,
                            categoriasController,
                            reportesController,
                            loginView,
                            new VentaDAOImpl(connection));

                    dashboardController.showView();

                    logger.info("Auto-login enabled. Bypassed login view for user: {}", loggedUser.getUsername());
                } else {
                    new LoginController(loginView, new UsuarioDAOImpl(connection));
                    loginView.setVisible(true);
                }

                logger.info("MiniMarket Application successfully booted. Database Connection established.");
            } catch (Exception e) {
                logger.error("CRITICAL: Failed to connect to MySQL database on startup: {}", e.getMessage());
                logger.warn("Booting LoginView in offline demo mode.");

                new LoginController(loginView, new UsuarioDAOImpl(null));
                loginView.setVisible(true);
                loginView.showStatusMessage("ALERTA: Sin conexión a MySQL. Verifique database.properties.", true);
            }
        });
    }
}
