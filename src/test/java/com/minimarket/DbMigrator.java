package com.minimarket;

import com.minimarket.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbMigrator {

    public static void main(String[] args) {
        System.out.println("Starting Database Migration...");
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            if (connection == null) {
                System.err.println("Failed to obtain database connection!");
                return;
            }

            // Step 1: Add columns if they do not exist
            try (Statement stmt = connection.createStatement()) {
                System.out.println("Adding new columns: nombre, apellido_paterno, apellido_materno...");
                try {
                    stmt.execute("ALTER TABLE usuario ADD COLUMN nombre VARCHAR(100)");
                    System.out.println("Column 'nombre' added.");
                } catch (SQLException e) {
                    System.out.println("Column 'nombre' already exists or could not be added: " + e.getMessage());
                }

                try {
                    stmt.execute("ALTER TABLE usuario ADD COLUMN apellido_paterno VARCHAR(100)");
                    System.out.println("Column 'apellido_paterno' added.");
                } catch (SQLException e) {
                    System.out.println("Column 'apellido_paterno' already exists or could not be added: " + e.getMessage());
                }

                try {
                    stmt.execute("ALTER TABLE usuario ADD COLUMN apellido_materno VARCHAR(100)");
                    System.out.println("Column 'apellido_materno' added.");
                } catch (SQLException e) {
                    System.out.println("Column 'apellido_materno' already exists or could not be added: " + e.getMessage());
                }
            }

            // Step 2: Migrate data from nombre_completo if the column exists
            boolean nombreCompletoExists = false;
            try (Statement stmt = connection.createStatement()) {
                // Test if nombre_completo is still there
                try (ResultSet rs = stmt.executeQuery("SELECT nombre_completo FROM usuario LIMIT 1")) {
                    nombreCompletoExists = true;
                    System.out.println("Column 'nombre_completo' exists. Preparing migration...");
                } catch (SQLException e) {
                    System.out.println("Column 'nombre_completo' does not exist or has already been dropped.");
                }
            }

            if (nombreCompletoExists) {
                List<UserRecord> records = new ArrayList<>();
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT Id_usuario, nombre_completo FROM usuario")) {
                    while (rs.next()) {
                        records.add(new UserRecord(rs.getInt("Id_usuario"), rs.getString("nombre_completo")));
                    }
                }

                System.out.println("Found " + records.size() + " user records to migrate.");

                String updateSql = "UPDATE usuario SET nombre = ?, apellido_paterno = ?, apellido_materno = ? WHERE Id_usuario = ?";
                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    for (UserRecord r : records) {
                        String full = r.nombreCompleto;
                        String nombre = "";
                        String apPaterno = "";
                        String apMaterno = "";

                        if (full != null) {
                            String[] parts = full.trim().split("\\s+");
                            if (parts.length == 1) {
                                nombre = parts[0];
                            } else if (parts.length == 2) {
                                nombre = parts[0];
                                apPaterno = parts[1];
                            } else if (parts.length == 3) {
                                nombre = parts[0];
                                apPaterno = parts[1];
                                apMaterno = parts[2];
                            } else if (parts.length >= 4) {
                                StringBuilder nameBuilder = new StringBuilder();
                                for (int i = 0; i < parts.length - 2; i++) {
                                    if (i > 0) nameBuilder.append(" ");
                                    nameBuilder.append(parts[i]);
                                }
                                nombre = nameBuilder.toString();
                                apPaterno = parts[parts.length - 2];
                                apMaterno = parts[parts.length - 1];
                            }
                        }

                        ps.setString(1, nombre);
                        ps.setString(2, apPaterno);
                        ps.setString(3, apMaterno);
                        ps.setInt(4, r.idUsuario);
                        ps.addBatch();
                        System.out.println("Migrating '" + full + "' -> Nombre: '" + nombre + "', Paterno: '" + apPaterno + "', Materno: '" + apMaterno + "'");
                    }
                    ps.executeBatch();
                    System.out.println("Data migration completed successfully.");
                }

                // Step 3: Drop the old column
                System.out.println("Dropping old column 'nombre_completo'...");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE usuario DROP COLUMN nombre_completo");
                    System.out.println("Column 'nombre_completo' dropped.");
                } catch (SQLException e) {
                    System.err.println("Could not drop column 'nombre_completo': " + e.getMessage());
                }
            }

            System.out.println("Database migration completed successfully!");
        } catch (Exception e) {
            System.err.println("Error running database migration:");
            e.printStackTrace();
        }
    }

    private static class UserRecord {
        int idUsuario;
        String nombreCompleto;

        UserRecord(int idUsuario, String nombreCompleto) {
            this.idUsuario = idUsuario;
            this.nombreCompleto = nombreCompleto;
        }
    }
}
