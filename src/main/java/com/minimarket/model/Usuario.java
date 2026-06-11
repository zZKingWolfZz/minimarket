package com.minimarket.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private int idUsuario;
    private String username;
    private String password;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private int estado; 
    private List<Rol> roles = new ArrayList<>();

    public Usuario() {}

    public Usuario(int idUsuario, String username, String password, String nombre, String apellidoPaterno, String apellidoMaterno, int estado) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.estado = estado;
    }

    public Usuario(int idUsuario, String username, String password, String nombreCompleto, int estado) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.estado = estado;
        setNombreCompletos(nombreCompleto);
    }

    public Usuario(String username, String password, String nombreCompleto, int estado) {
        this.username = username;
        this.password = password;
        this.estado = estado;
        setNombreCompletos(nombreCompleto);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getNombreCompletos() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null && !nombre.trim().isEmpty()) {
            sb.append(nombre.trim());
        }
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

    public void setNombreCompletos(String nombreCompleto) {
        if (nombreCompleto == null) {
            this.nombre = "";
            this.apellidoPaterno = "";
            this.apellidoMaterno = "";
            return;
        }
        String[] parts = nombreCompleto.trim().split("\\s+");
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public void addRol(Rol rol) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(rol);
    }

    @Override
    public String toString() {
        return getNombreCompletos() + " (" + username + ")";
    }
}
