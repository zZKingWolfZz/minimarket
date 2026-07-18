package com.minimarket.view;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.util.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfigView extends JDialog {
    private RoundedTextField txtHost;
    private RoundedTextField txtPort;
    private RoundedTextField txtDatabase;
    private RoundedTextField txtUser;
    private RoundedPasswordField txtPassword;
    private JLabel lblStatus;
    private RoundedButton btnConnect;
    private JButton btnDemo;
    private boolean connectionSuccessful = false;

    public DatabaseConfigView(Frame parent) {
        super(parent, "Configuración de Base de Datos", true);
        initComponents();
        IconUtil.setWindowIcon(this);
    }

    private void initComponents() {
        setSize(450, 660);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252)); // Slate 50

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        // Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        cardPanel.setPreferredSize(new Dimension(380, 580));
        cardPanel.setMaximumSize(new Dimension(380, 580));

        // Logo
        java.net.URL imgURL = DatabaseConfigView.class.getResource("/logo.png");
        if (imgURL != null) {
            ImageIcon logoIcon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH));
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardPanel.add(lblLogo);
            cardPanel.add(Box.createVerticalStrut(10));
        }

        JLabel lblTitle = new JLabel("Configuración de Conexión", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Ingrese las credenciales del servidor MySQL", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSub);
        cardPanel.add(Box.createVerticalStrut(15));

        // Load saved settings or fallbacks
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        String savedHost = dbConn.getProperty("db.host");
        String savedPort = dbConn.getProperty("db.port");
        String savedDb = dbConn.getProperty("db.name");
        String savedUser = dbConn.getProperty("db.username");
        
        // Backward compatibility: parse from db.url if host isn't set
        String savedUrl = dbConn.getProperty("db.url");
        if (savedHost == null && savedUrl != null && savedUrl.startsWith("jdbc:mysql://")) {
            try {
                String cleanUrl = savedUrl.substring("jdbc:mysql://".length());
                int slashIdx = cleanUrl.indexOf('/');
                if (slashIdx != -1) {
                    String hostPort = cleanUrl.substring(0, slashIdx);
                    String dbParams = cleanUrl.substring(slashIdx + 1);
                    int colonIdx = hostPort.indexOf(':');
                    if (colonIdx != -1) {
                        savedHost = hostPort.substring(0, colonIdx);
                        savedPort = hostPort.substring(colonIdx + 1);
                    } else {
                        savedHost = hostPort;
                        savedPort = "3306";
                    }
                    int qIdx = dbParams.indexOf('?');
                    savedDb = qIdx != -1 ? dbParams.substring(0, qIdx) : dbParams;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        if (savedHost == null) savedHost = "localhost";
        if (savedPort == null) savedPort = "3306";
        if (savedDb == null) savedDb = "minimarket_yuly";
        if (savedUser == null) savedUser = "root";

        // Form fields
        cardPanel.add(createFieldPanel("Servidor / IP *", txtHost = new RoundedTextField(savedHost, null)));
        cardPanel.add(Box.createVerticalStrut(8));
        
        cardPanel.add(createFieldPanel("Puerto *", txtPort = new RoundedTextField(savedPort, null)));
        cardPanel.add(Box.createVerticalStrut(8));

        cardPanel.add(createFieldPanel("Base de Datos *", txtDatabase = new RoundedTextField(savedDb, null)));
        cardPanel.add(Box.createVerticalStrut(8));

        cardPanel.add(createFieldPanel("Usuario *", txtUser = new RoundedTextField(savedUser, null)));
        cardPanel.add(Box.createVerticalStrut(8));

        cardPanel.add(createFieldPanel("Contraseña", txtPassword = new RoundedPasswordField("", null)));
        cardPanel.add(Box.createVerticalStrut(12));

        // Status Label
        lblStatus = new JLabel(" ", JLabel.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(100, 116, 139));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblStatus);
        cardPanel.add(Box.createVerticalStrut(10));

        // Action buttons
        btnConnect = new RoundedButton("Probar y Conectar");
        btnConnect.setPreferredSize(new Dimension(300, 38));
        btnConnect.setMaximumSize(new Dimension(300, 38));
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnect.addActionListener(e -> handleConnect());
        cardPanel.add(btnConnect);
        cardPanel.add(Box.createVerticalStrut(8));

        btnDemo = new JButton("Modo Demostración (Offline)") {
            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(new Color(100, 116, 139));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        };
        btnDemo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDemo.addActionListener(e -> {
            connectionSuccessful = false;
            dispose();
        });
        cardPanel.add(btnDemo);

        centerContainer.add(cardPanel);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(310, 48));
        panel.setPreferredSize(new Dimension(310, 48));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        textField.setMaximumSize(new Dimension(310, 30));
        textField.setPreferredSize(new Dimension(310, 30));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(3));
        panel.add(textField);

        return panel;
    }

    private void handleConnect() {
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();
        String db = txtDatabase.getText().trim();
        String user = txtUser.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (host.isEmpty() || port.isEmpty() || db.isEmpty() || user.isEmpty()) {
            lblStatus.setText("Por favor, rellene todos los campos obligatorios (*)");
            lblStatus.setForeground(Color.RED);
            return;
        }

        lblStatus.setText("Probando conexión...");
        lblStatus.setForeground(new Color(24, 119, 242));
        
        // Disable connect button temporarily
        btnConnect.setEnabled(false);

        // Run connection test in a separate thread so it doesn't freeze the GUI
        new Thread(() -> {
            try {
                DatabaseConnection.getInstance().setConnectionSettings(host, port, db, user, password);
                
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("¡Conexión Exitosa! Guardando configuración...");
                    lblStatus.setForeground(new Color(34, 197, 94));
                    
                    Timer timer = new Timer(1000, e -> {
                        connectionSuccessful = true;
                        dispose();
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
            } catch (SQLException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Error: No se pudo conectar a MySQL.");
                    lblStatus.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(this, 
                        "Error de Conexión: " + ex.getMessage(), 
                        "Fallo de Conexión", 
                        JOptionPane.ERROR_MESSAGE);
                    btnConnect.setEnabled(true);
                });
            }
        }).start();
    }

    public boolean isConnectionSuccessful() {
        return connectionSuccessful;
    }
}
