package com.minimarket.model;

public class Stock {
    private int idStock;
    private int cantidad;
    private int idProducto;

    public Stock() {}

    public Stock(int idStock, int cantidad, int idProducto) {
        this.idStock = idStock;
        this.cantidad = cantidad;
        this.idProducto = idProducto;
    }

    public Stock(int cantidad, int idProducto) {
        this.cantidad = cantidad;
        this.idProducto = idProducto;
    }

    public int getIdStock() {
        return idStock;
    }

    public void setIdStock(int idStock) {
        this.idStock = idStock;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "idStock=" + idStock +
                ", cantidad=" + cantidad +
                ", idProducto=" + idProducto +
                '}';
    }
}
