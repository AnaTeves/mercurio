package app.Controllers;
import javafx.fxml.FXML;

import java.util.Map;
import app.BDD.VentaService;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

/* Controlador del administrador que hereda del controlador de metodos comunes */
public class AdminController extends ComunesController {

    @FXML
    private ComboBox<String> monthComboBox;
    @FXML
    private BarChart<String, Number> stockBarChart;
    @FXML
    private BarChart<String, Number> ventasPorDiaChart;
    @FXML
    private LineChart<String, Number> ingresosLineChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    private VentaService ventaService = new VentaService();
    @FXML
    private BorderPane mainBorderPane;
    @FXML GridPane gridPane;
    @FXML
    private StackPane mainContent;
    @FXML
    private Label lblIngresosDia, lblIngresosMes, lblGananciaNeta;

    @FXML
    public void initialize(){
        super.initialize();
        cargarMeses();
        ingresosDelDia();
        cargarVentasPorDia();
        // Ganancia neta en desarrollo
        lblGananciaNeta.setText("S/ 0.00");
    }

    // FUNCIONA Y SE USA
    public void ingresosDelDia(){
        double ingresos = ventaService.obtenerIngresosDelDia();
        lblIngresosDia.setText(String.format("S/ %.2f", ingresos));
    }

    // FUNCIONA Y SE USA
    public void cargarMeses(){
        monthComboBox.setItems(FXCollections.observableArrayList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));

        // Evento al seleccionar un mes
        monthComboBox.setOnAction(event -> {
            String mesSeleccionado = monthComboBox.getSelectionModel().getSelectedItem();
            if (mesSeleccionado != null) {
                int numeroMes = monthComboBox.getItems().indexOf(mesSeleccionado) + 1;
                ingresosMensuales(numeroMes);
            }
        });
    }

    public void cargarVentasPorDia() {
        ingresosLineChart.getData().clear();
        ingresosLineChart.setTitle("Comparación de ingresos mensuales");
        yAxis.setLabel("Ingresos ($)");

        Map<String, Double> ingresosPorMes = ventaService.obtenerIngresosPorMes();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos por mes");

        for (Map.Entry<String, Double> entry : ingresosPorMes.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        ingresosLineChart.getData().add(series);
        System.out.println("exito");
    }

    public void cargarProductosVendidos(){
        stockBarChart.setData(ventaService.obtenerProductosVendidos());

        // Rotar las etiquetas del eje X para que no se sobrepongan
        CategoryAxis xAxis = (CategoryAxis) stockBarChart.getXAxis();
        xAxis.setTickLabelRotation(45); // Gira las etiquetas 45 grados
    }

    // FUNCIONA Y SE USA
    private void ingresosMensuales(int mesSeleccionado) {
        double ingresosMensuales = ventaService.obtenerIngresosMensuales(mesSeleccionado);
        lblIngresosMes.setText(String.format("S/ %.2f", ingresosMensuales));
    }

    // Metodo que carga la vista de usuarios
    @FXML
    public void userManagement(){
        setView("/resources/UserView.fxml");
    }

    // Metodo que carga la vista del backup
    @FXML
    public void openBackupForm(){
        setView("/resources/BackupForm.fxml");
    }

    // Metodo que carga la vista del formulario para editar el perfil
    @FXML
    public void editProfile(){
        setView("/resources/ProfileForm.fxml");
    }

    // Metodo que cierra la sesion heredada de los metodos comunes
    @FXML
    public void logout(){
        handleLogout();
    }
}
