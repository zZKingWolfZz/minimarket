package com.minimarket.view;

import com.minimarket.dao.impl.UsuarioDAOImpl;
import com.minimarket.controller.LoginController;
import com.minimarket.config.DatabaseConnection;
import com.minimarket.util.CustomDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.minimarket.util.IconUtil;

public class SetupAdminView extends JFrame {

    private final Connection connection;

    private RoundedTextField txtUsername;
    private RoundedPasswordField txtPassword;
    private RoundedPasswordField txtConfirmPassword;
    private RoundedTextField txtNombre;
    private RoundedTextField txtApellidoPaterno;
    private RoundedTextField txtApellidoMaterno;
    private RoundedButton btnRegister;

    public SetupAdminView(Connection connection) {
        this.connection = connection;
        initComponents();
        IconUtil.setWindowIcon(this);
    }

    private void initComponents() {
        setTitle("Minimarket - Configuración del Administrador Inicial");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252)); // Slate 50

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        // Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        cardPanel.setPreferredSize(new Dimension(580, 430));
        cardPanel.setMaximumSize(new Dimension(580, 430));

        // Header Panel (Logo & Title)
        JPanel headerPanel = new JPanel(new BorderLayout(0, 2));
        headerPanel.setOpaque(false);

        java.net.URL imgURL = SetupAdminView.class.getResource("/logo.png");
        JPanel titleLogoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleLogoPanel.setOpaque(false);
        if (imgURL != null) {
            ImageIcon logoIcon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            JLabel lblLogo = new JLabel(logoIcon);
            titleLogoPanel.add(lblLogo);
        }

        JLabel lblLogoText = new JLabel("MINI-POS");
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogoText.setForeground(new Color(24, 119, 242));
        titleLogoPanel.add(lblLogoText);
        headerPanel.add(titleLogoPanel, BorderLayout.NORTH);

        JLabel lblTitle = new JLabel("Registro del Administrador Inicial", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JLabel lblSub = new JLabel("Cree la cuenta inicial para administrar el negocio", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));
        headerPanel.add(lblSub, BorderLayout.SOUTH);

        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // Two-Column Grid Panel
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 20, 12));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Column 1: Personal Info
        txtNombre = new RoundedTextField("Ingrese nombres", null);
        gridPanel.add(createFieldPanel("Nombres *", txtNombre));

        // Column 2: Account Info
        txtUsername = new RoundedTextField("Ingrese nombre de usuario", null);
        gridPanel.add(createFieldPanel("Usuario (Username) *", txtUsername));

        txtApellidoPaterno = new RoundedTextField("Ingrese apellido paterno", null);
        gridPanel.add(createFieldPanel("Apellido Paterno *", txtApellidoPaterno));

        txtPassword = new RoundedPasswordField("Ingrese contraseña", null);
        gridPanel.add(createFieldPanel("Contraseña *", txtPassword));

        txtApellidoMaterno = new RoundedTextField("Ingrese apellido materno", null);
        gridPanel.add(createFieldPanel("Apellido Materno", txtApellidoMaterno));

        txtConfirmPassword = new RoundedPasswordField("Confirme contraseña", null);
        gridPanel.add(createFieldPanel("Confirmar Contraseña *", txtConfirmPassword));

        cardPanel.add(gridPanel, BorderLayout.CENTER);

        // Register Button
        btnRegister = new RoundedButton("Registrar Administrador y Continuar");
        btnRegister.setPreferredSize(new Dimension(0, 40));
        btnRegister.addActionListener(e -> handleRegister());

        cardPanel.add(btnRegister, BorderLayout.SOUTH);

        centerContainer.add(cardPanel);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(71, 85, 105));

        textField.setPreferredSize(new Dimension(0, 32));

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String nombre = txtNombre.getText().trim();
        String apPaterno = txtApellidoPaterno.getText().trim();
        String apMaterno = txtApellidoMaterno.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty() || apPaterno.isEmpty()) {
            CustomDialog.showWarning(this, "Por favor complete todos los campos obligatorios (*) marcados.", "Campos Requeridos");
            return;
        }

        if (!password.equals(confirmPassword)) {
            CustomDialog.showWarning(this, "Las contraseñas no coinciden. Verifique de nuevo.", "Contraseña Incorrecta");
            return;
        }

        try {
            // Check uniqueness
            String checkSql = "SELECT COUNT(*) FROM usuario WHERE username = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        CustomDialog.showWarning(this, "El nombre de usuario '" + username + "' ya está registrado. Elija otro.", "Usuario Duplicado");
                        return;
                    }
                }
            }

            // Insert User
            String insertUserSql = "INSERT INTO usuario (username, password, nombre, apellido_paterno, apellido_materno, estado) VALUES (?, SHA2(?, 256), ?, ?, ?, 1)";
            int newUserId = -1;
            try (PreparedStatement ps = connection.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, nombre);
                ps.setString(4, apPaterno);
                ps.setString(5, apMaterno);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        newUserId = keys.getInt(1);
                    }
                }
            }

            // Assign Admin Role (ID = 1)
            if (newUserId != -1) {
                String insertRoleSql = "INSERT INTO usuario_rol (Id_usuario, Id_rol) VALUES (?, 1)";
                try (PreparedStatement psRole = connection.prepareStatement(insertRoleSql)) {
                    psRole.setInt(1, newUserId);
                    psRole.executeUpdate();
                }
            }

            CustomDialog.showSuccess(this, "¡Administrador registrado correctamente!\nAhora puede iniciar sesión.", "Registro Exitoso");

            // Mark setup as completed in database properties
            DatabaseConnection.getInstance().saveProperty("db.setup.completed", "true");

            this.dispose();
            LoginView loginView = new LoginView();
            IconUtil.setWindowIcon(loginView);
            new LoginController(loginView, new UsuarioDAOImpl(connection));
            loginView.setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            CustomDialog.showError(this, "Error de base de datos al guardar el usuario: " + ex.getMessage(), "Error");
        }
    }
}
