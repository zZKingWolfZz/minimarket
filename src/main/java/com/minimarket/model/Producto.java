package com.minimarket.model;

import java.math.BigDecimal;

public class Producto {
    private int idProducto;
    private String nombreProducto;
    private BigDecimal precioUnitario;
    private int idCategoria;
    private String codigoBarras;

    public Producto() {}

    public Producto(int idProducto, String nombreProducto, BigDecimal precioUnitario, int idCategoria) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.idCategoria = idCategoria;
    }

    public Producto(int idProducto, String nombreProducto, BigDecimal precioUnitario, int idCategoria, String codigoBarras) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.idCategoria = idCategoria;
        this.codigoBarras = codigoBarras;
    }



    public Producto(String nombreProducto, BigDecimal precioUnitario, int idCategoria) {
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.idCategoria = idCategoria;
    }

    public Producto(String nombreProducto, BigDecimal precioUnitario, int idCategoria, String codigoBarras) {
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.idCategoria = idCategoria;
        this.codigoBarras = codigoBarras;
    }



    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }



    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", precioUnitario=" + precioUnitario +
                ", idCategoria=" + idCategoria +
                ", codigoBarras='" + codigoBarras + '\'' +
                '}';
    }
}
