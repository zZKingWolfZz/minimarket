package com.minimarket.view;

import javax.swing.*;
import java.awt.*;

public class RoundedTextField extends JTextField {
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
        setBorder(BorderFactory.createEmptyBorder(8, leftIcon != null ? 38 : 12, 8, 12));
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

        // Draw placeholder
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

        // Draw left icon
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
