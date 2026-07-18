package com.minimarket.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Venta {
    private int idVenta;
    private int idProducto;
    private int cantidad;
    private BigDecimal precioTotal;
    private LocalDate fecha;
    private int idCliente;
    private String metodoPago;

    public Venta() {}

    public Venta(int idVenta, int idProducto, int cantidad, BigDecimal precioTotal, LocalDate fecha, int idCliente, String metodoPago) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.metodoPago = metodoPago;
    }

    public Venta(int idProducto, int cantidad, BigDecimal precioTotal, LocalDate fecha, int idCliente, String metodoPago) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.metodoPago = metodoPago;
    }

    public Venta(int idVenta, int idProducto, int cantidad, BigDecimal precioTotal, LocalDate fecha, int idCliente) {
        this(idVenta, idProducto, cantidad, precioTotal, fecha, idCliente, "Efectivo");
    }

    public Venta(int idProducto, int cantidad, BigDecimal precioTotal, LocalDate fecha, int idCliente) {
        this(idProducto, cantidad, precioTotal, fecha, idCliente, "Efectivo");
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "idVenta=" + idVenta +
                ", idProducto=" + idProducto +
                ", cantidad=" + cantidad +
                ", precioTotal=" + precioTotal +
                ", fecha=" + fecha +
                ", idCliente=" + idCliente +
                ", metodoPago='" + metodoPago + '\'' +
                '}';
    }
}
