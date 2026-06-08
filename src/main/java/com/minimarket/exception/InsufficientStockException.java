package com.minimarket.exception;

public class InsufficientStockException extends RuntimeException {

    private final int stockDisponible;
    private final int cantidadRequerida;

    public InsufficientStockException(String message, int stockDisponible, int cantidadRequerida) {
        super(message);
        this.stockDisponible = stockDisponible;
        this.cantidadRequerida = cantidadRequerida;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public int getCantidadRequerida() {
        return cantidadRequerida;
    }
}
