package app.Controllers;

import app.BDD.VentaService;
import app.Models.Usuario;
import app.Models.DetalleVenta;
import app.Models.Producto;
import app.Models.Venta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    // Menú flotante para el autocompletado
    private ContextMenu popupAutocompletado = new ContextMenu();

    @FXML
    public void initialize() {
        colIDProducto.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tablaDetalleVenta.setItems(detallesVenta);
        configurarColumnaAcciones();

        // Configurar el autocompletado de productos
        configurarBuscadorProductos();

        // Cargar usuario de la sesión
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario != null) {
            dniLabel.setText(usuario.getDni());
            nameLabel.setText(usuario.getNomYape());
        }
    }

    // --- NUEVO SISTEMA DE BÚSQUEDA ---
    private void configurarBuscadorProductos() {
        buscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                popupAutocompletado.hide();
                return;
            }

            // Buscamos coincidencias en la base de datos
            List<Producto> coincidencias = ventaService.buscarProductosPorFiltro(newValue);
            popupAutocompletado.getItems().clear();

            if (!coincidencias.isEmpty()) {
                for (Producto p : coincidencias) {
                    // Creamos el item del menú (Ej: "Coca Cola - $1500.0 | Stock: 10")
                    MenuItem item = new MenuItem(p.getNombre() + " - $" + p.getPrecio() + " (Stock: " + p.getStock() + ")");
                    
                    item.setOnAction(event -> {
                        if (p.getStock() > 0) {
                            agregarOActualizarProductoEnTabla(p);
                            buscarProducto.clear(); // Limpiamos el buscador para el siguiente
                        } else {
                            mostrarAlerta("Sin Stock", "El producto " + p.getNombre() + " no tiene stock disponible.");
                        }
                    });
                    popupAutocompletado.getItems().add(item);
                }
                // Mostramos el menú debajo del TextField
                popupAutocompletado.show(buscarProducto, Side.BOTTOM, 0, 0);
            } else {
                popupAutocompletado.hide();
            }
        });
    }

    @FXML
    public void buscarProducto() {
        // Este método se mantiene por si el usuario presiona el botón "Buscar" (la lupa)
        // en lugar del autocompletado, pero ahora abrirá el menú si hay texto.
        if(!buscarProducto.getText().isEmpty()){
            popupAutocompletado.show(buscarProducto, Side.BOTTOM, 0, 0);
        }
    }
    // ----------------------------------

    private void agregarOActualizarProductoEnTabla(Producto producto) {
        for (DetalleVenta detalle : detallesVenta) {
            if (detalle.getId_producto() == producto.getId()) {
                detalle.setCantidad(detalle.getCantidad() + 1);
                tablaDetalleVenta.refresh();
                actualizarTotal();
                return;
            }
        }
        
        DetalleVenta nuevoDetalle = new DetalleVenta(producto.getNombre(), 1, producto.getPrecio(), producto.getId());
        detallesVenta.add(nuevoDetalle);
        actualizarTotal();
    }

    @FXML
    public void openTheWindowClients() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/ClientsList.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            ClientsListController controller = loader.getController();
            // IMPORTANTE: Asegúrate de que esto devuelve el DOCUMENTO (DNI), no el nombre
            String clienteSeleccionado = controller.getClienteSeleccionado();

            if (clienteSeleccionado != null) {
                campoCliente.setText(clienteSeleccionado);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void confirmarVenta() {
        registrarVenta();
    }

    @FXML
    private void registrarVenta() {
        try {
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

            // 1. LIMPIEZA DE DATOS (Quitamos espacios para evitar errores)
            String docCliente = campoCliente.getText().trim(); 
            String dniVendedor = dniLabel.getText().trim();

            if (docCliente.isEmpty() || docCliente.contains("Presione")) {
                mostrarAlerta("Error", "Debe seleccionar un cliente.");
                return;
            }
            if (detallesVenta.isEmpty()) {
                mostrarAlerta("Error", "El carrito está vacío.");
                return;
            }

            // 2. OBTENER IDs DESDE LA BDD
            int idCliente = ventaService.obtenerIdCliente(docCliente);
            int idUsuario = ventaService.obtenerIdUsuario(dniVendedor);

            // 3. VALIDACIÓN ESTRICTA
            if (idCliente == -1) {
                mostrarAlerta("Error", "Cliente no encontrado. Asegúrese de que el buscador ingresó el DNI del cliente: " + docCliente);
                return;
            }
            if (idUsuario == -1) {
                mostrarAlerta("Error", "Vendedor no encontrado. (DNI detectado: " + dniVendedor + ")");
                return;
            }

            // 4. PROCESAR VENTA
            for (DetalleVenta detalle : detallesVenta) {
                ventaService.descontarStock(detalle.getId_producto(), detalle.getCantidad());
            }

            Venta nuevaVenta = new Venta(timestamp, totalAcumulado, idUsuario, idCliente);
            ventaService.registrarVenta(nuevaVenta, detallesVenta);

            mostrarAlerta("Éxito", "Venta realizada correctamente.");
            limpiarCampos();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al procesar: " + e.getMessage());
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnMenos = new Button("-");
            private final Button btnMas = new Button("+");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(5, btnMenos, btnMas, btnEliminar);
            {
                contenedor.setAlignment(Pos.CENTER);
                btnMas.setOnAction(e -> {
                    DetalleVenta d = getTableView().getItems().get(getIndex());
                    d.setCantidad(d.getCantidad() + 1);
                    tablaDetalleVenta.refresh();
                    actualizarTotal();
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
}