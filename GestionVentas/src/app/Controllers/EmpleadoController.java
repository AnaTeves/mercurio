package app.Controllers;

import java.io.IOException;

import java.sql.Timestamp;

import app.BDD.VentaService;
import app.BDD.CajaService; 
import app.Models.Usuario;
import app.Models.Venta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;

// Controlador del empleado que hereda del controlador de metodos comunes
public class EmpleadoController extends ComunesController {
    
    // Tabla y columnas de la tabla de ventas del día
    @FXML private TableView<Venta> tableViewVentasDia;
    @FXML private TableColumn<Venta, Timestamp> fechaventaCol;
    @FXML private TableColumn<Venta, Float> totalventaCol;
    @FXML private TableColumn<Venta, String> dniclienteCol;
    @FXML private TableColumn<Venta, String> detalleCol;
    
    // Etiquetas de resumen
    @FXML private Label lblTotalVentasDia;
    @FXML private Label lblMontoTotalDia;

    private VentaService ventaService = new VentaService(); 
    private CajaService cajaService = new CajaService();

    @FXML private StackPane mainContent;
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox menuLateral; 
    @FXML private GridPane gridPane;
    
    // Usamos tu SessionManager para obtener el usuario actual
    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        super.initialize();
        
        // Configuración de las columnas de la tabla
        fechaventaCol.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        totalventaCol.setCellValueFactory(new PropertyValueFactory<>("totalVenta"));
        dniclienteCol.setCellValueFactory(new PropertyValueFactory<>("dni_cliente"));
        
        // Configurar columna de detalle con botón
        detalleCol.setCellFactory(col -> new TableCell<Venta, String>() {
            private final Button btnVerDetalle = new Button("Ver Detalle");
            
            {
                btnVerDetalle.setStyle("-fx-background-color: rgb(90, 108, 128); -fx-text-fill: White; -fx-cursor: hand;");
                btnVerDetalle.setOnAction(event -> {
                    Venta venta = getTableView().getItems().get(getIndex());
                    verDetalleVenta(venta);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVerDetalle);
                }
            }
        });

        // Verificamos el estado de la caja antes de permitirle hacer nada
        verificarYMostrarCaja();
    }

    private void cargarVentasDelDia() {
        Usuario usuarioActual = sessionManager.getCurrentUser();
        if (usuarioActual == null) return;
        
        String dniUsuario = usuarioActual.getDni();
        
        try {
            // Cargar ventas del día del usuario actual
            ObservableList<Venta> ventasDia = FXCollections.observableArrayList(
                ventaService.obtenerVentasDelDiaPorUsuario(dniUsuario)
            );
            tableViewVentasDia.setItems(ventasDia);
            
            // Calcular resumen
            int totalVentas = ventasDia.size();
            double montoTotal = ventasDia.stream().mapToDouble(Venta::getTotalVenta).sum();
            
            lblTotalVentasDia.setText(String.valueOf(totalVentas));
            lblMontoTotalDia.setText(String.format("S/ %.2f", montoTotal));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verDetalleVenta(Venta venta) {
        // Por ahora, mostramos una alerta con el detalle
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Detalle de Venta");
        alert.setHeaderText("Detalle de la venta");
        alert.setContentText(
            "Fecha: " + venta.getFechaVenta() + "\n" +
            "Total: S/ " + venta.getTotalVenta() + "\n" +
            "Cliente: " + venta.getDni_cliente()
        );
        alert.showAndWait();
    }

    // ==========================================
    // LÓGICA DE APERTURA DE CAJA (Refactorizada con FXML)
    // ==========================================

    private void verificarYMostrarCaja() {
        Usuario usuarioActual = sessionManager.getCurrentUser();
        
        if (usuarioActual == null) {
            mostrarAlertaError("Error de Sesión", "No se pudo recuperar el usuario logueado.");
            return;
        }

        String dniEmpleado = usuarioActual.getDni();
        boolean tieneCajaAbierta = cajaService.isCajaAbierta(dniEmpleado);

        if (tieneCajaAbierta) {
            if (menuLateral != null) menuLateral.setDisable(false);
            cargarVentasDelDia(); 
        } else {
            mostrarPantallaApertura(dniEmpleado);
        }
    }

    private void mostrarPantallaApertura(String dniEmpleado) {
        if (menuLateral != null) menuLateral.setDisable(true);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/AperturaCaja.fxml"));
            Parent root = loader.load();

            // Pasamos el DNI al controlador de apertura
            AperturaCajaController controller = loader.getController();
            controller.inicializarDatos(dniEmpleado);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal
            stage.setTitle("Apertura de Caja");
            stage.setScene(new Scene(root));
            
            // Forzamos a que no puedan cerrar la ventana desde la "X"
            stage.setOnCloseRequest(event -> event.consume()); 
            
            stage.showAndWait();

            // Si la caja se abrió correctamente, habilitamos todo
            if (controller.isCajaAbierta()) {
                if (menuLateral != null) menuLateral.setDisable(false);
                cargarVentasDelDia();
            } else {
                // Si por alguna razón forzó el cierre sin abrir caja, lo sacamos del sistema
                handleLogout();
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaError("Error", "No se pudo cargar la vista de apertura de caja.");
        }
    }

    // ==========================================
    // LÓGICA DE CIERRE DE CAJA
    // ==========================================

    @FXML
    public void cerrarSesion() {
        Usuario usuarioActual = sessionManager.getCurrentUser();
        
        // 1. Verificamos si hay una caja abierta
        if (usuarioActual != null && cajaService.isCajaAbierta(usuarioActual.getDni())) {
            try {
                // Cargar la ventana de cierre (esto ya lo tienes)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/CierreCaja.fxml"));
                Parent root = loader.load();

                CierreCajaController controller = loader.getController();
                controller.inicializarDatos(usuarioActual.getDni());

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Cierre de Caja Obligatorio");
                stage.setScene(new Scene(root));
                
                // IMPORTANTE: Si el usuario cierra esta ventanita con la X, NO lo deslogueamos
                stage.showAndWait(); 

                // 2. SOLO si la caja se cerró en la BDD, procedemos al logout
                if (controller.isCajaCerrada()) {
                    handleLogout(); // Este llama al del padre (ComunesController)
                } else {
                    mostrarAlertaError("Cierre Cancelado", "Debes cerrar la caja para poder salir del sistema.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Si no hay usuario o no hay caja abierta, sale directo
            handleLogout();
        }
    }

    // ==========================================
    // MÉTODOS DE NAVEGACIÓN
    // ==========================================

    @FXML
    public void handleInventario(){
        setView("/resources/InventarioView.fxml");
    }

    @FXML
    public void handleClientes(){
        setView("/resources/ClientesView.fxml");
    }

    @FXML
    public void handleVentas(){
        setView("/resources/FormVenta.fxml");
    }

    @FXML
    public void handleConsultaStock(){
        setView("/resources/InventarioView.fxml");
    }

    @FXML
    public void handleArqueoCaja(){
        setView("/resources/CierreCaja.fxml");
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
