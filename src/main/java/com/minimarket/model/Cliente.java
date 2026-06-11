package com.minimarket.model;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String dniRuc;

    public Cliente() {}

    public Cliente(int idCliente, String nombre, String apellidoPaterno, String apellidoMaterno, String dniRuc) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.dniRuc = dniRuc;
    }

    public Cliente(String nombre, String apellidoPaterno, String apellidoMaterno, String dniRuc) {
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.dniRuc = dniRuc;
    }

    // Legacy constructor compatibility
    public Cliente(int idCliente, String nombre, String dniRuc) {
        this.idCliente = idCliente;
        this.dniRuc = dniRuc;
        setNombreYApellidos(nombre);
    }

    // Legacy constructor compatibility
    public Cliente(String nombre, String dniRuc) {
        this.dniRuc = dniRuc;
        setNombreYApellidos(nombre);
    }

    private void setNombreYApellidos(String fullName) {
        if (fullName == null) {
            this.nombre = "";
            this.apellidoPaterno = "";
            this.apellidoMaterno = "";
            return;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0 || (parts.length == 1 && parts[0].isEmpty())) {
            this.nombre = "";
            this.apellidoPaterno = "";
            this.apellidoMaterno = "";
        } else if (parts.length == 1) {
            this.nombre = parts[0];
            this.apellidoPaterno = "";
            this.apellidoMaterno = "";
        } else if (parts.length == 2) {
            this.nombre = parts[0];
            this.apellidoPaterno = parts[1];
            this.apellidoMaterno = "";
        } else if (parts.length == 3) {
            this.nombre = parts[0];
            this.apellidoPaterno = parts[1];
            this.apellidoMaterno = parts[2];
        } else {
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 2; i++) {
                if (i > 0) nameBuilder.append(" ");
                nameBuilder.append(parts[i]);
            }
            this.nombre = nameBuilder.toString();
            this.apellidoPaterno = parts[parts.length - 2];
            this.apellidoMaterno = parts[parts.length - 1];
        }
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getDniRuc() {
        return dniRuc;
    }

    public void setDniRuc(String dniRuc) {
        this.dniRuc = dniRuc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(nombre != null ? nombre.trim() : "");
        if (apellidoPaterno != null && !apellidoPaterno.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(apellidoPaterno.trim());
        }
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(apellidoMaterno.trim());
        }
        return sb.toString();
    }
}
