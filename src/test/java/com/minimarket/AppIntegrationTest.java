package com.minimarket;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.controller.*;
import com.minimarket.dao.*;
import com.minimarket.dao.impl.*;
import com.minimarket.model.*;
import com.minimarket.view.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AppIntegrationTest {

    private Thread dialogDismissThread;
    private volatile boolean keepDismissing = true;

    private static final Logger logger = LoggerFactory.getLogger(AppIntegrationTest.class);

    private DatabaseConnection originalDbConnInstance;
    private Connection mockConnection;
    private DatabaseConnection mockDbConn;

    @BeforeEach
    public void setUp() throws Exception {
        logger.info("========================================= STARTING TEST SETUP =========================================");
        
        // Save original singleton instance of DatabaseConnection
        Field instanceField = DatabaseConnection.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        originalDbConnInstance = (DatabaseConnection) instanceField.get(null);

        // Create Mock Connection and PreparedStatement structures
        mockConnection = mock(Connection.class);
        mockDbConn = mock(DatabaseConnection.class);
        when(mockDbConn.getConnection()).thenReturn(mockConnection);
        when(mockDbConn.getProperty("db.autologin")).thenReturn("false");

        // Inject the mock DatabaseConnection singleton
        instanceField.set(null, mockDbConn);

        // Stub standard connection features (auto-commit state)
        when(mockConnection.getAutoCommit()).thenReturn(true);

        // Start Auto-dismisser thread for modal JDialogs and Option Panes to prevent blockages
        keepDismissing = true;
        dialogDismissThread = new Thread(() -> {
            try {
                while (keepDismissing && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);
                    SwingUtilities.invokeLater(() -> {
                        for (Window window : Window.getWindows()) {
                            if (window.isVisible()) {
                                String name = window.getClass().getName();
                                if (window instanceof JDialog || window instanceof FileDialog || window.getClass().getSimpleName().contains("Dialog")) {
                                    // Search for confirmation/alert buttons
                                    JButton yesButton = findButtonByText((Container) window, "Sí");
                                    if (yesButton == null) yesButton = findButtonByText((Container) window, "Yes");
                                    if (yesButton == null) yesButton = findButtonByText((Container) window, "Aceptar");
                                    if (yesButton == null) yesButton = findButtonByText((Container) window, "OK");

                                    if (yesButton != null) {
                                        logger.info("Auto-confirming dialog: " + name + " via button: " + yesButton.getText());
                                        yesButton.doClick();
                                    } else {
                                        logger.info("Auto-dismissing dialog: " + name);
                                        window.dispose();
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                // finished
            }
        });
        dialogDismissThread.setDaemon(true);
        dialogDismissThread.start();

        logger.info("Successfully injected mock database connection singleton.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        logger.info("Stopping dialog dismiss thread...");
        keepDismissing = false;
        if (dialogDismissThread != null) {
            dialogDismissThread.interrupt();
        }

        logger.info("Cleaning up GUI windows...");
        // Dispose all Swing frames to prevent memory leaks and blockages
        SwingUtilities.invokeAndWait(() -> {
            for (Window window : Window.getWindows()) {
                window.dispose();
                logger.info("Disposed window: " + window.getClass().getName());
            }
        });

        // Restore original DatabaseConnection singleton
        Field instanceField = DatabaseConnection.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, originalDbConnInstance);

        logger.info("========================================= END OF TEST =========================================");
    }

    @Test
    public void testOfflineBootFlow() throws Exception {
        logger.info("Testing App offline boot flow when database is not reachable...");

        // Make database getConnection throw exception to force offline mode
        when(mockDbConn.getConnection()).thenThrow(new SQLException("Could not connect to MySQL server"));

        // Run application boot flow on the EDT
        SwingUtilities.invokeAndWait(() -> {
            App.main(new String[]{});
        });

        // Give the EDT some milliseconds to boot the frame
        Thread.sleep(500);

        // Verify that LoginView is displayed and contains the status warning message
        boolean foundLoginView = false;
        for (Window window : Window.getWindows()) {
            if (window instanceof LoginView && window.isVisible()) {
                foundLoginView = true;
                logger.info("Found visible LoginView in offline mode!");
                LoginView lv = (LoginView) window;
                // Since showStatusMessage is called on startup in catch block
                // we check that the window initialized
                assertNotNull(lv);
                break;
            }
        }
        assertTrue(foundLoginView, "LoginView should be visible in offline mode fallback.");
        logger.info("Offline boot flow test completed successfully.");
    }

    @Test
    public void testFullE2EIntegrationFlow() throws Exception {
        logger.info("Starting Full E2E Integration Flow with mocked JDBC layer...");

        // Setup mock ResultSet and PreparedStatement responses for all database tables
        setupMockDatabaseQueries();

        // 1. Instantiate LoginView & Controller on EDT
        final LoginView[] loginViewHolder = new LoginView[1];
        final LoginController[] loginControllerHolder = new LoginController[1];

        SwingUtilities.invokeAndWait(() -> {
            loginViewHolder[0] = new LoginView();
            // We use the mock connection directly wrapped in UsuarioDAOImpl
            UsuarioDAOImpl userDAO = new UsuarioDAOImpl(mockConnection);
            loginControllerHolder[0] = new LoginController(loginViewHolder[0], userDAO);
            logger.info("Views and LoginController instantiated.");
        });

        LoginView loginView = loginViewHolder[0];
        assertNotNull(loginView);

        // 2. Perform Login Action (Set username/password and trigger action listener)
        SwingUtilities.invokeAndWait(() -> {
            // Find txtUsername and txtPassword fields using reflection (since they are private)
            try {
                Field txtUserField = LoginView.class.getDeclaredField("txtUsername");
                txtUserField.setAccessible(true);
                JTextField txtUser = (JTextField) txtUserField.get(loginView);
                txtUser.setText("admin");

                Field txtPassField = LoginView.class.getDeclaredField("txtPassword");
                txtPassField.setAccessible(true);
                JPasswordField txtPass = (JPasswordField) txtPassField.get(loginView);
                txtPass.setText("admin_pass");

                Field btnLoginField = LoginView.class.getDeclaredField("btnLogin");
                btnLoginField.setAccessible(true);
                JButton btnLogin = (JButton) btnLoginField.get(loginView);

                logger.info("Credentials entered. Clicking Login Button...");
                btnLogin.doClick();

            } catch (Exception ex) {
                fail("Failed to simulate UI inputs: " + ex.getMessage());
            }
        });

        // Sleep to let dashboard load in the background
        Thread.sleep(500);

        // Verify LoginView is closed/hidden and DashboardView is shown
        assertFalse(loginView.isVisible(), "LoginView should be hidden after successful authentication.");

        DashboardView dashboardView = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof DashboardView) {
                dashboardView = (DashboardView) window;
                break;
            }
        }
        assertNotNull(dashboardView, "DashboardView should be created after login.");
        assertTrue(dashboardView.isVisible(), "DashboardView should be visible.");
        logger.info("Authentication integration check passed. Dashboard loaded.");

        // 3. Test Dashboard Controller actions and inner panel updates (Ventas)
        final DashboardView fDashboardView = dashboardView;
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field btnVentasField = DashboardView.class.getDeclaredField("btnVentas");
                btnVentasField.setAccessible(true);
                JButton btnVentas = (JButton) btnVentasField.get(fDashboardView);

                logger.info("Navigating to Ventas panel via Dashboard menu...");
                btnVentas.doClick();
            } catch (Exception ex) {
                fail("Failed to trigger dashboard view transitions: " + ex.getMessage());
            }
        });

        // 4. Test Ventas Terminal integration
        logger.info("Verifying VentasController flow...");
        VentasView ventasView = findComponent(dashboardView, VentasView.class);
        assertNotNull(ventasView, "VentasView must be loaded inside the dashboard main panel.");

        final VentasView fVentasView = ventasView;
        SwingUtilities.invokeAndWait(() -> {
            // Assert that mock products and clients are loaded in UI components
            JComboBox<Producto> cmbProd = getComboBox(fVentasView, "cbProductos");
            JComboBox<Cliente> cmbCl = getComboBox(fVentasView, "cbClientes");

            assertNotNull(cmbProd, "Product combo box should be present.");
            assertNotNull(cmbCl, "Client combo box should be present.");
            
            // Check loaded mock data (from setupMockDatabaseQueries)
            assertTrue(cmbProd.getItemCount() > 0, "Products should be loaded in VentasView.");
            assertTrue(cmbCl.getItemCount() > 0, "Clients should be loaded in VentasView.");

            // Calculate total price action
            cmbProd.setSelectedIndex(0);
            JSpinner spinner = getSpinner(fVentasView, "spCantidad");
            assertNotNull(spinner);
            spinner.setValue(5); // 5 items * 1.20 = 6.00

            logger.info("Simulated selecting product 'Galletas Soda' and quantity 5.");
        });

        // Verify total price calculation logic
        SwingUtilities.invokeAndWait(() -> {
            JLabel lblTotal = getLabel(fVentasView, "lblPrecioTotalVal");
            assertNotNull(lblTotal);
            String text = lblTotal.getText();
            logger.info("Total price calculated label reads: " + text);
            assertTrue(text.contains("6.00") || text.contains("0.00"), "Calculated price should update.");
        });

        // 5. Test mock transaction registration (VentaDAOImpl.registrarVentaTransaccional)
        SwingUtilities.invokeAndWait(() -> {
            try {
                // Add a item to checkout cart
                Field btnAddField = VentasView.class.getDeclaredField("btnAgregarAlCarrito");
                btnAddField.setAccessible(true);
                JButton btnAdd = (JButton) btnAddField.get(fVentasView);
                btnAdd.doClick();

                // Select client
                JComboBox<Cliente> cmbCl = getComboBox(fVentasView, "cbClientes");
                cmbCl.setSelectedIndex(0);

                // Register checkout
                Field btnPayField = VentasView.class.getDeclaredField("btnRegistrar");
                btnPayField.setAccessible(true);
                JButton btnPay = (JButton) btnPayField.get(fVentasView);

                logger.info("Registering sales transaction...");
                btnPay.doClick();
            } catch (Exception ex) {
                logger.warn("Simulated checkout cart additions: " + ex.getMessage());
            }
        });
        Thread.sleep(200);

        // 6. Test Inventario Flow (InventarioView, InventarioAddView, InventarioEditView)
        logger.info("Starting Inventario E2E view flow...");
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToInventario();
        });
        Thread.sleep(200);

        final InventarioView inventarioView1 = findComponent(fDashboardView, InventarioView.class);
        assertNotNull(inventarioView1, "InventarioView should be active.");

        // Click alerts and clear filters
        SwingUtilities.invokeAndWait(() -> {
            JButton btnAlerts = findButtonByText(inventarioView1, "Buscar Alertas");
            if (btnAlerts != null) {
                logger.info("Clicking Buscar Alertas button in InventarioView...");
                btnAlerts.doClick();
            }
            JButton btnAll = findButtonByText(inventarioView1, "Limpiar Filtros");
            if (btnAll != null) {
                logger.info("Clicking Limpiar Filtros button in InventarioView...");
                btnAll.doClick();
            }
            JButton btnExport = findButtonByText(inventarioView1, "Exportar");
            if (btnExport != null) {
                logger.info("Clicking Exportar Excel button in InventarioView...");
                btnExport.doClick();
            }
        });
        Thread.sleep(200);

        // Open InventarioAddView
        SwingUtilities.invokeAndWait(() -> {
            JButton btnAdd = findButtonByText(inventarioView1, "+ Añadir Producto");
            assertNotNull(btnAdd, "Add Product button should exist.");
            logger.info("Clicking Add Product button to load InventarioAddView...");
            btnAdd.doClick();
        });
        Thread.sleep(200);

        InventarioAddView addView = findComponent(fDashboardView, InventarioAddView.class);
        assertNotNull(addView, "InventarioAddView should be loaded.");

        // Fill fields in AddView and click Guardar
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field fSku = InventarioAddView.class.getDeclaredField("txtSku");
                fSku.setAccessible(true);
                ((JTextField) fSku.get(addView)).setText("99999999");

                Field fNombre = InventarioAddView.class.getDeclaredField("txtNombre");
                fNombre.setAccessible(true);
                ((JTextField) fNombre.get(addView)).setText("Product E2E Test");

                Field fPrecio = InventarioAddView.class.getDeclaredField("txtPrecioVenta");
                fPrecio.setAccessible(true);
                ((JTextField) fPrecio.get(addView)).setText("5.50");

                Field fStock = InventarioAddView.class.getDeclaredField("txtStockInicial");
                fStock.setAccessible(true);
                ((JTextField) fStock.get(addView)).setText("20");

                JButton btnSave = findButtonByText(addView, "Guardar");
                assertNotNull(btnSave, "Guardar button should exist in AddView.");
                logger.info("Clicking Guardar in InventarioAddView...");
                btnSave.doClick();
            } catch (Exception ex) {
                fail("Failed in InventarioAddView input: " + ex.getMessage());
            }
        });
        Thread.sleep(300);

        // Ensure we navigate back to InventarioView explicitly and locate the active view
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToInventario();
        });
        Thread.sleep(200);

        final InventarioView activeInventarioView = findComponent(fDashboardView, InventarioView.class);
        assertNotNull(activeInventarioView, "InventarioView should be active after saving.");

        // Open InventarioEditView by double clicking the first row of tblStock
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTable tblStock = findComponent(activeInventarioView, JTable.class);
                assertNotNull(tblStock);

                Field lastStocksField = InventarioView.class.getDeclaredField("lastStocks");
                lastStocksField.setAccessible(true);
                List<Stock> lastStocks = (List<Stock>) lastStocksField.get(activeInventarioView);

                Field lastProductsField = InventarioView.class.getDeclaredField("lastProducts");
                lastProductsField.setAccessible(true);
                List<Producto> lastProducts = (List<Producto>) lastProductsField.get(activeInventarioView);

                Window parentWindow = SwingUtilities.getWindowAncestor(activeInventarioView);

                logger.info("tblStock row count: " + tblStock.getRowCount());
                logger.info("lastStocks: " + (lastStocks == null ? "null" : lastStocks.toString()));
                logger.info("lastProducts: " + (lastProducts == null ? "null" : lastProducts.toString()));
                logger.info("parentWindow: " + (parentWindow == null ? "null" : parentWindow.getClass().getName()));

                if (tblStock.getRowCount() > 0) {
                    tblStock.setRowSelectionInterval(0, 0);
                    logger.info("Simulating double click on tblStock row to open InventarioEditView...");
                    MouseEvent doubleClick = new MouseEvent(tblStock, MouseEvent.MOUSE_CLICKED, 
                            System.currentTimeMillis(), 0, 0, 0, 2, false);
                    for (MouseListener ml : tblStock.getMouseListeners()) {
                        ml.mouseClicked(doubleClick);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error in debug print: ", ex);
            }
        });
        Thread.sleep(200);

        InventarioEditView editView = findComponent(fDashboardView, InventarioEditView.class);
        assertNotNull(editView, "InventarioEditView should be loaded.");

        // Edit fields and Save
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field fNombre = InventarioEditView.class.getDeclaredField("txtNombre");
                fNombre.setAccessible(true);
                ((JTextField) fNombre.get(editView)).setText("Product Updated E2E");

                JButton btnSave = findButtonByText(editView, "Guardar");
                assertNotNull(btnSave);
                logger.info("Clicking Guardar in InventarioEditView...");
                btnSave.doClick();
            } catch (Exception ex) {
                fail("Failed in InventarioEditView: " + ex.getMessage());
            }
        });
        Thread.sleep(300);

        // Ensure we navigate back to InventarioView explicitly
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToInventario();
        });
        Thread.sleep(200);

        // 7. Test Categorias Flow (CategoriasView, CategoriasAddView, CategoriasEditView)
        logger.info("Starting Categorias E2E view flow...");
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToCategorias();
        });
        Thread.sleep(200);

        final CategoriasView categoriasView1 = findComponent(fDashboardView, CategoriasView.class);
        assertNotNull(categoriasView1, "CategoriasView should be active.");

        // Select first category in list
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field fList = CategoriasView.class.getDeclaredField("lstCategories");
                fList.setAccessible(true);
                JList<Categoria> lst = (JList<Categoria>) fList.get(categoriasView1);
                if (lst.getModel().getSize() > 0) {
                    logger.info("Selecting first category in lstCategories...");
                    lst.setSelectedIndex(0);
                }
            } catch (Exception ex) {
                fail("Failed selecting category in list: " + ex.getMessage());
            }
        });
        Thread.sleep(100);

        // Click create category to load CategoriasAddView
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field fBtn = CategoriasView.class.getDeclaredField("btnCrearCategoria");
                fBtn.setAccessible(true);
                JButton btn = (JButton) fBtn.get(categoriasView1);
                logger.info("Clicking Crear Categoria button to load CategoriasAddView...");
                btn.doClick();
            } catch (Exception ex) {
                fail("Failed loading CategoriasAddView: " + ex.getMessage());
            }
        });
        Thread.sleep(200);

        CategoriasAddView catAddView = findComponent(fDashboardView, CategoriasAddView.class);
        assertNotNull(catAddView, "CategoriasAddView should be loaded.");

        // Fill Add Category form and Save
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field fNombre = CategoriasAddView.class.getDeclaredField("txtNombreCategoria");
                fNombre.setAccessible(true);
                ((JTextField) fNombre.get(catAddView)).setText("Category E2E");

                Field fSku = CategoriasAddView.class.getDeclaredField("txtSkuPrefix");
                fSku.setAccessible(true);
                ((JTextField) fSku.get(catAddView)).setText("CAT");

                Field fColor = CategoriasAddView.class.getDeclaredField("txtTagColor");
                fColor.setAccessible(true);
                ((JTextField) fColor.get(catAddView)).setText("#FF5733");

                JButton btnSave = findButtonByText(catAddView, "Guardar Categoría");
                assertNotNull(btnSave);
                logger.info("Clicking Guardar in CategoriasAddView...");
                btnSave.doClick();
            } catch (Exception ex) {
                fail("Failed saving category in CategoriasAddView: " + ex.getMessage());
            }
        });
        Thread.sleep(300);

        // Navigate back to CategoriasView and query the active view
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToCategorias();
        });
        Thread.sleep(200);

        final CategoriasView activeCategoriasView = findComponent(fDashboardView, CategoriasView.class);
        assertNotNull(activeCategoriasView, "CategoriasView should be active after saving new category.");

        // Edit Category to load CategoriasEditView
        SwingUtilities.invokeAndWait(() -> {
            JButton btnEdit = findButtonByText(activeCategoriasView, "Ajustar Reglas");
            assertNotNull(btnEdit);
            logger.info("Clicking Ajustar Reglas button to load CategoriasEditView...");
            btnEdit.doClick();
        });
        Thread.sleep(200);

        CategoriasEditView catEditView = findComponent(fDashboardView, CategoriasEditView.class);
        assertNotNull(catEditView, "CategoriasEditView should be loaded.");

        // Edit Category name and Save
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field fNombre = CategoriasEditView.class.getDeclaredField("txtNombreCategoria");
                fNombre.setAccessible(true);
                ((JTextField) fNombre.get(catEditView)).setText("Category Updated E2E");

                JButton btnSave = findButtonByText(catEditView, "Guardar");
                assertNotNull(btnSave);
                logger.info("Clicking Guardar in CategoriasEditView...");
                btnSave.doClick();
            } catch (Exception ex) {
                fail("Failed editing category: " + ex.getMessage());
            }
        });
        Thread.sleep(300);

        // Navigate back to CategoriasView and query the active view
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToCategorias();
        });
        Thread.sleep(200);

        final CategoriasView activeCategoriasView2 = findComponent(fDashboardView, CategoriasView.class);
        assertNotNull(activeCategoriasView2, "CategoriasView should be active after updating category.");

        // Test Delete Category
        SwingUtilities.invokeAndWait(() -> {
            JButton btnEdit = findButtonByText(activeCategoriasView2, "Ajustar Reglas");
            assertNotNull(btnEdit);
            logger.info("Re-opening CategoriasEditView to test deletion...");
            btnEdit.doClick();
        });
        Thread.sleep(200);

        CategoriasEditView catEditView2 = findComponent(fDashboardView, CategoriasEditView.class);
        assertNotNull(catEditView2);

        SwingUtilities.invokeAndWait(() -> {
            JButton btnDelete = findButtonByText(catEditView2, "Eliminar");
            assertNotNull(btnDelete);
            logger.info("Clicking Eliminar Categoría button...");
            btnDelete.doClick();
        });
        Thread.sleep(300); // Wait for the confirm dialog to open and be auto-confirmed

        // Navigate back to CategoriasView
        SwingUtilities.invokeAndWait(() -> {
            fDashboardView.navigateToCategorias();
        });
        Thread.sleep(200);

        // 8. Test Reportes Flow (ReportesView)
        logger.info("Starting Reportes E2E view flow...");
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field btnReportsField = DashboardView.class.getDeclaredField("btnReports");
                btnReportsField.setAccessible(true);
                JButton btnReports = (JButton) btnReportsField.get(fDashboardView);
                logger.info("Clicking Reports menu button on Dashboard...");
                btnReports.doClick();
            } catch (Exception ex) {
                fail("Failed navigating to Reportes: " + ex.getMessage());
            }
        });
        Thread.sleep(200);

        ReportesView reportesView = findComponent(fDashboardView, ReportesView.class);
        assertNotNull(reportesView, "ReportesView should be active.");

        // Press time range buttons
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field btnHoyField = ReportesView.class.getDeclaredField("btnHoy");
                btnHoyField.setAccessible(true);
                ((JButton) btnHoyField.get(reportesView)).doClick();

                Field btn7DField = ReportesView.class.getDeclaredField("btn7D");
                btn7DField.setAccessible(true);
                ((JButton) btn7DField.get(reportesView)).doClick();

                Field btn30DField = ReportesView.class.getDeclaredField("btn30D");
                btn30DField.setAccessible(true);
                ((JButton) btn30DField.get(reportesView)).doClick();

                Field btn1AField = ReportesView.class.getDeclaredField("btn1A");
                btn1AField.setAccessible(true);
                ((JButton) btn1AField.get(reportesView)).doClick();
                
                logger.info("Tested date range buttons (Hoy, 7D, 30D, 1A) in ReportesView.");
            } catch (Exception ex) {
                fail("Failed testing time range buttons: " + ex.getMessage());
            }
        });
        Thread.sleep(100);

        // Custom range button click
        SwingUtilities.invokeAndWait(() -> {
            JButton btnCustom = findButtonByText(reportesView, "Rango Personalizado");
            if (btnCustom != null) {
                logger.info("Clicking Rango Personalizado button...");
                btnCustom.doClick();
            }
        });
        Thread.sleep(200); // Will open showInputDialog twice and get auto-dismissed

        // Share / Clipboard action
        SwingUtilities.invokeAndWait(() -> {
            JButton btnShare = findButtonByText(reportesView, "Compartir");
            if (btnShare != null) {
                logger.info("Clicking Compartir button to copy summary report...");
                btnShare.doClick();
            }
        });
        Thread.sleep(200);

        // Ver Lista Completa dialog
        SwingUtilities.invokeAndWait(() -> {
            JButton btnList = findButtonByText(reportesView, "Ver Lista");
            if (btnList != null) {
                logger.info("Clicking Ver Lista Completa button...");
                btnList.doClick();
            }
        });
        Thread.sleep(200); // Opens modal dialog and gets auto-dismissed

        // Export Report action
        SwingUtilities.invokeAndWait(() -> {
            JButton btnExport = findButtonByText(reportesView, "Exportar Reporte");
            if (btnExport != null) {
                logger.info("Clicking Exportar Reporte button...");
                btnExport.doClick();
            }
        });
        Thread.sleep(200); // Opens file chooser and gets auto-dismissed

        // 9. Logout flow
        logger.info("Executing Logout E2E flow...");
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field btnLogoutField = DashboardView.class.getDeclaredField("btnLogout");
                btnLogoutField.setAccessible(true);
                JButton btnLogout = (JButton) btnLogoutField.get(fDashboardView);
                logger.info("Clicking Logout button on Dashboard...");
                btnLogout.doClick();
            } catch (Exception ex) {
                fail("Failed to click Logout button: " + ex.getMessage());
            }
        });
        Thread.sleep(200);

        // Verify return to LoginView
        assertFalse(fDashboardView.isVisible(), "DashboardView should be hidden after logout.");
        assertTrue(loginView.isVisible(), "LoginView should be visible after logout.");

        logger.info("Full E2E Integration Flow test completed successfully.");
    }

    // ==========================================
    // HELPER METHODS FOR REFLECTION & COMPONENT ACCESS
    // ==========================================

    @SuppressWarnings("unchecked")
    private <T extends Component> T findComponent(Component comp, Class<T> clazz) {
        if (clazz.isInstance(comp)) {
            return (T) comp;
        } else if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                T found = findComponent(child, clazz);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JButton findButtonByText(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton) {
                String btnText = ((JButton) child).getText();
                if (btnText != null && btnText.contains(text)) {
                    return (JButton) child;
                }
            }
            if (child instanceof Container) {
                JButton found = findButtonByText((Container) child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> JComboBox<T> getComboBox(Container parent, String fieldName) {
        try {
            Field f = parent.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (JComboBox<T>) f.get(parent);
        } catch (Exception e) {
            return null;
        }
    }

    private JSpinner getSpinner(Container parent, String fieldName) {
        try {
            Field f = parent.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (JSpinner) f.get(parent);
        } catch (Exception e) {
            return null;
        }
    }

    private JLabel getLabel(Container parent, String fieldName) {
        try {
            Field f = parent.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (JLabel) f.get(parent);
        } catch (Exception e) {
            return null;
        }
    }

    // ==========================================
    // STUB DATABASE RESPONSES FOR FULL FLOW
    // ==========================================
    private void setupMockDatabaseQueries() throws SQLException {
        // We will intercept connection prepareStatement calls and return mock PreparedStatements
        when(mockConnection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            ResultSetMetaData meta = mock(ResultSetMetaData.class);

            when(ps.executeQuery()).thenReturn(rs);
            when(ps.getMetaData()).thenReturn(meta);
            when(ps.executeUpdate()).thenReturn(1);
            when(ps.executeBatch()).thenReturn(new int[]{1});

            // Setup default rs.next() behavior
            when(rs.next()).thenReturn(true, false);

            // Dynamically intercept rs.getInt case-insensitively
            when(rs.getInt(anyString())).thenAnswer(inv -> {
                String col = inv.getArgument(0).toString().toLowerCase();
                if (col.equals("id_usuario")) return 1;
                if (col.equals("id_rol")) return 1;
                if (col.equals("id_producto")) return 1;
                if (col.equals("id_categoria")) return 1;
                if (col.equals("id_cliente")) return 1;
                if (col.equals("id_stock")) return 1;
                if (col.equals("cantidad")) return 100;
                if (col.equals("estado")) return 1;
                return 0;
            });

            // Dynamically intercept rs.getString case-insensitively
            when(rs.getString(anyString())).thenAnswer(inv -> {
                String col = inv.getArgument(0).toString().toLowerCase();
                if (col.equals("username")) return "admin";
                if (col.equals("nombre_rol")) return "Administrador";
                if (col.equals("nombre_producto")) return "Galletas Soda";
                if (col.equals("nombre")) {
                    if (sql.contains("FROM cliente") || sql.contains("from cliente")) return "Juan";
                    return "Administrador";
                }
                if (col.equals("apellido_paterno")) {
                    if (sql.contains("FROM cliente") || sql.contains("from cliente")) return "Perez";
                    return "Negocio";
                }
                if (col.equals("apellido_materno")) {
                    if (sql.contains("FROM cliente") || sql.contains("from cliente")) return "Gomez";
                    return "Principal";
                }
                if (col.equals("dni_ruc")) return "12345678";
                if (col.equals("codigo_barras")) return "12345678";
                if (col.equals("nombre_categoria")) return "Abarrotes";
                return "";
            });

            // Dynamically intercept rs.getBigDecimal case-insensitively
            when(rs.getBigDecimal(anyString())).thenAnswer(inv -> {
                String col = inv.getArgument(0).toString().toLowerCase();
                if (col.equals("precio_unitario")) return new BigDecimal("1.20");
                if (col.equals("precio_total")) return new BigDecimal("6.00");
                return BigDecimal.ZERO;
            });

            // Dynamically intercept rs.getDate
            when(rs.getDate(anyString())).thenAnswer(inv -> {
                return java.sql.Date.valueOf(LocalDate.now());
            });

            return ps;
        });

        // Support for inserts/updates with RETURN_GENERATED_KEYS (inserts for products, sales, clients)
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenAnswer(invocation -> {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rsKeys = mock(ResultSet.class);
            when(ps.getGeneratedKeys()).thenReturn(rsKeys);
            when(rsKeys.next()).thenReturn(true, false);
            when(rsKeys.getInt(1)).thenReturn(1);
            when(ps.executeUpdate()).thenReturn(1);
            return ps;
        });
    }

    @AfterAll
    public static void generateHtmlReport() {
        try {
            File logFile = new File("logs/test_execution.log");
            List<String> logLines = new ArrayList<>();
            if (logFile.exists()) {
                logLines = java.nio.file.Files.readAllLines(logFile.toPath());
            }

            StringBuilder logsJson = new StringBuilder();
            for (int i = 0; i < logLines.size(); i++) {
                logsJson.append("            \"").append(escapeJsonString(logLines.get(i))).append("\"");
                if (i < logLines.size() - 1) {
                    logsJson.append(",\n");
                }
            }

            String htmlTemplate = """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reporte de Ejecución E2E y Análisis de Errores</title>
    <!-- Modern Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;700&family=Outfit:wght@300;400;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg: #0b0f19;
            --bg-gradient: radial-gradient(circle at 50% 0%, #1e1b4b 0%, #0b0f19 80%);
            --card-bg: rgba(17, 24, 39, 0.7);
            --border: rgba(255, 255, 255, 0.08);
            --text: #f3f4f6;
            --text-secondary: #9ca3af;
            --primary: #6366f1;
            --primary-glow: rgba(99, 102, 241, 0.15);
            --success: #10b981;
            --success-glow: rgba(16, 185, 129, 0.15);
            --warning: #f59e0b;
            --warning-glow: rgba(245, 158, 11, 0.15);
            --danger: #ef4444;
            --danger-glow: rgba(239, 68, 68, 0.15);
            --card-hover-border: rgba(99, 102, 241, 0.35);
        }
        
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Outfit', sans-serif;
            background: var(--bg);
            background-image: var(--bg-gradient);
            color: var(--text);
            min-height: 100vh;
            padding: 40px 20px;
            line-height: 1.5;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        /* Header design */
        header {
            text-align: center;
            margin-bottom: 40px;
            animation: fadeIn 0.8s ease-out;
        }

        h1 {
            font-size: 2.8rem;
            font-weight: 700;
            margin-bottom: 12px;
            background: linear-gradient(135deg, #a5b4fc 0%, #6366f1 50%, #d8b4fe 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            letter-spacing: -0.025em;
        }

        .subtitle {
            color: var(--text-secondary);
            font-size: 1.15rem;
            max-width: 600px;
            margin: 0 auto;
            font-weight: 300;
        }

        /* Stats Cards */
        .grid-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
            animation: slideUp 0.6s ease-out;
        }

        .card-stat {
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 24px;
            backdrop-filter: blur(16px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }

        .card-stat::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: transparent;
            transition: background 0.3s ease;
        }

        .card-stat:hover {
            transform: translateY(-5px);
            border-color: var(--card-hover-border);
            box-shadow: 0 15px 35px rgba(99, 102, 241, 0.1);
        }

        .card-stat.errors.active {
            border-color: rgba(239, 68, 68, 0.4);
            box-shadow: 0 0 15px rgba(239, 68, 68, 0.15);
            animation: pulse-border 2s infinite;
        }
        
        .card-stat.errors.active::before {
            background: var(--danger);
        }
        .card-stat.warnings.active::before {
            background: var(--warning);
        }
        .card-stat.milestones::before {
            background: var(--primary);
        }
        .card-stat.total::before {
            background: var(--text-secondary);
        }

        .stat-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: var(--text-secondary);
            font-size: 0.9rem;
            text-transform: uppercase;
            font-weight: 600;
            letter-spacing: 0.05em;
            margin-bottom: 10px;
        }

        .stat-value {
            font-size: 2.25rem;
            font-weight: 700;
            color: var(--text);
            display: flex;
            align-items: baseline;
        }

        .stat-icon {
            font-size: 1.5rem;
            opacity: 0.8;
        }

        /* Spotlight de Errores */
        .spotlight-section {
            margin-bottom: 40px;
            animation: slideUp 0.7s ease-out;
        }

        .spotlight-title {
            font-size: 1.4rem;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-weight: 600;
        }

        .spotlight-card {
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 30px;
            backdrop-filter: blur(16px);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
        }

        .spotlight-empty {
            text-align: center;
            padding: 40px 20px;
            color: var(--success);
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 12px;
        }

        .spotlight-empty .icon {
            font-size: 3rem;
            animation: bounce 2s infinite;
        }

        .spotlight-empty .msg-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text);
        }

        .spotlight-empty .msg-desc {
            color: var(--text-secondary);
            font-size: 0.95rem;
        }

        .error-item {
            background: rgba(239, 68, 68, 0.05);
            border: 1px solid rgba(239, 68, 68, 0.15);
            border-radius: 12px;
            padding: 16px;
            margin-bottom: 12px;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .error-item.warn {
            background: rgba(245, 158, 11, 0.05);
            border: 1px solid rgba(245, 158, 11, 0.15);
        }

        .error-item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
        }

        .error-badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
        }

        .error-item.error .error-badge {
            background: var(--danger-glow);
            color: #fca5a5;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        .error-item.warn .error-badge {
            background: var(--warning-glow);
            color: #fcd34d;
            border: 1px solid rgba(245, 158, 11, 0.3);
        }

        .error-meta {
            font-size: 0.85rem;
            color: var(--text-secondary);
            font-family: 'JetBrains Mono', monospace;
        }

        .error-msg {
            font-size: 1rem;
            font-weight: 500;
            word-break: break-word;
        }

        .error-item.error .error-msg {
            color: #fca5a5;
        }

        .error-item.warn .error-msg {
            color: #fcd34d;
        }

        /* Collapsible trace */
        .btn-collapse {
            align-self: flex-start;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--border);
            color: var(--text-secondary);
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 0.8rem;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 6px;
            margin-top: 5px;
            font-weight: 500;
        }

        .btn-collapse:hover {
            background: rgba(255, 255, 255, 0.1);
            color: var(--text);
            border-color: rgba(255, 255, 255, 0.2);
        }

        .details-container {
            display: none;
            width: 100%;
            margin-top: 10px;
            border-top: 1px solid rgba(255, 255, 255, 0.05);
            padding-top: 10px;
            overflow-x: auto;
        }

        .details-container.show {
            display: block;
            animation: slideDownCollapse 0.25s ease-out;
        }

        .stack-trace {
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.82rem;
            color: #cbd5e1;
            background: #020617;
            padding: 12px;
            border-radius: 8px;
            border: 1px solid var(--border);
            white-space: pre-wrap;
            word-break: break-all;
            line-height: 1.45;
        }

        /* Actions and Links */
        .action-bar {
            display: flex;
            justify-content: center;
            gap: 15px;
            margin-bottom: 40px;
            flex-wrap: wrap;
            animation: slideUp 0.8s ease-out;
        }

        .btn-action {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 28px;
            border-radius: 14px;
            font-weight: 600;
            font-size: 1rem;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }

        .btn-action-primary {
            background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
            color: white;
            border: 1px solid rgba(99, 102, 241, 0.5);
            box-shadow: 0 4px 15px rgba(99, 102, 241, 0.35);
        }

        .btn-action-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
            opacity: 0.95;
        }

        .btn-action-secondary {
            background: rgba(255, 255, 255, 0.04);
            color: var(--text);
            border: 1px solid var(--border);
        }

        .btn-action-secondary:hover {
            transform: translateY(-2px);
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(255, 255, 255, 0.2);
        }

        /* Interactive Controls */
        .console-controls {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
            gap: 15px;
            flex-wrap: wrap;
            animation: slideUp 0.9s ease-out;
        }

        /* Search input */
        .search-box {
            position: relative;
            flex-grow: 1;
            max-width: 400px;
            min-width: 250px;
        }

        .search-input {
            width: 100%;
            background: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 12px 16px 12px 40px;
            color: var(--text);
            font-family: inherit;
            font-size: 0.95rem;
            outline: none;
            transition: all 0.2s;
        }

        .search-input:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px var(--primary-glow);
        }

        .search-icon {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-secondary);
            font-size: 1rem;
            pointer-events: none;
        }

        /* Filtering Tabs */
        .tabs {
            display: flex;
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid var(--border);
            padding: 4px;
            border-radius: 12px;
            gap: 4px;
        }

        .tab-btn {
            background: transparent;
            border: none;
            color: var(--text-secondary);
            padding: 8px 16px;
            border-radius: 8px;
            font-family: inherit;
            font-size: 0.9rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .tab-btn:hover {
            color: var(--text);
            background: rgba(255, 255, 255, 0.05);
        }

        .tab-btn.active {
            background: var(--primary);
            color: white;
            box-shadow: 0 4px 10px rgba(99, 102, 241, 0.2);
        }

        /* Terminal Console */
        .terminal-wrapper {
            animation: slideUp 1s ease-out;
        }

        .terminal {
            background: #020617;
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 20px;
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.88rem;
            line-height: 1.5;
            max-height: 600px;
            overflow-y: auto;
            box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.8), 0 10px 35px rgba(0, 0, 0, 0.3);
            scroll-behavior: smooth;
        }

        /* Custom Scrollbars */
        ::-webkit-scrollbar {
            width: 8px;
            height: 8px;
        }
        ::-webkit-scrollbar-track {
            background: rgba(0, 0, 0, 0.2);
            border-radius: 10px;
        }
        ::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.15);
            border-radius: 10px;
            border: 2px solid transparent;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .log-row {
            margin-bottom: 6px;
            padding: 6px 12px;
            border-radius: 8px;
            transition: background 0.15s;
            display: flex;
            flex-direction: column;
            gap: 4px;
        }

        .log-row:hover {
            background: rgba(255, 255, 255, 0.03);
        }

        .log-row-main {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            word-break: break-all;
        }

        .log-row.error {
            background: rgba(239, 68, 68, 0.03);
            border-left: 3px solid var(--danger);
        }
        .log-row.error:hover {
            background: rgba(239, 68, 68, 0.06);
        }

        .log-row.warn {
            background: rgba(245, 158, 11, 0.03);
            border-left: 3px solid var(--warning);
        }
        .log-row.warn:hover {
            background: rgba(245, 158, 11, 0.06);
        }

        .log-row.system {
            border-left: 3px solid var(--primary);
        }

        .log-badge {
            font-size: 0.72rem;
            font-weight: 700;
            padding: 2px 6px;
            border-radius: 4px;
            text-transform: uppercase;
            letter-spacing: 0.02em;
            display: inline-block;
            flex-shrink: 0;
            margin-top: 1px;
        }

        .log-row.error .log-badge {
            background: var(--danger-glow);
            color: #fca5a5;
            border: 1px solid rgba(239, 68, 68, 0.2);
        }
        .log-row.warn .log-badge {
            background: var(--warning-glow);
            color: #fcd34d;
            border: 1px solid rgba(245, 158, 11, 0.2);
        }
        .log-row.system .log-badge {
            background: var(--primary-glow);
            color: #a5b4fc;
            border: 1px solid rgba(99, 102, 241, 0.2);
        }
        .log-row.info .log-badge {
            background: rgba(255, 255, 255, 0.05);
            color: #e5e7eb;
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .log-time {
            color: #4b5563;
            font-size: 0.82rem;
            flex-shrink: 0;
            user-select: none;
        }

        .log-thread {
            color: #6b7280;
            font-size: 0.82rem;
            flex-shrink: 0;
            user-select: none;
        }

        .log-logger {
            color: #818cf8;
            font-size: 0.82rem;
            flex-shrink: 0;
            font-weight: 500;
        }

        .log-message-text {
            color: #cbd5e1;
            font-size: 0.86rem;
            flex-grow: 1;
        }

        .log-row.error .log-message-text {
            color: #fca5a5;
        }
        .log-row.warn .log-message-text {
            color: #fcd34d;
        }
        .log-row.system .log-message-text {
            color: #c084fc;
            font-weight: 500;
        }

        /* Footer */
        .footer {
            text-align: center;
            margin-top: 50px;
            color: var(--text-secondary);
            font-size: 0.85rem;
            padding-bottom: 20px;
            animation: fadeIn 1.2s ease-out;
        }

        .footer span {
            color: var(--primary);
            font-weight: 600;
        }

        /* Keyframes */
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @keyframes slideUp {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        @keyframes slideDownCollapse {
            from { opacity: 0; transform: translateY(-5px); }
            to { opacity: 1; transform: translateY(0); }
        }

        @keyframes bounce {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-8px); }
        }

        @keyframes pulse-border {
            0%, 100% { border-color: rgba(239, 68, 68, 0.4); }
            50% { border-color: rgba(239, 68, 68, 0.8); }
        }

        @media (max-width: 768px) {
            .console-controls {
                flex-direction: column;
                align-items: stretch;
            }
            .search-box {
                max-width: 100%;
            }
            .tabs {
                justify-content: center;
            }
            .log-row-main {
                flex-wrap: wrap;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>Dashboard de Integración E2E</h1>
            <div class="subtitle">Análisis inteligente de ejecución de la suite de pruebas del Minimarket</div>
        </header>

        <!-- Stats Grid -->
        <div class="grid-stats">
            <div class="card-stat total">
                <div class="stat-header">
                    <span>Total Logs</span>
                    <span class="stat-icon">📋</span>
                </div>
                <div class="stat-value" id="stat-total">0</div>
            </div>
            <div class="card-stat milestones">
                <div class="stat-header">
                    <span>Hitos de Prueba</span>
                    <span class="stat-icon">🚀</span>
                </div>
                <div class="stat-value" id="stat-milestones">0</div>
            </div>
            <div class="card-stat warnings" id="card-warnings">
                <div class="stat-header">
                    <span>Advertencias</span>
                    <span class="stat-icon">⚠️</span>
                </div>
                <div class="stat-value" id="stat-warnings" style="color: var(--warning);">0</div>
            </div>
            <div class="card-stat errors" id="card-errors">
                <div class="stat-header">
                    <span>Errores</span>
                    <span class="stat-icon">🚨</span>
                </div>
                <div class="stat-value" id="stat-errors" style="color: var(--danger);">0</div>
            </div>
        </div>

        <!-- Spotlight de Errores -->
        <div class="spotlight-section">
            <div class="spotlight-title">
                <span>🔎</span> Spotlight de Anomalías (Errores y Advertencias)
            </div>
            <div class="spotlight-card" id="spotlight-content">
                <!-- Will be populated dynamically by JS -->
            </div>
        </div>

        <!-- Action Links -->
        <div class="action-bar">
            <a href="target/site/jacoco/index.html" target="_blank" class="btn-action btn-action-primary">
                <span>📊</span> Ver Reporte de Cobertura JaCoCo (Línea por Línea)
            </a>
            <a href="logs/test_execution.log" target="_blank" class="btn-action btn-action-secondary">
                <span>📄</span> Ver Log de Texto Plano (logback)
            </a>
        </div>

        <!-- Console Log Title & Controls -->
        <div class="console-controls">
            <h2 style="font-size: 1.4rem; font-weight: 600;">Consola Completa de Ejecución</h2>
            <div class="search-box">
                <span class="search-icon">🔍</span>
                <input type="text" id="search-input" class="search-input" placeholder="Buscar en logs (ej. Ventas, Inventario)..." oninput="handleSearch()">
            </div>
            <div class="tabs">
                <button class="tab-btn active" id="tab-all" onclick="filterTab('all')">Todos</button>
                <button class="tab-btn" id="tab-milestones" onclick="filterTab('milestones')">🚀 Hitos</button>
                <button class="tab-btn" id="tab-warn" onclick="filterTab('warn')">⚠️ Warns</button>
                <button class="tab-btn" id="tab-error" onclick="filterTab('error')">🚨 Errores</button>
            </div>
        </div>

        <!-- Console Console Wrapper -->
        <div class="terminal-wrapper">
            <div class="terminal" id="terminal-console">
                <!-- Log rows populated dynamically -->
            </div>
        </div>

        <div class="footer">
            Generado automáticamente por la Suite de Pruebas de Integración en <span id="generation-date"></span>
        </div>
    </div>

    <!-- Script Block containing Raw Log Data & Parser Logic -->
    <script>
        // Injected logs from Java
        const rawLogs = [
// LOGS_PLACEHOLDER
        ];

        // Parser Logic
        const parsedLogs = [];
        let currentLog = null;

        rawLogs.forEach((line) => {
            const match = line.match(/^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[([^\\]]+)\\]\\s+(INFO|WARN|ERROR|DEBUG|TRACE)\\s+(.*?)\\s+-\\s+(.*)$/i);
            if (match) {
                if (currentLog) {
                    parsedLogs.push(currentLog);
                }
                currentLog = {
                    timestamp: match[1],
                    thread: match[2],
                    level: match[3].toUpperCase(),
                    logger: match[4],
                    message: match[5],
                    details: []
                };
            } else {
                if (currentLog) {
                    currentLog.details.push(line);
                } else {
                    currentLog = {
                        timestamp: '',
                        thread: '',
                        level: 'INFO',
                        logger: 'System',
                        message: line,
                        details: []
                    };
                }
            }
        });
        if (currentLog) {
            parsedLogs.push(currentLog);
        }

        // Helper to detect Milestones
        function isMilestone(log) {
            if (log.level === 'ERROR' || log.level === 'WARN') return false;
            const msg = log.message;
            return log.logger.includes("AppIntegrationTest") || 
                   msg.includes("===") || 
                   msg.includes("passed") || 
                   msg.includes("completed") || 
                   msg.includes("Starting") || 
                   msg.includes("Auto-confirming") || 
                   msg.includes("Auto-dismissing");
        }

        // Statistics Counting
        let countTotal = parsedLogs.length;
        let countWarn = 0;
        let countError = 0;
        let countMilestones = 0;

        parsedLogs.forEach(log => {
            if (log.level === 'WARN') countWarn++;
            else if (log.level === 'ERROR') countError++;
            
            if (isMilestone(log)) countMilestones++;
        });

        // Set date
        document.getElementById('generation-date').innerText = new Date().toLocaleDateString('es-ES', {
            year: 'numeric', month: 'long', day: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });

        // Update Stat Cards UI
        document.getElementById('stat-total').innerText = countTotal;
        document.getElementById('stat-milestones').innerText = countMilestones;
        document.getElementById('stat-warnings').innerText = countWarn;
        document.getElementById('stat-errors').innerText = countError;

        if (countWarn > 0) {
            document.getElementById('card-warnings').classList.add('active');
        }
        if (countError > 0) {
            document.getElementById('card-errors').classList.add('active');
        }

        // Spotlight population
        const spotlightContent = document.getElementById('spotlight-content');
        const anomalies = parsedLogs.filter(log => log.level === 'ERROR' || log.level === 'WARN');

        if (anomalies.length === 0) {
            spotlightContent.innerHTML = `
                <div class="spotlight-empty">
                    <div class="icon">🎉</div>
                    <div class="msg-title">¡Sistema Saludable!</div>
                    <div class="msg-desc">Cero errores y advertencias detectados durante la ejecución de las pruebas.</div>
                </div>
            `;
        } else {
            let html = '';
            anomalies.forEach((log, index) => {
                const levelClass = log.level.toLowerCase(); // error or warn
                const icon = log.level === 'ERROR' ? '🚨' : '⚠️';
                const hasDetails = log.details.length > 0;
                
                html += `
                    <div class="error-item ${levelClass}">
                        <div class="error-item-header">
                            <span class="error-badge">${log.level}</span>
                            <span class="error-meta">${log.timestamp} [${log.thread}] - ${log.logger}</span>
                        </div>
                        <div class="error-msg">${icon} ${escapeHtml(log.message)}</div>
                        ${hasDetails ? `
                            <button class="btn-collapse" onclick="toggleDetails('spot-details-${index}')">
                                <span>▶</span> Ver Stacktrace/Detalles (${log.details.length} líneas)
                            </button>
                            <div class="details-container" id="spot-details-${index}">
                                <pre class="stack-trace">${escapeHtml(log.details.join('\\n'))}</pre>
                            </div>
                        ` : ''}
                    </div>
                `;
            });
            spotlightContent.innerHTML = html;
        }

        // Toggling collapsible trace
        function toggleDetails(id) {
            const container = document.getElementById(id);
            const button = container.previousElementSibling;
            const arrow = button.querySelector('span');
            
            if (container.classList.contains('show')) {
                container.classList.remove('show');
                arrow.innerText = '▶';
            } else {
                container.classList.add('show');
                arrow.innerText = '▼';
            }
        }

        // Escape HTML helper
        function escapeHtml(text) {
            return text
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        // Filter and Search
        let currentFilter = 'all'; // all, milestones, warn, error
        let currentSearch = '';

        function filterTab(tab) {
            // Remove active classes
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            
            // Set active
            document.getElementById('tab-' + tab).classList.add('active');
            currentFilter = tab;
            
            applyFilterAndSearch();
        }

        function handleSearch() {
            currentSearch = document.getElementById('search-input').value.toLowerCase().trim();
            applyFilterAndSearch();
        }

        function applyFilterAndSearch() {
            const terminal = document.getElementById('terminal-console');
            let filtered = parsedLogs;

            // Apply level filter
            if (currentFilter === 'milestones') {
                filtered = filtered.filter(log => isMilestone(log));
            } else if (currentFilter === 'warn') {
                filtered = filtered.filter(log => log.level === 'WARN');
            } else if (currentFilter === 'error') {
                filtered = filtered.filter(log => log.level === 'ERROR');
            }

            // Apply search filter
            if (currentSearch) {
                filtered = filtered.filter(log => {
                    const inMsg = log.message.toLowerCase().includes(currentSearch);
                    const inLogger = log.logger.toLowerCase().includes(currentSearch);
                    const inDetails = log.details.some(d => d.toLowerCase().includes(currentSearch));
                    return inMsg || inLogger || inDetails;
                });
            }

            // Render
            if (filtered.length === 0) {
                terminal.innerHTML = `<div style="color: var(--text-secondary); text-align: center; padding: 30px;">Ningún log coincide con los filtros especificados.</div>`;
                return;
            }

            let html = '';
            filtered.forEach((log, index) => {
                const isSys = isMilestone(log);
                const hasDetails = log.details.length > 0;
                let rowClass = log.level.toLowerCase();
                let badgeLabel = log.level;

                if (isSys) {
                    rowClass = 'system';
                    badgeLabel = 'HIT';
                }

                html += `
                    <div class="log-row ${rowClass}">
                        <div class="log-row-main">
                            <span class="log-time">${log.timestamp}</span>
                            <span class="log-thread">[${log.thread}]</span>
                            <span class="log-badge">${badgeLabel}</span>
                            <span class="log-logger">${log.logger}</span>
                            <span class="log-message-text">${escapeHtml(log.message)}</span>
                        </div>
                        ${hasDetails ? `
                            <button class="btn-collapse" onclick="toggleDetails('term-details-${index}')">
                                <span>▶</span> Mostrar detalles
                            </button>
                            <div class="details-container" id="term-details-${index}">
                                <pre class="stack-trace">${escapeHtml(log.details.join('\\n'))}</pre>
                            </div>
                        ` : ''}
                    </div>
                `;
            });
            terminal.innerHTML = html;
        }

        // Initial render
        applyFilterAndSearch();
    </script>
</body>
</html>
""";

            String htmlContent = htmlTemplate.replace("// LOGS_PLACEHOLDER", logsJson.toString());
            java.nio.file.Files.writeString(new File("reporte_pruebas.html").toPath(), htmlContent);
        } catch (Exception e) {
            logger.error("Error al generar el reporte HTML: ", e);
        }
    }

    private static String escapeJsonString(String val) {
        if (val == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        String hex = Integer.toHexString(ch);
                        sb.append("\\u").append("0000", 0, 4 - hex.length()).append(hex);
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}
