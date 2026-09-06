package app.Controllers;

import app.BDD.AuditoriaService;
import app.Models.Auditoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class AuditoriaController {
    private AdminController adminController;

    // Método para recibir la referencia del AdminController
    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    @FXML
    public void handleVolver(ActionEvent event) {
        System.out.println("¡Apretaste el botón Volver!");
        if (adminController != null) {
            adminController.volverAlMainContent();
        } else {
            System.out.println("ERROR: adminController es NULL");
        }
    }

    // Componentes mapeados desde el FXML
    @FXML private ComboBox<String> cbModulo;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private TableView<Auditoria> tablaAuditoria;
    @FXML private TableColumn<Auditoria, String> colFecha;
    @FXML private TableColumn<Auditoria, String> colUsuario;
    @FXML private TableColumn<Auditoria, String> colModulo;
    @FXML private TableColumn<Auditoria, String> colAccion;
    @FXML private TableColumn<Auditoria, String> colDetalle;

    private final AuditoriaService auditoriaService = new AuditoriaService();
    private ObservableList<Auditoria> listaAuditoria = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Vinculación de columnas con los atributos del modelo Auditoria
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        if (colUsuario != null) colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        if (colModulo != null) colModulo.setCellValueFactory(new PropertyValueFactory<>("modulo"));
        if (colAccion != null) colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        if (colDetalle != null) colDetalle.setCellValueFactory(new PropertyValueFactory<>("detalle"));

        // Cargar opciones en el ComboBox de módulos
        if (cbModulo != null) {
            cbModulo.setItems(FXCollections.observableArrayList("Todos", "Usuarios", "Ventas", "Productos", "Clientes"));
            cbModulo.getSelectionModel().selectFirst();
        }

        cargarDatosAuditoria();
    }

    private void cargarDatosAuditoria() {
        if (tablaAuditoria != null) {
            listaAuditoria = auditoriaService.obtenerTodosLosMovimientos();
            tablaAuditoria.setItems(listaAuditoria);
        }
    }

    // Método invocado por el botón "Filtrar"
    @FXML
    public void handleFiltrar() {
    String moduloSeleccionado = cbModulo.getValue();
    LocalDate desde = dpDesde.getValue();
    LocalDate hasta = dpHasta.getValue();

    var filtrada = listaAuditoria.filtered(aud -> {
        // 1. Validar módulo
        boolean cumpleModulo = moduloSeleccionado == null || moduloSeleccionado.equals("Todos") 
                                || (aud.getModulo() != null && aud.getModulo().equalsIgnoreCase(moduloSeleccionado));

        // 2. Validar rango de fechas
        boolean cumpleFecha = true;
        LocalDate fechaRegistro = parsearFecha(aud.getFechaHora());

            if (fechaRegistro != null) {
                if (desde != null && fechaRegistro.isBefore(desde)) {
                    cumpleFecha = false;
                }
                if (hasta != null && fechaRegistro.isAfter(hasta)) {
                    cumpleFecha = false;
                }
            }

            return cumpleModulo && cumpleFecha;
        });

        tablaAuditoria.setItems(filtrada);
    }

    // Convierte la cadena de fecha recibida de la base de datos a un objeto LocalDate
    private LocalDate parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) return null;
        try {
            // Toma únicamente la parte "YYYY-MM-DD" ignorando la hora
            String soloFecha = fechaStr.trim().split(" ")[0];
            return LocalDate.parse(soloFecha);
        } catch (Exception e) {
            return null;
        }
    }

    // Método invocado por el botón "Limpiar"
    @FXML
    public void handleLimpiar() {
        if (cbModulo != null) cbModulo.getSelectionModel().selectFirst();
        if (dpDesde != null) dpDesde.setValue(null);
        if (dpHasta != null) dpHasta.setValue(null);

        cargarDatosAuditoria();
    }
}