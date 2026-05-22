package app.Controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;

import app.BDD.VentaService;
import app.BDD.CajaService; 
import app.Models.Usuario;
import app.Models.Venta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Controlador del empleado que hereda del controlador de metodos comunes
public class EmpleadoController extends ComunesController {
    
    // Tabla y columnas de la tabla
    @FXML private TableView<Venta> tableView;
    @FXML private TableColumn<Venta, Timestamp> fechaventaCol;
    @FXML private TableColumn<Venta, Float> totalventaCol;
    @FXML private TableColumn<Venta, String> dniusuarioCol;
    @FXML private TableColumn<Venta, String> dniclienteCol;
    
    // Controlar las fechas de los reportes
    @FXML private DatePicker datePickerInicio;
    @FXML private DatePicker datePickerFin;

    private VentaService ventaService = new VentaService(); 
    private CajaService cajaService = new CajaService();

    @FXML private StackPane mainContent;
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox menuLateral; 
    @FXML private GridPane gridPane;
    
    private Node vistaInicial;
    
    // Usamos tu SessionManager para obtener el usuario actual
    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        super.initialize();
        vistaInicial = mainContent; // Asigno la vista inicial que contiene los reportes
        
        // Configuracion de las columnas de la tabla
        fechaventaCol.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        totalventaCol.setCellValueFactory(new PropertyValueFactory<>("totalVenta"));
        dniusuarioCol.setCellValueFactory(new PropertyValueFactory<>("dni_usuario"));
        dniclienteCol.setCellValueFactory(new PropertyValueFactory<>("dni_cliente"));

        // Verificamos el estado de la caja antes de permitirle hacer nada
        verificarYMostrarCaja();
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
            loadVentas(); 
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
                loadVentas();
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
    // MÉTODOS DE REPORTES Y NAVEGACIÓN
    // ==========================================

    private void loadVentas() {
        ObservableList<Venta> ventas = FXCollections.observableArrayList(ventaService.getAllVentas());
        tableView.setItems(ventas);
    }

    @FXML
    public void calcularReporteVentas() {
        LocalDate fechaInicio = datePickerInicio.getValue();
        LocalDate fechaFin = datePickerFin.getValue();

        if (fechaInicio != null && fechaFin != null && !fechaInicio.isAfter(fechaFin)) {
            try {
                Map<String, Object> reporte = ventaService.obtenerReporteVentas(fechaInicio, fechaFin);
                if (reporte != null) {
                    int totalVentas = (int) reporte.get("total_ventas");
                    double montoTotal = (double) reporte.get("monto_total");
                    mostrarReporte(totalVentas, montoTotal);
                } else {
                    mostrarReporte(0, 0.0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlertaError("Error al calcular el reporte", "Ha ocurrido un error al obtener los datos.");
            }
        } else {
            mostrarAlertaError("Fechas inválidas", "Por favor, selecciona un rango de fechas válido.");
        }
    }

    private void mostrarReporte(int totalVentas, double montoTotal) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Reporte de Ventas");
        alert.setHeaderText("Resumen del rango seleccionado");
        alert.setContentText(
                "Total de Ventas: " + totalVentas + "\n" +
                "Monto Generado: $" + String.format("%.2f", montoTotal)
        );
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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
    public void handleReportes(){
        mainBorderPane.setCenter(vistaInicial);
    }
}