package com.minimarket.view;

import com.minimarket.dao.impl.UsuarioDAOImpl;
import com.minimarket.model.Usuario;
import com.minimarket.model.Rol;
import com.minimarket.util.CustomDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuariosAddView extends JPanel {

    private final Connection connection;

    private RoundedTextField txtUsername;
    private RoundedPasswordField txtPassword;
    private RoundedPasswordField txtConfirmPassword;
    private RoundedTextField txtNombre;
    private RoundedTextField txtApellidoPaterno;
    private RoundedTextField txtApellidoMaterno;
    private JComboBox<String> cbRole;
    private RoundedButton btnRegister;
    private JTable tblUsers;
    private DefaultTableModel tableModel;

    // CRUD Edits and deletion variables
    private Integer editingUserId = null;
    private RoundedButton btnCancelEdit;
    private RoundedButton btnEditUser;
    private RoundedButton btnDeleteUser;

    public UsuariosAddView(Connection connection) {
        this.connection = connection;
        initComponents();
        refreshUsersTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 250, 252)); // Slate 50
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // --- TOP: Registration Form Card ---
        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Form Title
        JPanel titlePanel = new JPanel(new BorderLayout(0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Registrar Nuevo Usuario");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        
        JLabel lblSub = new JLabel("Cree una cuenta para cajeros, vendedores o administradores del sistema.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        
        titlePanel.add(lblTitle, BorderLayout.NORTH);
        titlePanel.add(lblSub, BorderLayout.SOUTH);
        formCard.add(titlePanel, BorderLayout.NORTH);

        // Fields Container (Grid 2 columns)
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 20, 10));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        txtNombre = new RoundedTextField("Ingrese nombres", null);
        fieldsPanel.add(createFieldWrapper("Nombres *", txtNombre));

        txtUsername = new RoundedTextField("Ingrese nombre de usuario", null);
        fieldsPanel.add(createFieldWrapper("Usuario (Username) *", txtUsername));

        txtApellidoPaterno = new RoundedTextField("Ingrese apellido paterno", null);
        fieldsPanel.add(createFieldWrapper("Apellido Paterno *", txtApellidoPaterno));

        txtPassword = new RoundedPasswordField("Ingrese contraseña", null);
        fieldsPanel.add(createFieldWrapper("Contraseña *", txtPassword));

        txtApellidoMaterno = new RoundedTextField("Ingrese apellido materno", null);
        fieldsPanel.add(createFieldWrapper("Apellido Materno", txtApellidoMaterno));

        txtConfirmPassword = new RoundedPasswordField("Confirme contraseña", null);
        fieldsPanel.add(createFieldWrapper("Confirmar Contraseña *", txtConfirmPassword));

        cbRole = new JComboBox<>(new String[]{"Vendedor", "Administrador"});
        styleComboBox(cbRole);
        fieldsPanel.add(createComboWrapper("Rol de Usuario *", cbRole));

        // Register Button container
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnCancelEdit = new RoundedButton("Cancelar Edición") {
            {
                setForeground(new Color(100, 116, 139));
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(226, 232, 240));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                } else {
                    g2.setColor(new Color(248, 250, 252));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCancelEdit.setPreferredSize(new Dimension(140, 36));
        btnCancelEdit.setVisible(false);
        btnCancelEdit.addActionListener(e -> cancelEditing());

        btnRegister = new RoundedButton("Registrar y Guardar Usuario");
        btnRegister.setPreferredSize(new Dimension(240, 36));
        btnRegister.addActionListener(e -> handleRegister());

        btnPanel.add(btnCancelEdit);
        btnPanel.add(btnRegister);

        // Right side of register row: empty cell in layout, so we add button at the bottom of form card
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(fieldsPanel, BorderLayout.CENTER);
        centerWrapper.add(btnPanel, BorderLayout.SOUTH);

        formCard.add(centerWrapper, BorderLayout.CENTER);
        add(formCard, BorderLayout.NORTH);

        // --- BOTTOM: Users Table Container ---
        JPanel tableCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTableTitle = new JLabel("Usuarios Registrados en el Sistema");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(new Color(15, 23, 42));
        lblTableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        // Define table columns
        String[] colNames = {"ID", "Usuario", "Nombre Completo", "Rol", "Estado"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblUsers = new JTable(tableModel);
        tblUsers.setRowHeight(38);
        tblUsers.setShowGrid(false);
        tblUsers.setIntercellSpacing(new Dimension(0, 0));
        tblUsers.setBackground(Color.WHITE);
        tblUsers.setSelectionBackground(new Color(243, 244, 246));

        // Styling columns cell renderers
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                c.setForeground(new Color(15, 23, 42));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                
                // Colorize the Rol column
                if (column == 3) {
                    c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if ("Administrador".equals(value)) {
                        c.setForeground(new Color(24, 119, 242)); // Blue
                    } else {
                        c.setForeground(new Color(100, 116, 139)); // Slate
                    }
                }
                
                // Colorize Estado
                if (column == 4) {
                    c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if ("Activo".equals(value)) {
                        c.setForeground(new Color(34, 197, 94)); // Green
                    } else {
                        c.setForeground(new Color(239, 68, 68)); // Red
                    }
                }

                if (isSelected) {
                    c.setBackground(new Color(241, 245, 249));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        for (int i = 0; i < tblUsers.getColumnCount(); i++) {
            tblUsers.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Table Header
        JTableHeader header = tblUsers.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(new Color(100, 116, 139));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
        header.setPreferredSize(new Dimension(0, 32));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        JScrollPane scrollPane = new JScrollPane(tblUsers);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnEditUser = new RoundedButton("Editar Seleccionado");
        btnEditUser.setPreferredSize(new Dimension(180, 32));
        btnEditUser.addActionListener(e -> startEditingSelectedUser());

        btnDeleteUser = new RoundedButton("Eliminar Seleccionado") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(185, 28, 28));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(220, 38, 38));
                } else {
                    g2.setColor(new Color(239, 68, 68));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btnDeleteUser.setPreferredSize(new Dimension(180, 32));
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());

        actionPanel.add(btnEditUser);
        actionPanel.add(btnDeleteUser);
        tableCard.add(actionPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createFieldWrapper(String labelText, JTextField field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 116, 139));
        field.setPreferredSize(new Dimension(0, 32));
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createComboWrapper(String labelText, JComboBox<String> comboBox) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 116, 139));
        comboBox.setPreferredSize(new Dimension(0, 32));
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(comboBox, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(15, 23, 42));
    }

    public void refreshUsersTable() {
        try {
            if (connection == null) return;
            UsuarioDAOImpl userDAO = new UsuarioDAOImpl(connection);
            java.util.List<Usuario> users = userDAO.findAll();

            tableModel.setRowCount(0);
            for (Usuario u : users) {
                StringBuilder roles = new StringBuilder();
                if (u.getRoles() != null) {
                    for (Rol r : u.getRoles()) {
                        if (roles.length() > 0) roles.append(", ");
                        roles.append(r.getNombreRol());
                    }
                }
                String rolName = roles.toString();
                if (rolName.isEmpty()) rolName = "Sin Rol";

                String state = u.getEstado() == 1 ? "Activo" : "Inactivo";
                String fullName = u.getNombre() + " " + u.getApellidoPaterno() + " " + u.getApellidoMaterno();

                tableModel.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getUsername(),
                        fullName,
                        rolName,
                        state
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String nombre = txtNombre.getText().trim();
        String apPaterno = txtApellidoPaterno.getText().trim();
        String apMaterno = txtApellidoMaterno.getText().trim();
        String selectedRole = (String) cbRole.getSelectedItem();

        // Validation for CREATE vs UPDATE
        if (editingUserId == null) {
            // Create validations: Password is required
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty() || apPaterno.isEmpty()) {
                CustomDialog.showWarning(this, "Por favor complete todos los campos obligatorios (*) marcados.", "Campos Requeridos");
                return;
            }
            if (!password.equals(confirmPassword)) {
                CustomDialog.showWarning(this, "Las contraseñas no coinciden. Verifique de nuevo.", "Contraseña Incorrecta");
                return;
            }
        } else {
            // Update validations: Password is optional
            if (username.isEmpty() || nombre.isEmpty() || apPaterno.isEmpty()) {
                CustomDialog.showWarning(this, "Por favor complete todos los campos obligatorios (*) marcados.", "Campos Requeridos");
                return;
            }
            if (!password.isEmpty() && !password.equals(confirmPassword)) {
                CustomDialog.showWarning(this, "Las contraseñas no coinciden. Verifique de nuevo.", "Contraseña Incorrecta");
                return;
            }
        }

        try {
            if (connection == null) {
                if (editingUserId == null) {
                    CustomDialog.showSuccess(this, "Registrado exitosamente (Modo Demo Offline).", "Éxito");
                } else {
                    CustomDialog.showSuccess(this, "Actualizado exitosamente (Modo Demo Offline).", "Éxito");
                    cancelEditing();
                }
                refreshUsersTable();
                return;
            }

            UsuarioDAOImpl userDAO = new UsuarioDAOImpl(connection);

            if (editingUserId == null) {
                // Check uniqueness (Only for new users)
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

                CustomDialog.showSuccess(this, "¡Usuario registrado correctamente como " + selectedRole + "!", "Registro Exitoso");
            } else {
                // Check uniqueness of username for other users
                String checkSql = "SELECT COUNT(*) FROM usuario WHERE username = ? AND Id_usuario != ?";
                try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
                    ps.setString(1, username);
                    ps.setInt(2, editingUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            CustomDialog.showWarning(this, "El nombre de usuario '" + username + "' ya está registrado por otro usuario.", "Usuario Duplicado");
                            return;
                        }
                    }
                }

                // Update User Info
                Usuario u = new Usuario(editingUserId, username, null, nombre, apPaterno, apMaterno, 1);
                int roleId = "Administrador".equalsIgnoreCase(selectedRole) ? 1 : 2;
                u.addRol(new Rol(roleId, selectedRole));
                
                boolean updated = userDAO.update(u);
                if (updated) {
                    // Update password if a new one was provided
                    if (!password.isEmpty()) {
                        String updatePassSql = "UPDATE usuario SET password = SHA2(?, 256) WHERE Id_usuario = ?";
                        try (PreparedStatement ps = connection.prepareStatement(updatePassSql)) {
                            ps.setString(1, password);
                            ps.setInt(2, editingUserId);
                            ps.executeUpdate();
                        }
                    }
                    CustomDialog.showSuccess(this, "¡Usuario actualizado correctamente!", "Actualización Exitosa");
                    cancelEditing();
                } else {
                    CustomDialog.showError(this, "No se pudo actualizar el usuario.", "Error");
                }
            }

            // Clean fields & refresh
            if (editingUserId == null) {
                txtUsername.setText("");
                txtPassword.setText("");
                txtConfirmPassword.setText("");
                txtNombre.setText("");
                txtApellidoPaterno.setText("");
                txtApellidoMaterno.setText("");
                cbRole.setSelectedIndex(0);
            }
            refreshUsersTable();

        } catch (SQLException ex) {
            ex.printStackTrace();
            CustomDialog.showError(this, "Error de base de datos: " + ex.getMessage(), "Error");
        }
    }

    private void cancelEditing() {
        editingUserId = null;
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtNombre.setText("");
        txtApellidoPaterno.setText("");
        txtApellidoMaterno.setText("");
        cbRole.setSelectedIndex(0);

        btnRegister.setText("Registrar y Guardar Usuario");
        btnCancelEdit.setVisible(false);
        tblUsers.clearSelection();
    }

    private void startEditingSelectedUser() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow == -1) {
            CustomDialog.showWarning(this, "Por favor seleccione un usuario de la tabla para editar.", "Selección Requerida");
            return;
        }
        int modelRow = tblUsers.convertRowIndexToModel(selectedRow);
        Integer idUser = (Integer) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);
        String fullName = (String) tableModel.getValueAt(modelRow, 2);
        String rol = (String) tableModel.getValueAt(modelRow, 3);

        editingUserId = idUser;

        String nombre = "";
        String apPaterno = "";
        String apMaterno = "";

        if (connection != null) {
            try {
                UsuarioDAOImpl userDAO = new UsuarioDAOImpl(connection);
                Usuario u = userDAO.findById(idUser);
                if (u != null) {
                    nombre = u.getNombre();
                    apPaterno = u.getApellidoPaterno();
                    apMaterno = u.getApellidoMaterno();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (nombre.isEmpty()) {
            String[] parts = fullName.split(" ");
            if (parts.length > 0) nombre = parts[0];
            if (parts.length > 1) apPaterno = parts[1];
            if (parts.length > 2) {
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < parts.length; i++) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(parts[i]);
                }
                apMaterno = sb.toString();
            }
        }

        txtNombre.setText(nombre);
        txtUsername.setText(username);
        txtApellidoPaterno.setText(apPaterno);
        txtApellidoMaterno.setText(apMaterno);
        txtPassword.setText(""); 
        txtConfirmPassword.setText("");

        cbRole.setSelectedItem("Administrador".equalsIgnoreCase(rol) ? "Administrador" : "Vendedor");

        btnRegister.setText("Guardar Cambios");
        btnCancelEdit.setVisible(true);
    }

    private void deleteSelectedUser() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow == -1) {
            CustomDialog.showWarning(this, "Por favor seleccione un usuario de la tabla para eliminar.", "Selección Requerida");
            return;
        }
        int modelRow = tblUsers.convertRowIndexToModel(selectedRow);
        Integer idUser = (Integer) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);

        if (idUser == 1) {
            CustomDialog.showWarning(this, "No se puede eliminar el usuario administrador principal (ID: 1) por seguridad.", "Acción Bloqueada");
            return;
        }

        boolean confirm = CustomDialog.showConfirm(this, 
                "¿Está seguro de eliminar al usuario '" + username + "'?\nEsta acción es irreversible y eliminará sus datos de acceso.", 
                "Eliminar Usuario");
        if (confirm) {
            try {
                if (connection == null) {
                    CustomDialog.showSuccess(this, "Usuario '" + username + "' eliminado (Modo Demo Offline).", "Éxito");
                    refreshUsersTable();
                    return;
                }
                UsuarioDAOImpl userDAO = new UsuarioDAOImpl(connection);
                boolean deleted = userDAO.delete(idUser);
                if (deleted) {
                    CustomDialog.showSuccess(this, "¡Usuario '" + username + "' eliminado exitosamente!", "Eliminado Correctamente");
                    if (editingUserId != null && editingUserId.equals(idUser)) {
                        cancelEditing();
                    }
                    refreshUsersTable();
                } else {
                    CustomDialog.showError(this, "No se pudo eliminar el usuario.", "Error");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                CustomDialog.showError(this, "Error al eliminar usuario: " + ex.getMessage(), "Error");
            }
        }
    }
}
