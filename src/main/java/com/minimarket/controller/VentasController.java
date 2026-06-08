package com.minimarket.controller;

import com.minimarket.dao.ClienteDAO;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.VentaDAO;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.model.Cliente;
import com.minimarket.model.Producto;
import com.minimarket.model.Venta;
import com.minimarket.view.VentasView;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VentasController {

    private final VentasView view;
    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;

    public VentasController(VentasView view, VentaDAO ventaDAO, ProductoDAO productoDAO, ClienteDAO clienteDAO) {
        this.view = view;
        this.ventaDAO = ventaDAO;
        this.productoDAO = productoDAO;
        this.clienteDAO = clienteDAO;

        initListeners();
    }

    private void initListeners() {
        view.addRegistrarVentaListener(new RegistrarVentaListener());
        view.addProductoSelectionListener(new UpdatePriceListener());
        view.addCantidadSelectionListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                calcularTotal();
            }
        });
    }

    public VentasView getView() {
        return view;
    }

    public void initData() {
        try {
            // Limpiar carrito y campos de simulación para iniciar completamente en blanco en tiempo de ejecución
            view.clearFields();

            List<Producto> productos = productoDAO.findAll();
            List<Cliente> clientes = clienteDAO.findAll();
            List<Venta> ventas = ventaDAO.findAll();

            view.setProductos(productos);
            view.setClientes(clientes);
            view.setSalesTableData(ventas, productos, clientes);

            calcularTotal();
        } catch (SQLException ex) {
            view.mostrarMensaje("Error cargando datos: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    public void calcularTotal() {
        Producto producto = view.getSelectedProducto();
        int cantidad = view.getCantidad();
        if (producto != null) {
            BigDecimal precio = producto.getPrecioUnitario();
            BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
            view.setPrecioTotal(total);
        } else {
            view.setPrecioTotal(BigDecimal.ZERO);
        }
    }

    private class UpdatePriceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            calcularTotal();
        }
    }

    private class RegistrarVentaListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String dni = view.getDniCliente();
            if (dni.isEmpty() || dni.equals("Ingrese DNI de Cliente...")) {
                view.mostrarMensaje("La venta no está permitida si no se ha asignado un cliente. Por favor, ingrese el DNI del cliente.", true);
                return;
            }
            if (dni.length() != 8) {
                view.mostrarMensaje("El DNI del cliente debe tener exactamente 8 dígitos.", true);
                return;
            }

            Cliente cliente = view.getSelectedCliente();
            if (cliente == null) {
                view.mostrarMensaje("El cliente con DNI " + dni + " es nuevo y no está registrado en el sistema. Por favor, ingrese su Nombre y Apellido para poder agregarlo y proceder con la venta.", true);
                return;
            }
            java.util.List<VentasView.CartItem> items = view.getCartItems();
            if (items == null || items.isEmpty()) {
                view.mostrarMensaje("El carrito de compras está vacío. Agregue productos antes de pagar.", true);
                return;
            }

            try {
                // If the client was fetched from RENIEC but is not registered locally, register them now!
                if (cliente.getIdCliente() == 0) {
                    clienteDAO.insert(cliente); // This inserts and populates client.getIdCliente()
                }

                int registrados = 0;
                for (VentasView.CartItem item : items) {
                    Venta venta = new Venta(
                            item.getProducto().getIdProducto(),
                            item.getCantidad(),
                            item.getSubtotal(),
                            LocalDate.now(),
                            cliente.getIdCliente()
                    );
                    ventaDAO.registrarVentaTransaccional(venta);
                    registrados++;
                }
                view.mostrarMensaje("Venta registrada con éxito para " + registrados + " producto(s). Inventario de stock disminuido.", false);
                view.clearFields();
                initData(); 
            } catch (InsufficientStockException ex) {
                view.mostrarMensaje("Error: " + ex.getMessage(), true);
            } catch (SQLException ex) {
                view.mostrarMensaje("Error de Base de Datos: " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        }
    }
}
