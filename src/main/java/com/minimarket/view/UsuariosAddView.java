package com.minimarket.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuariosAddView extends JFrame {

    private final Connection connection;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtNombre;
    private JTextField txtApellidoPaterno;
    private JTextField txtApellidoMaterno;
    private JComboBox<String> cbRole;
    private JButton btnRegister;

    public UsuariosAddView(Connection connection) {
        this.connection = connection;
        initComponents();
    }

    private void initComponents() {
        setTitle("Minimarket - Registrar Nuevo Usuario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Crucial: Dispose only, don't exit app!
        setSize(520, 710);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252)); // Slate 50

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        // Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new EmptyBorder(25, 35, 25, 35));
        cardPanel.setPreferredSize(new Dimension(420, 610));
        cardPanel.setMaximumSize(new Dimension(420, 610));

        // Header Title
        JLabel lblLogoText = new JLabel("MINI-POS");
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogoText.setForeground(new Color(24, 119, 242));
        lblLogoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Registrar Nuevo Usuario", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Cree una cuenta para cajeros, vendedores o administradores", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(lblLogoText);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(lblSub);
        cardPanel.add(Box.createVerticalStrut(20));

        // Form Fields
        cardPanel.add(createFieldPanel("Usuario (Username) *", txtUsername = new JTextField()));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(createFieldPanel("Contraseña *", txtPassword = new JPasswordField()));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(createFieldPanel("Confirmar Contraseña *", txtConfirmPassword = new JPasswordField()));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(createFieldPanel("Nombres *", txtNombre = new JTextField()));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(createFieldPanel("Apellido Paterno *", txtApellidoPaterno = new JTextField()));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(createFieldPanel("Apellido Materno", txtApellidoMaterno = new JTextField()));
        cardPanel.add(Box.createVerticalStrut(10));

        // Role field
        cbRole = new JComboBox<>(new String[]{"Vendedor", "Administrador"});
        cardPanel.add(createComboPanel("Rol de Usuario *", cbRole));
        cardPanel.add(Box.createVerticalStrut(25));

        // Register Button
        btnRegister = new JButton("Registrar y Guardar Usuario");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBackground(new Color(24, 119, 242));
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(350, 42));
        btnRegister.setMaximumSize(new Dimension(350, 42));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnRegister.addActionListener(e -> handleRegister());

        cardPanel.add(btnRegister);

        centerContainer.add(cardPanel);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(350, 46));
        panel.setPreferredSize(new Dimension(350, 46));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setMaximumSize(new Dimension(350, 26));
        textField.setPreferredSize(new Dimension(350, 26));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(3));
        panel.add(textField);

        return panel;
    }

    private JPanel createComboPanel(String labelText, JComboBox<String> comboBox) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(350, 46));
        panel.setPreferredSize(new Dimension(350, 46));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setMaximumSize(new Dimension(350, 26));
        comboBox.setPreferredSize(new Dimension(350, 26));
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(3));
        panel.add(comboBox);

        return panel;
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String nombre = txtNombre.getText().trim();
        String apPaterno = txtApellidoPaterno.getText().trim();
        String apMaterno = txtApellidoMaterno.getText().trim();
        String selectedRole = (String) cbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty() || apPaterno.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos obligatorios (*) marcados.", "Campos Requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden. Verifique de nuevo.", "Contraseña Incorrecta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Check uniqueness
            String checkSql = "SELECT COUNT(*) FROM usuario WHERE username = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "El nombre de usuario '" + username + "' ya está registrado. Elija otro.", "Usuario Duplicado", JOptionPane.WARNING_MESSAGE);
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

            // Assign Selected Role (Administrador = 1, Vendedor = 2)
            int roleId = "Administrador".equalsIgnoreCase(selectedRole) ? 1 : 2;
            if (newUserId != -1) {
                String insertRoleSql = "INSERT INTO usuario_rol (Id_usuario, Id_rol) VALUES (?, ?)";
                try (PreparedStatement psRole = connection.prepareStatement(insertRoleSql)) {
                    psRole.setInt(1, newUserId);
                    psRole.setInt(2, roleId);
                    psRole.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "¡Usuario registrado correctamente como " + selectedRole + "!", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

            // Clean fields
            txtUsername.setText("");
            txtPassword.setText("");
            txtConfirmPassword.setText("");
            txtNombre.setText("");
            txtApellidoPaterno.setText("");
            txtApellidoMaterno.setText("");
            cbRole.setSelectedIndex(0);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al guardar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
