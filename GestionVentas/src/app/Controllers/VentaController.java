package app.Controllers;

import app.BDD.CajaService;
import app.BDD.InventService;
import app.BDD.VentaService;
import app.Models.Usuario;
import app.Models.DetalleVenta;
import app.Models.Producto;
import app.Models.Venta;
import javafx.scene.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Side;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class VentaController {

    @FXML private TextField campoCliente; 
    @FXML private TextField buscarProducto;
    @FXML private Label dniLabel;   
    @FXML private Label nameLabel;  
    @FXML private Label lblTotal;

    @FXML private TableView<DetalleVenta> tablaDetalleVenta;
    @FXML private TableColumn<DetalleVenta, Integer> colIDProducto;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Float> colSubtotal;
    @FXML private TableColumn<DetalleVenta, Void> colAcciones;

    private ObservableList<DetalleVenta> detallesVenta = FXCollections.observableArrayList();
    private float totalAcumulado = 0.0f;
    
    private VentaService ventaService = new VentaService();
    private InventService inventService = new InventService();
    private CajaService caja = new CajaService();
    
    private ContextMenu popupAutocompletado = new ContextMenu();

    @FXML
    public void initialize() {
        colIDProducto.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tablaDetalleVenta.setItems(detallesVenta);
        configurarColumnaAcciones();
        configurarBuscadorProductos();

        // Cargar y validar usuario de la sesión
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario != null) {
            dniLabel.setText(usuario.getDni());
            nameLabel.setText(usuario.getNomYape());
        }
    }

    // --- SISTEMA DE BÚSQUEDA Y VALIDACIÓN DE STOCK ---
    private void configurarBuscadorProductos() {
        buscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                popupAutocompletado.hide();
                return;
            }

            List<Producto> coincidencias = ventaService.buscarProductosPorFiltro(newValue);
            popupAutocompletado.getItems().clear();

            if (!coincidencias.isEmpty()) {
                for (Producto p : coincidencias) {
                    MenuItem item = new MenuItem(p.getNombre() + " - $" + p.getPrecio() + " (Stock: " + p.getStock() + ")");
                    
                    item.setOnAction(event -> {
                        if (p.getStock() > 0) {
                            agregarOActualizarProductoEnTabla(p);
                            buscarProducto.clear();
                        } else {
                            mostrarAlerta("Sin Stock", "El producto '" + p.getNombre() + "' no tiene stock disponible.");
                        }
                    });
                    popupAutocompletado.getItems().add(item);
                }
                popupAutocompletado.show(buscarProducto, Side.BOTTOM, 0, 0);
            } else {
                popupAutocompletado.hide();
            }
        });
    }

    @FXML
    public void buscarProducto() {
        if (!buscarProducto.getText().isEmpty()) {
            popupAutocompletado.show(buscarProducto, Side.BOTTOM, 0, 0);
        }
    }

    private void agregarOActualizarProductoEnTabla(Producto producto) {
        int stockDisponible = producto.getStock();

        for (DetalleVenta detalle : detallesVenta) {
            if (detalle.getId_producto() == producto.getId()) {
                // Validación de stock al incrementar desde el buscador
                if (detalle.getCantidad() + 1 > stockDisponible) {
                    mostrarAlerta("Stock insuficiente", 
                        "No puede agregar más unidades de '" + producto.getNombre() + 
                        "'. Stock disponible: " + stockDisponible + " unidades.");
                    return;
                }
                detalle.setCantidad(detalle.getCantidad() + 1);
                tablaDetalleVenta.refresh();
                actualizarTotal();
                return;
            }
        }

        // Si es el primer elemento a agregar
        if (1 > stockDisponible) {
            mostrarAlerta("Stock insuficiente", "El producto '" + producto.getNombre() + "' no cuenta con stock suficiente.");
            return;
        }

        DetalleVenta nuevoDetalle = new DetalleVenta(producto.getNombre(), 1, producto.getPrecio(), producto.getId());
        detallesVenta.add(nuevoDetalle);
        actualizarTotal();
    }

    @FXML
    private void confirmarVenta() {
        try {
            // 1. Validar Sesión Activa
            Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
            if (usuarioActual == null) {
                mostrarAlerta("Sesión no válida", "La sesión ha caducado. Debe iniciar sesión nuevamente.");
                return;
            }

            String dniVendedor = dniLabel.getText().trim();

            // 2. Validar que la Caja del usuario esté abierta
            int idCaja = caja.obtenerIdCajaAbierta(dniVendedor);
            if (idCaja == -1) {
                mostrarAlerta("Caja Cerrada", "La caja actual se encuentra cerrada. No se pueden procesar ventas.");
                return;
            }

            // 3. Validar cliente y carrito
            String docCliente = campoCliente.getText().trim();
            if (docCliente.isEmpty() || docCliente.contains("Presione")) {
                mostrarAlerta("Error", "Debe seleccionar un cliente.");
                return;
            }
            if (detallesVenta.isEmpty()) {
                mostrarAlerta("Error", "El carrito está vacío.");
                return;
            }

            int idCliente = ventaService.obtenerIdCliente(docCliente);
            int idUsuario = ventaService.obtenerIdUsuario(dniVendedor);

            if (idCliente == -1) {
                mostrarAlerta("Error", "Cliente no encontrado. DNI ingresado: " + docCliente);
                return;
            }
            if (idUsuario == -1) {
                mostrarAlerta("Error", "Vendedor no encontrado. DNI: " + dniVendedor);
                return;
            }

            // 4. Registro y actualización de stock
            for (DetalleVenta detalle : detallesVenta) {
                ventaService.descontarStock(detalle.getId_producto(), detalle.getCantidad());
            }

            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            Venta nuevaVenta = new Venta(timestamp, totalAcumulado, idUsuario, idCliente, idCaja);
            ventaService.registrarVenta(nuevaVenta, detallesVenta);

            mostrarAlerta("Éxito", "Venta realizada correctamente.");
            limpiarCampos();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al procesar la venta: " + e.getMessage());
        }
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnMenos = new Button("-");
            private final Button btnMas = new Button("+");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(5, btnMenos, btnMas, btnEliminar);

            {
                contenedor.setAlignment(Pos.CENTER);

                // Botón '+' con comprobación de stock en BD
                btnMas.setOnAction(e -> {
                    DetalleVenta d = getTableView().getItems().get(getIndex());
                    Producto p = inventService.buscarProductoPorId(d.getId_producto());
                    int stockMaximo = (p != null) ? p.getStock() : 0;

                    if (d.getCantidad() + 1 > stockMaximo) {
                        mostrarAlerta("Stock insuficiente", 
                            "No hay más stock disponible de '" + d.getNombre() + "'. Máximo disponible: " + stockMaximo);
                    } else {
                        d.setCantidad(d.getCantidad() + 1);
                        tablaDetalleVenta.refresh();
                        actualizarTotal();
                    }
                });

                btnMenos.setOnAction(e -> {
                    DetalleVenta d = getTableView().getItems().get(getIndex());
                    if (d.getCantidad() > 1) {
                        d.setCantidad(d.getCantidad() - 1);
                        tablaDetalleVenta.refresh();
                        actualizarTotal();
                    }
                });

                btnEliminar.setOnAction(e -> {
                    detallesVenta.remove(getTableView().getItems().get(getIndex()));
                    actualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    @FXML
    public void openTheWindowClients(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/ClientsList.fxml"));
            Parent root = loader.load();

            ClientsListController controller = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.initOwner(parentStage);
            stage.initModality(Modality.WINDOW_MODAL);

            stage.showAndWait();

            String clienteSeleccionado = controller.getClienteSeleccionado();
            if (clienteSeleccionado != null && campoCliente != null) {
                campoCliente.setText(clienteSeleccionado);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarTotal() {
        totalAcumulado = 0;
        for (DetalleVenta d : detallesVenta) {
            totalAcumulado += d.getSubtotal();
        }
        lblTotal.setText(String.format("$ %.2f", totalAcumulado));
    }

    @FXML
    private void cancelarVenta() {
        limpiarCampos();
    }

    private void limpiarCampos() {
        campoCliente.clear();
        buscarProducto.clear();
        detallesVenta.clear();
        actualizarTotal();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}