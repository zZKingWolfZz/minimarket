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
import com.minimarket.view.SetupAdminView;
import com.minimarket.view.DatabaseConfigView;
import com.minimarket.util.IconUtil;
import com.minimarket.config.DatabaseInitializer;

import javax.swing.*;
import java.sql.Connection;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static Connection checkAndPromptDatabase() {
        if (!DatabaseConnection.getInstance().isConfigured()) {
            logger.info("Fresh installation detected. No database configuration file found. Launching DatabaseConfigView...");
            DatabaseConfigView configView = new DatabaseConfigView(null);
            configView.setVisible(true);
            
            if (configView.isConnectionSuccessful()) {
                try {
                    return DatabaseConnection.getInstance().getConnection();
                } catch (Exception e) {
                    logger.error("Failed to retrieve connection after user configuration: ", e);
                }
            }
            return null;
        }

        try {
            if (DatabaseConnection.getInstance().checkHealth()) {
                return DatabaseConnection.getInstance().getConnection();
            }
        } catch (Exception e) {
            // failed
        }

        logger.info("Database connection failed or not configured. Launching DatabaseConfigView dialog...");
        DatabaseConfigView configView = new DatabaseConfigView(null);
        configView.setVisible(true); // Modal blocks here

        if (configView.isConnectionSuccessful()) {
            try {
                return DatabaseConnection.getInstance().getConnection();
            } catch (Exception e) {
                logger.error("Failed to retrieve connection after user configuration: ", e);
            }
        }
        return null;
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Unable to set system Look and Feel: {}", e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            IconUtil.setWindowIcon(loginView);

            try {
                Connection connection = checkAndPromptDatabase();

                if (connection != null) {
                    // 1. Initialize schema automatically if not exists
                    DatabaseInitializer.initializeDatabase(connection);

                    // 2. Check if first run (no users in database)
                    if (DatabaseInitializer.isDatabaseEmpty(connection)) {
                        logger.info("Database is empty (no users found). Launching SetupAdminView for first-run administrator setup.");
                        SetupAdminView setupAdmin = new SetupAdminView(connection);
                        IconUtil.setWindowIcon(setupAdmin);
                        setupAdmin.setVisible(true);
                        return;
                    }

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
                        IconUtil.setWindowIcon(dashboardView);
                        
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
                } else {
                    logger.warn("Booting LoginView in offline demo mode.");
                    new LoginController(loginView, new UsuarioDAOImpl(null));
                    loginView.setVisible(true);
                    loginView.showStatusMessage("MODO DEMOSTRACIÓN: Sin conexión a MySQL.", true);
                }
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
