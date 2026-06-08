package com.minimarket.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.QuadCurve2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Minimarket - Portal de Negocio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // 1. PANEL PRINCIPAL: Simula el fondo gris claro de toda la pantalla (Slate 50)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));

        // Contenedor para centrar la tarjeta
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        // 2. TARJETA BLANCA CON SOMBRA Y ESQUINAS REDONDEADAS
        RoundedPanel cardPanel = new RoundedPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        // Padding interno que toma en cuenta la sombra (10px) + margen
        cardPanel.setBorder(new EmptyBorder(38, 42, 32, 42));
        cardPanel.setPreferredSize(new Dimension(380, 490));
        cardPanel.setMaximumSize(new Dimension(380, 490));

        // --- ENCABEZADO ---
        // Logotipo: Panel horizontal con Icono Circular y Texto
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblIcon = new JLabel(new MinimarketLogoIcon());
        JLabel lblLogoText = new JLabel("Minimarket");
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogoText.setForeground(new Color(24, 119, 242)); // Azul corporativo
        
        logoPanel.add(lblIcon);
        logoPanel.add(lblLogoText);

        JLabel lblTitle = new JLabel("Portal de Negocio", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(15, 23, 42)); // Gris pizarra muy oscuro
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Inicia sesión para gestionar tu inventario y ventas", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139)); // Slate 400 (Gris suave)
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- FORMULARIO ---
        
        // Sub-Panel de Usuario: Agrupa etiqueta e input con alineación izquierda
        JPanel userFieldPanel = new JPanel();
        userFieldPanel.setLayout(new BoxLayout(userFieldPanel, BoxLayout.Y_AXIS));
        userFieldPanel.setOpaque(false);
        userFieldPanel.setMaximumSize(new Dimension(300, 59));
        userFieldPanel.setPreferredSize(new Dimension(300, 59));
        userFieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = new JLabel("Usuario");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(71, 85, 105)); // Slate 600
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new RoundedTextField("Ingresa tu ID profesional", new UserIcon());
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocusInWindow();
                }
            }
        });
        txtUsername.setMaximumSize(new Dimension(300, 38));
        txtUsername.setPreferredSize(new Dimension(300, 38));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        userFieldPanel.add(lblUser);
        userFieldPanel.add(Box.createVerticalStrut(6));
        userFieldPanel.add(txtUsername);

        // Sub-Panel de Contraseña: Agrupa etiqueta e input con alineación izquierda
        JPanel passFieldPanel = new JPanel();
        passFieldPanel.setLayout(new BoxLayout(passFieldPanel, BoxLayout.Y_AXIS));
        passFieldPanel.setOpaque(false);
        passFieldPanel.setMaximumSize(new Dimension(300, 59));
        passFieldPanel.setPreferredSize(new Dimension(300, 59));
        passFieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(71, 85, 105));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new RoundedPasswordField("Ingresa tu contraseña", new LockIcon());
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Verifica si la tecla presionada es Enter
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnLogin.doClick(); // Cambiado a 'btnLogin' para evitar error de compilación
                }
            }
        });

        txtPassword.setMaximumSize(new Dimension(300, 38));
        txtPassword.setPreferredSize(new Dimension(300, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        passFieldPanel.add(lblPass);
        passFieldPanel.add(Box.createVerticalStrut(6));
        passFieldPanel.add(txtPassword);

        // Opciones Extras (Remember me / Help)
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setMaximumSize(new Dimension(300, 22));
        optionsPanel.setPreferredSize(new Dimension(300, 22));
        optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox chkRemember = new JCheckBox("Recordar este dispositivo");
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkRemember.setForeground(new Color(100, 116, 139));
        chkRemember.setBackground(Color.WHITE);
        chkRemember.setFocusPainted(false);
        chkRemember.setMargin(new Insets(0, 0, 0, 0));

        JLabel lblHelp = new JLabel("Ayuda de Login");
        lblHelp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHelp.setForeground(new Color(100, 116, 139));
        lblHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        optionsPanel.add(chkRemember, BorderLayout.WEST);
        optionsPanel.add(lblHelp, BorderLayout.EAST);

        // --- BOTÓN ACCIÓN ---
        btnLogin = new RoundedButton("Iniciar sesión en Minimarket  →");
        btnLogin.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        	}
        });
        btnLogin.setMaximumSize(new Dimension(300, 42));
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Enlace de contraseña olvidada
        JLabel lblForgot = new JLabel("¿Olvidaste tu contraseña?", JLabel.CENTER);
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForgot.setForeground(new Color(100, 116, 139));
        lblForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Label de Estado (Error/Mensajes)
        lblStatus = new JLabel("", JLabel.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setPreferredSize(new Dimension(300, 16));
        lblStatus.setMinimumSize(new Dimension(300, 16));
        lblStatus.setMaximumSize(new Dimension(300, 16));

       
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(241, 245, 249)); // Slate 100
        separator.setMaximumSize(new Dimension(300, 1));
        separator.setPreferredSize(new Dimension(300, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSecure = new JLabel(" ENCRIPTACIÓN SEGURA AES-256", JLabel.CENTER);
        lblSecure.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblSecure.setForeground(new Color(148, 163, 184)); // Slate 400
        lblSecure.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- CONSTRUCCIÓN DE LA TARJETA ---
        cardPanel.add(logoPanel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSub);
        cardPanel.add(Box.createVerticalStrut(24));

        cardPanel.add(userFieldPanel);
        cardPanel.add(Box.createVerticalStrut(14));

        cardPanel.add(passFieldPanel);
        cardPanel.add(Box.createVerticalStrut(8));

        cardPanel.add(optionsPanel);
        cardPanel.add(Box.createVerticalStrut(22));
        cardPanel.add(btnLogin);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(lblForgot);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(lblStatus);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(separator);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(lblSecure);

        // Agregar la tarjeta al contenedor centrado
        centerContainer.add(cardPanel);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // --- PIE DE PÁGINA (FOOTER EXCLUSIVO DE FONDO) ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 24, 18, 24));

        JLabel lblBuild = new JLabel("BUILD 4.2.0-PRO      SISTEMA OPERATIVO");
        lblBuild.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBuild.setForeground(new Color(148, 163, 184)); // Gris Slate muy sutil
        footerPanel.add(lblBuild, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
    }

    public void showStatusMessage(String message, boolean isError) {
        lblStatus.setText(message);
        if (isError) {
            lblStatus.setForeground(new Color(224, 49, 49)); // Rojo suave
        } else {
            lblStatus.setForeground(new Color(43, 138, 62)); // Verde suave
        }
    }

    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
    }

    // ==========================================
    // CLASES DE SOPORTE DE COMPONENTES SWING PERSONALIZADOS
    // ==========================================

    // Icono del logotipo: Círculo azul con silueta de tienda blanca
    private static class MinimarketLogoIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Círculo azul de fondo
            g2.setColor(new Color(24, 119, 242));
            g2.fillOval(x, y, 32, 32);

            // Silueta de tienda blanca
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Línea base del suelo
            g2.drawLine(x + 7, y + 23, x + 25, y + 23);

            // Edificio/Estructura principal
            g2.drawRect(x + 9, y + 16, 14, 7);

            // Puerta
            g2.fillRect(x + 14, y + 19, 4, 4);

            // Toldo trapezoidal superior (Techo)
            int[] rx = {x + 7, x + 25, x + 23, x + 9};
            int[] ry = {y + 16, y + 16, y + 12, y + 12};
            g2.fillPolygon(rx, ry, 4);

            // Rayas verticales en el toldo
            g2.setColor(new Color(24, 119, 242));
            g2.drawLine(x + 12, y + 12, x + 11, y + 16);
            g2.drawLine(x + 16, y + 12, x + 16, y + 16);
            g2.drawLine(x + 20, y + 12, x + 21, y + 16);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 32; }
        @Override
        public int getIconHeight() { return 32; }
    }

    // Icono de Usuario (Perfil)
    private static class UserIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.6f));

            // Cabeza
            g2.drawOval(x + 4, y + 1, 8, 8);
            // Hombros
            g2.drawArc(x + 1, y + 10, 14, 10, 0, 180);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }
        @Override
        public int getIconHeight() { return 18; }
    }

    // Icono de Candado
    private static class LockIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.6f));

            // Cuerpo del candado
            g2.drawRoundRect(x + 2, y + 8, 12, 8, 2, 2);
            // Asa del candado
            g2.drawArc(x + 4, y + 3, 8, 10, 0, 180);
            
            // Punto central (ojo de la cerradura)
            g2.fillOval(x + 7, y + 11, 2, 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 16; }
        @Override
        public int getIconHeight() { return 18; }
    }

    // Panel con esquinas redondeadas y sombra difuminada (Drop Shadow)
    private static class RoundedPanel extends JPanel {
        private final int radius = 20;
        private final int shadowSize = 10;

        public RoundedPanel() {
            setOpaque(false);
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dibuja múltiples capas para simular una sombra difuminada suave
            for (int i = shadowSize; i > 0; i--) {
                float opacity = 0.015f * (1.0f - (float) i / shadowSize);
                g2.setColor(new Color(0, 0, 0, opacity));
                g2.fillRoundRect(shadowSize - i, shadowSize - i + 2, getWidth() - 2 * (shadowSize - i), getHeight() - 2 * (shadowSize - i), radius, radius);
            }

            // Dibuja la tarjeta principal
            g2.setColor(getBackground());
            g2.fillRoundRect(shadowSize, shadowSize, getWidth() - 2 * shadowSize, getHeight() - 2 * shadowSize, radius, radius);

            g2.dispose();
        }
    }

    // Campo de texto redondeado con icono a la izquierda y soporte para placeholder con recorte
    private static class RoundedTextField extends JTextField {
        private final String placeholder;
        private final Icon leftIcon;
        private final Color placeholderColor = new Color(160, 174, 192);
        private final Color borderColor = new Color(226, 232, 240);
        private final Color focusColor = new Color(24, 119, 242);
        private final Color backgroundColor = new Color(248, 250, 252);
        private final int radius = 10;

        public RoundedTextField(String placeholder, Icon leftIcon) {
            this.placeholder = placeholder;
            this.leftIcon = leftIcon;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 38, 8, 12));
            setBackground(backgroundColor);
            setForeground(new Color(15, 23, 42));
            setCaretColor(new Color(15, 23, 42));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            addFocusListener(new java.awt.event.FocusListener() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g2);

            // Dibuja marcador de posición con plano de recorte
            if (getText().isEmpty()) {
                g2.setColor(placeholderColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                int maxWidth = getWidth() - getInsets().left - getInsets().right;
                Shape oldClip = g2.getClip();
                g2.clipRect(x, 0, maxWidth, getHeight());
                g2.drawString(placeholder, x, y);
                g2.setClip(oldClip);
            }

            // Dibuja icono izquierdo
            if (leftIcon != null) {
                int iconWidth = leftIcon.getIconWidth();
                int iconHeight = leftIcon.getIconHeight();
                int x = 12;
                int y = (getHeight() - iconHeight) / 2;
                leftIcon.paintIcon(this, g2, x, y);
            }

            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hasFocus() ? focusColor : borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // Campo de contraseña redondeado con icono de candado, placeholder con recorte e icono interactivo de ojo a la derecha
    private static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;
        private final Icon leftIcon;
        private boolean isPasswordVisible = false;
        private final char defaultEchoChar;
        private final Color placeholderColor = new Color(160, 174, 192);
        private final Color borderColor = new Color(226, 232, 240);
        private final Color focusColor = new Color(24, 119, 242);
        private final Color backgroundColor = new Color(248, 250, 252);
        private final int radius = 10;

        public RoundedPasswordField(String placeholder, Icon leftIcon) {
            this.placeholder = placeholder;
            this.leftIcon = leftIcon;
            this.defaultEchoChar = getEchoChar();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 38, 8, 38));
            setBackground(backgroundColor);
            setForeground(new Color(15, 23, 42));
            setCaretColor(new Color(15, 23, 42));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            addFocusListener(new java.awt.event.FocusListener() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });

            // Detecta clics sobre el área del ojo interactivo
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    int x = e.getX();
                    if (x >= getWidth() - 36) {
                        togglePasswordVisibility();
                    }
                }
            });

            // Cambia el cursor a MANO al flotar sobre el ojo
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    int x = e.getX();
                    if (x >= getWidth() - 36) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    }
                }
            });
        }

        private void togglePasswordVisibility() {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                setEchoChar((char) 0);
            } else {
                setEchoChar(defaultEchoChar);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g2);

            // Dibuja placeholder con plano de recorte
            if (getPassword().length == 0) {
                g2.setColor(placeholderColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                int maxWidth = getWidth() - getInsets().left - getInsets().right;
                Shape oldClip = g2.getClip();
                g2.clipRect(x, 0, maxWidth, getHeight());
                g2.drawString(placeholder, x, y);
                g2.setClip(oldClip);
            }

            // Dibuja icono izquierdo
            if (leftIcon != null) {
                int iconWidth = leftIcon.getIconWidth();
                int iconHeight = leftIcon.getIconHeight();
                int x = 12;
                int y = (getHeight() - iconHeight) / 2;
                leftIcon.paintIcon(this, g2, x, y);
            }

            // Dibuja icono de ojo interactivo a la derecha
            int eyeX = getWidth() - 28;
            int eyeY = (getHeight() - 16) / 2;
            paintEyeIcon(g2, eyeX, eyeY, isPasswordVisible);

            g2.dispose();
        }

        private void paintEyeIcon(Graphics2D g2, int x, int y, boolean visible) {
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Curva superior del ojo
            g2.draw(new QuadCurve2D.Float(x, y + 8, x + 8, y, x + 16, y + 8));
            // Curva inferior del ojo
            g2.draw(new QuadCurve2D.Float(x, y + 8, x + 8, y + 16, x + 16, y + 8));

            // Pupila central
            g2.fillOval(x + 5, y + 5, 6, 6);

            if (visible) {
                // Barra diagonal (ojo tachado = contraseña visible)
                g2.setColor(new Color(148, 163, 184));
                g2.drawLine(x + 2, y + 2, x + 14, y + 14);
            }
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hasFocus() ? focusColor : borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    // Botón redondeado de estilo corporativo moderno (Azul) con hover animado
    private static class RoundedButton extends JButton {
        private final Color normalColor = new Color(24, 119, 242);
        private final Color hoverColor = new Color(13, 102, 220);
        private final Color pressedColor = new Color(10, 80, 180);
        private final int radius = 10;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(normalColor);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}