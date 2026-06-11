package com.minimarket.dao;

import com.minimarket.model.Stock;
import java.sql.SQLException;
import java.util.List;

public interface StockDAO extends CRUDDAO<Stock, Integer> {

    List<Stock> alertaStockMinimo(int limite) throws SQLException;

    Stock findByProductoId(int idProducto) throws SQLException;
}
