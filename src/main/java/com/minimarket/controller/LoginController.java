package com.minimarket.controller;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.UsuarioDAO;
import com.minimarket.dao.impl.ClienteDAOImpl;
import com.minimarket.dao.impl.ProductoDAOImpl;
import com.minimarket.dao.impl.StockDAOImpl;
import com.minimarket.dao.impl.VentaDAOImpl;
import com.minimarket.dao.impl.CategoriaDAOImpl;
import com.minimarket.model.Usuario;
import com.minimarket.view.DashboardView;
import com.minimarket.view.InventarioView;
import com.minimarket.view.LoginView;
import com.minimarket.view.VentasView;
import com.minimarket.view.CategoriasView;
import com.minimarket.view.ReportesView;
import com.minimarket.controller.CategoriasController;
import com.minimarket.controller.ReportesController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginController {

    private final LoginView view;
    private final UsuarioDAO usuarioDAO;

    public LoginController(LoginView view, UsuarioDAO usuarioDAO) {
        this.view = view;
        this.usuarioDAO = usuarioDAO;

        this.view.addLoginListener(new LoginActionListener());
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                view.showStatusMessage("Por favor ingrese usuario y contraseña.", true);
                return;
            }

            try {
                view.showStatusMessage("Autenticando...", false);
                Usuario usuario = usuarioDAO.login(username, password);

                if (usuario != null) {
                    view.showStatusMessage("¡Acceso concedido!", false);
                    view.setVisible(false);

                    DashboardView dashboardView = new DashboardView();

                    Connection connection = DatabaseConnection.getInstance().getConnection();

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
                            usuario,
                            ventasController,
                            inventarioController,
                            categoriasController,
                            reportesController,
                            view,
                            new VentaDAOImpl(connection));

                    dashboardController.showView();

                } else {
                    view.showStatusMessage("Credenciales incorrectas o usuario inactivo.", true);
                }
            } catch (SQLException ex) {
                view.showStatusMessage("Error de conexión a la base de datos: " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        }
    }
}
