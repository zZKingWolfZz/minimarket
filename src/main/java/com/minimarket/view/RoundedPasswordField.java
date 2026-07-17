package com.minimarket.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;

public class RoundedPasswordField extends JPasswordField {
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
        setBorder(BorderFactory.createEmptyBorder(8, leftIcon != null ? 38 : 12, 8, 38));
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

        // Detect eye click
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int x = e.getX();
                if (x >= getWidth() - 36) {
                    togglePasswordVisibility();
                }
            }
        });

        // Hand cursor over eye
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

        // Draw placeholder
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

        // Draw left icon
        if (leftIcon != null) {
            int iconWidth = leftIcon.getIconWidth();
            int iconHeight = leftIcon.getIconHeight();
            int x = 12;
            int y = (getHeight() - iconHeight) / 2;
            leftIcon.paintIcon(this, g2, x, y);
        }

        // Draw interactive eye icon on the right
        int eyeX = getWidth() - 28;
        int eyeY = (getHeight() - 16) / 2;
        paintEyeIcon(g2, eyeX, eyeY, isPasswordVisible);

        g2.dispose();
    }

    private void paintEyeIcon(Graphics2D g2, int x, int y, boolean visible) {
        g2.setColor(new Color(148, 163, 184));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Upper eyelid
        g2.draw(new QuadCurve2D.Float(x, y + 8, x + 8, y, x + 16, y + 8));
        // Lower eyelid
        g2.draw(new QuadCurve2D.Float(x, y + 8, x + 8, y + 16, x + 16, y + 8));

        // Pupil
        g2.fillOval(x + 5, y + 5, 6, 6);

        if (visible) {
            // Crossed eye bar
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
