package com.minimarket;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class ConvertPngToIco {
    public static void main(String[] args) throws Exception {
        File pngFile = new File("C:/Users/arnie/.gemini/antigravity-ide/scratch/minimarket-arquitectura-unificado/images/OIP.png");
        File icoFile = new File("C:/Users/arnie/.gemini/antigravity-ide/scratch/minimarket-arquitectura-unificado/images/minimarket.ico");

        if (!pngFile.exists()) {
            System.err.println("OIP.png not found!");
            System.exit(1);
        }

        byte[] pngData = Files.readAllBytes(pngFile.toPath());
        int pngSize = pngData.length;

        try (FileOutputStream fos = new FileOutputStream(icoFile)) {
            // ICO Header: 6 bytes
            fos.write(new byte[]{0, 0}); // Reserved
            fos.write(new byte[]{1, 0}); // Type (1 = Icon)
            fos.write(new byte[]{1, 0}); // Count (1)

            // Directory Entry: 16 bytes
            fos.write(0); // Width (0 = 256)
            fos.write(0); // Height (0 = 256)
            fos.write(0); // Color count
            fos.write(0); // Reserved

            fos.write(new byte[]{1, 0}); // Planes
            fos.write(new byte[]{32, 0}); // BPP (32)

            // Image size (4 bytes, little-endian)
            fos.write((byte) (pngSize & 0xFF));
            fos.write((byte) ((pngSize >> 8) & 0xFF));
            fos.write((byte) ((pngSize >> 16) & 0xFF));
            fos.write((byte) ((pngSize >> 24) & 0xFF));

            // Image offset (4 bytes, little-endian) = 22 (0x16)
            fos.write(new byte[]{22, 0, 0, 0});

            // Write PNG bytes
            fos.write(pngData);
        }

        System.out.println("Success: Generated minimarket.ico");
    }
}
