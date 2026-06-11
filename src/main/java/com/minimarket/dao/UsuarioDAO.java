package com.minimarket.dao;

import com.minimarket.model.Usuario;
import java.sql.SQLException;

public interface UsuarioDAO extends CRUDDAO<Usuario, Integer> {

    Usuario login(String username, String password) throws SQLException;
}
