package app.Controllers;

import app.BDD.AuditoriaService;
import app.BDD.VentaService;
import app.Models.Auditoria;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Map;

public class AdminController extends ComunesController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private LineChart<String, Number> ingresosLineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    
    @FXML private BorderPane mainBorderPane;
    @FXML private GridPane gridPane;
    @FXML private StackPane mainContent;
    @FXML private Label lblIngresosDia, lblIngresosMes, lblGananciaNeta;

    @FXML private TableView<Auditoria> tablaAuditoriaPreview;
    @FXML private TableColumn<Auditoria, String> colAudFecha;
    @FXML private TableColumn<Auditoria, String> colAudUsuario;
    @FXML private TableColumn<Auditoria, String> colAudAccion;

    private VentaService ventaService = new VentaService();
    private AuditoriaService auditoriaService = new AuditoriaService();

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        
        // CORRECCIÓN CRÍTICA: Registrar el contenedor en ComunesController para que setView() funcione
        ComunesController.setMainBorderPane(mainBorderPane);

        cargarMeses();
        ingresosDelDia();
        cargarVentasPorDia();
        cargarGananciaNeta();

        if (colAudFecha != null && colAudUsuario != null && colAudAccion != null) {
            colAudFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
            colAudUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
            colAudAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        }

        cargarResumenAuditoria();
    }

    public void ingresosDelDia() {
        double ingresos = ventaService.obtenerIngresosDelDia();
        lblIngresosDia.setText(String.format("S/ %.2f", ingresos));
    }

    public void cargarGananciaNeta() {
        double gananciaNeta = ventaService.obtenerGananciaNeta();
        lblGananciaNeta.setText(String.format("S/ %.2f", gananciaNeta));
    }

    public void cargarMeses() {
        monthComboBox.setItems(FXCollections.observableArrayList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));

        monthComboBox.setOnAction(event -> {
            String mesSeleccionado = monthComboBox.getSelectionModel().getSelectedItem();
            if (mesSeleccionado != null) {
                int numeroMes = monthComboBox.getItems().indexOf(mesSeleccionado) + 1;
                ingresosMensuales(numeroMes);
            }
        });
    }

    private void cargarResumenAuditoria() {
        if (tablaAuditoriaPreview != null) {
            tablaAuditoriaPreview.setItems(auditoriaService.obtenerUltimosMovimientos(5));
        }
    }

    @FXML
    private void handleVerAuditoria() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/AuditoriaView.fxml"));
            Node view = loader.load();

            mainContent.getChildren().clear();
            mainContent.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cargarVentasPorDia() {
        if (ingresosLineChart == null) return;

        ingresosLineChart.getData().clear();
        ingresosLineChart.setTitle("Comparación de ingresos mensuales");

        Map<String, Double> ingresosPorMes = ventaService.obtenerIngresosPorMes();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos por mes");

        if (ingresosPorMes != null) {
            for (Map.Entry<String, Double> entry : ingresosPorMes.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }

        ingresosLineChart.getData().add(series);
    }

    private void ingresosMensuales(int mesSeleccionado) {
        double ingresosMensuales = ventaService.obtenerIngresosMensuales(mesSeleccionado);
        lblIngresosMes.setText(String.format("S/ %.2f", ingresosMensuales));
    }

    @FXML public void userManagement() { setView("/resources/UserView.fxml"); }
    @FXML public void openBackupForm() { setView("/resources/BackupForm.fxml"); }
    @FXML public void editProfile() { setView("/resources/ProfileForm.fxml"); }

    @FXML
    public void handleReports() {
        mainBorderPane.setCenter(mainContent);
    }

    @FXML
    public void logout() {
        handleLogout();
    }
}