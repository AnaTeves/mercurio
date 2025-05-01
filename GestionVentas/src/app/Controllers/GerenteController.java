package app.Controllers;
import java.util.List;
import app.BDD.VentaService;
import app.BDD.CategoriaService;
import app.BDD.DatabaseConnection;
// import app.BDD.UserService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;

public class GerenteController extends ComunesController {

    private CategoriaService categoriaService = new CategoriaService();
    private VentaService ventaService = new VentaService();
    @FXML
    private BarChart<String, Number> ventasPorDiaChart; 
    @FXML
    private LineChart<String, Number> ventasLineChart;
    @FXML
    private PieChart pieChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private StackPane mainContent;
    @FXML
    private BorderPane mainBorderPane;

    @FXML GridPane gridPane;
    private Node vistaInicial;
    @FXML
    private ComboBox<String> comboReporte;

    @FXML
    private DatePicker fechaInicio, fechaFin;

    @FXML
    private PieChart graficoReporte;

    @FXML
    public void initialize(){
        super.initialize();
        vistaInicial = mainContent;
        // Combo box para seleccionar el tipo de reporte
        comboReporte.setItems(FXCollections.observableArrayList("Productos más vendidos", "Categorías más vendidas"));

    }

    // Metodo que carga la vista del inventario
    @FXML
    public void handleInventario(){
        setView("/resources/InventarioView.fxml");
    }

    @FXML
    public void handleUsers(){
        setView("/resources/UserView.fxml");
    }

    // Metodo que carga la vista de las categorias
    @FXML
    public void handleCategorias(){
        setView("/resources/CategoriasView.fxml");
    }

    // Metodo que carga la vista de los clientes
    @FXML
    public void handleClientes(){
        setView("/resources/ClientesView.fxml");
    }

    // Metodo que carga la vista de las ventas
    @FXML
    public void handleVentas(){
        setView("/resources/VentasView.fxml");
    }

    @FXML
    public void handleReportes(){
        mainBorderPane.setCenter(vistaInicial);
    }

    @FXML
    public void cerrarSesion(){
        handleLogout();
    }

    // Metodo que genera el reporte de los mas vendidos (categorias o productos)
    @FXML
    private void generarReporte(){
        String opcionSeleccionada = comboReporte.getValue();
        LocalDate inicio = fechaInicio.getValue();
        LocalDate fin = fechaFin.getValue();
        filtrarDatos(); // REVIEW

        if (opcionSeleccionada == null || inicio == null || fin == null) {
            showAlert("Error", "Debe completar todos los campos para generar el reporte.");
            return;
        }

        if (opcionSeleccionada.equals("Productos más vendidos")) {
            generarGraficoProductos(inicio, fin);
        } else if (opcionSeleccionada.equals("Categorías más vendidas")) {
            generarGraficoCategorias(inicio, fin);
        }
    }

    private void generarGraficoProductos(LocalDate inicio, LocalDate fin) {
        // try{
        //     ObservableList<PieChart.Data> data = ventaService.obtenerProductosMasVendidos(inicio, fin);
        //     graficoReporte.setData(data);
        //     // graficoReporte.setTitle("Productos mas vendidos");
        // } catch(SQLException e){
        //     e.printStackTrace();
        // }
        try{
            ObservableList<PieChart.Data> data = ventaService.obtenerProductosMasVendidos(inicio, fin);
            graficoReporte.setData(data);
            // graficoReporte.setTitle("Categorias mas vendidas");

            double sum = 0;
            for (PieChart.Data d : data) {
                sum += d.getPieValue();
            }
                
            for (PieChart.Data d : data) {
                double porcentaje = (d.getPieValue() / sum) * 100;
                d.setName(d.getName() + " (" + String.format("%.2f", porcentaje) + "%)");
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void generarGraficoCategorias(LocalDate inicio, LocalDate fin) {
            ObservableList<PieChart.Data> data = categoriaService.obtenerVentasPorCategoriaFiltradas(inicio, fin);
            graficoReporte.setData(data);
            // graficoReporte.setTitle("Categorias mas vendidas");

            double sum = 0;
            for (PieChart.Data d : data) {
                sum += d.getPieValue();
            }
            
            for (PieChart.Data d : data) {
                double porcentaje = (d.getPieValue() / sum) * 100;
                d.setName(d.getName() + " (" + String.format("%.2f", porcentaje) + "%)");
            }
    }

    @FXML
    public void showAlert(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    } 

    public void filtrarDatos() {
        // Obtener las fechas de los DatePicker
        LocalDate fromDateLocal = fechaInicio.getValue();
        LocalDate toDateLocal = fechaFin.getValue();

        // Validar las fechas
        if (fromDateLocal == null || toDateLocal == null) {
            System.out.println("Por favor, seleccione ambas fechas para filtrar.");
            return;
        }
        if (fromDateLocal.isAfter(toDateLocal)) {
            System.out.println("La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }
    }
}