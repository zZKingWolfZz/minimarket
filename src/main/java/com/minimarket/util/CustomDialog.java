package com.minimarket.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class CustomDialog {

    public static void showInfo(Component parent, Object message, String title) {
        show(parent, title, message, "info", false, false, null);
    }

    public static void showWarning(Component parent, Object message, String title) {
        show(parent, title, message, "warning", false, false, null);
    }

    public static void showError(Component parent, Object message, String title) {
        show(parent, title, message, "error", false, false, null);
    }

    public static void showSuccess(Component parent, Object message, String title) {
        show(parent, title, message, "success", false, false, null);
    }

    public static boolean showConfirm(Component parent, Object message, String title) {
        return show(parent, title, message, "question", true, false, null);
    }

    public static String showInput(Component parent, Object message, String title, String defaultValue) {
        DialogWindow dw = createDialog(parent, title, message, "question", true, true, defaultValue);
        dw.setVisible(true);
        return dw.accepted ? dw.inputResult : null;
    }

    private static boolean show(Component parent, String title, Object message, String type, boolean showCancel, boolean isInput, String defaultValue) {
        DialogWindow dw = createDialog(parent, title, message, type, showCancel, isInput, defaultValue);
        dw.setVisible(true);
        return dw.accepted;
    }

    private static DialogWindow createDialog(Component parent, String title, Object message, String type, boolean showCancel, boolean isInput, String defaultValue) {
        Window owner = null;
        if (parent != null) {
            if (parent instanceof Window) {
                owner = (Window) parent;
            } else {
                owner = SwingUtilities.getWindowAncestor(parent);
            }
        }
        return new DialogWindow(owner, title, message, type, showCancel, isInput, defaultValue);
    }

    private static class DialogWindow extends JDialog {
        private boolean accepted = false;
        private String inputResult = null;

        public DialogWindow(Window owner, String title, Object message, String type, boolean showCancel, boolean isInput, String defaultValue) {
            super(owner, Dialog.ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0)); // Transparent window background for true rounded shape

            JPanel mainPanel = new JPanel(new BorderLayout(20, 20)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(new Color(226, 232, 240)); // Slate 200 border
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                    g2.dispose();
                }
            };
            mainPanel.setOpaque(false);
            mainPanel.setBorder(new EmptyBorder(24, 24, 20, 24));

            // Dragging listener
            final Point[] dragStart = new Point[1];
            mainPanel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragStart[0] = e.getPoint();
                }
            });
            mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - dragStart[0].x, p.y + e.getY() - dragStart[0].y);
                }
            });

            // Column left: Icon
            CustomIcon icon = new CustomIcon(type);
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setVerticalAlignment(SwingConstants.TOP);

            // Column right: Text and Field
            JPanel pnlText = new JPanel();
            pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
            pnlText.setOpaque(false);

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setForeground(new Color(15, 23, 42)); // Slate 900
            lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlText.add(lblTitle);
            pnlText.add(Box.createVerticalStrut(8));

            int width = 360;
            if (message instanceof JComponent) {
                JComponent comp = (JComponent) message;
                comp.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlText.add(comp);
                width = Math.max(360, comp.getPreferredSize().width + 80);
            } else {
                String msgStr = message != null ? message.toString() : "";
                JTextArea txtMessage = new JTextArea(msgStr);
                txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                txtMessage.setForeground(new Color(71, 85, 105)); // Slate 600
                txtMessage.setLineWrap(true);
                txtMessage.setWrapStyleWord(true);
                txtMessage.setEditable(false);
                txtMessage.setOpaque(false);
                txtMessage.setBackground(new Color(0, 0, 0, 0));
                txtMessage.setBorder(null);
                txtMessage.setFocusable(false);
                txtMessage.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Adjust window width according to message length
                width = Math.min(520, Math.max(340, 10 * msgStr.length()));
                txtMessage.setSize(new Dimension(width - 80, 1));
                txtMessage.setPreferredSize(new Dimension(width - 80, txtMessage.getPreferredSize().height));
                pnlText.add(txtMessage);
            }

            JTextField txtInput = null;
            if (isInput) {
                pnlText.add(Box.createVerticalStrut(12));
                txtInput = new JTextField(defaultValue != null ? defaultValue : "");
                txtInput.setPreferredSize(new Dimension(width - 80, 36));
                txtInput.setMaximumSize(new Dimension(width - 80, 36));
                txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                txtInput.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                        new EmptyBorder(0, 10, 0, 10)));
                txtInput.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlText.add(txtInput);

                final JTextField fInput = txtInput;
                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowOpened(WindowEvent e) {
                        fInput.requestFocusInWindow();
                    }
                });
            }

            JPanel pnlLeftRight = new JPanel(new BorderLayout(16, 0));
            pnlLeftRight.setOpaque(false);
            pnlLeftRight.add(lblIcon, BorderLayout.WEST);
            pnlLeftRight.add(pnlText, BorderLayout.CENTER);

            // Footer buttons
            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlFooter.setOpaque(false);

            JButton btnCancel = null;
            if (showCancel) {
                btnCancel = createButton("Cancelar", false);
                final DialogWindow dw = this;
                btnCancel.addActionListener(e -> {
                    accepted = false;
                    dw.dispose();
                });
                pnlFooter.add(btnCancel);
            }

            JButton btnOk = createButton(isInput ? "Aceptar" : (showCancel ? "Sí" : "Aceptar"), true);
            final DialogWindow dw = this;
            final JTextField finalTxtInput = txtInput;
            btnOk.addActionListener(e -> {
                accepted = true;
                if (isInput && finalTxtInput != null) {
                    inputResult = finalTxtInput.getText();
                }
                dw.dispose();
            });
            pnlFooter.add(btnOk);

            if (isInput && txtInput != null) {
                txtInput.addActionListener(e -> btnOk.doClick());
            }

            mainPanel.add(pnlLeftRight, BorderLayout.CENTER);
            mainPanel.add(pnlFooter, BorderLayout.SOUTH);

            setContentPane(mainPanel);
            pack();

            try {
                setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            } catch (Exception e) {
                // Shape transparency not supported on some vintage platforms
            }

            setLocationRelativeTo(owner);
        }

        private JButton createButton(String text, boolean primary) {
            JButton btn = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (primary) {
                        if (getModel().isPressed()) {
                            g2.setColor(new Color(21, 128, 61)); // Dark green/blue
                        } else if (getModel().isRollover()) {
                            g2.setColor(new Color(22, 163, 74));
                        } else {
                            g2.setColor(new Color(24, 119, 242)); // Primary blue
                        }
                    } else {
                        if (getModel().isPressed()) {
                            g2.setColor(new Color(226, 232, 240));
                        } else if (getModel().isRollover()) {
                            g2.setColor(new Color(241, 245, 249));
                        } else {
                            g2.setColor(new Color(248, 250, 252));
                        }
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    if (!primary) {
                        g2.setColor(new Color(226, 232, 240));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(primary ? Color.WHITE : new Color(71, 85, 105));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(8, 16, 8, 16));
            btn.setPreferredSize(new Dimension(100, 34));
            return btn;
        }
    }

    private static class CustomIcon implements Icon {
        private final String type;

        public CustomIcon(String type) {
            this.type = type;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if ("info".equals(type)) {
                g2.setColor(new Color(59, 130, 246)); // Blue Info
                g2.fillOval(x + 2, y + 2, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("i", x + 13, y + 22);
            } else if ("warning".equals(type)) {
                g2.setColor(new Color(245, 158, 11)); // Amber Warning
                int[] px = {x + 16, x + 2, x + 30};
                int[] py = {y + 2, y + 30, y + 30};
                g2.fillPolygon(px, py, 3);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                g2.drawString("!", x + 13, y + 25);
            } else if ("error".equals(type)) {
                g2.setColor(new Color(239, 68, 68)); // Red Error
                g2.fillOval(x + 2, y + 2, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(x + 10, y + 10, x + 22, y + 22);
                g2.drawLine(x + 22, y + 10, x + 10, y + 22);
            } else if ("success".equals(type)) {
                g2.setColor(new Color(34, 197, 94)); // Green Success
                g2.fillOval(x + 2, y + 2, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(x + 9, y + 16, x + 14, y + 21);
                g2.drawLine(x + 14, y + 21, x + 23, y + 11);
            } else if ("question".equals(type)) {
                g2.setColor(new Color(139, 92, 246)); // Purple Question
                g2.fillOval(x + 2, y + 2, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("?", x + 11, y + 22);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 32;
        }

        @Override
        public int getIconHeight() {
            return 32;
        }
    }
}
