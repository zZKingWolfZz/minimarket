package com.minimarket.dao;

import com.minimarket.model.Venta;
import java.sql.SQLException;

public interface VentaDAO extends CRUDDAO<Venta, Integer> {

    boolean registrarVentaTransaccional(Venta venta) throws SQLException;
}
