package com.minimarket.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class IconUtil {
    public static void setWindowIcon(Window window) {
        try {
            URL imgURL = IconUtil.class.getResource("/logo.png");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                window.setIconImage(icon.getImage());
            } else {
                System.err.println("Icon logo.png not found in resources!");
            }
        } catch (Exception e) {
            System.err.println("Error setting window icon: " + e.getMessage());
        }
    }
}
