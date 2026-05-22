package app.Controllers;
import javafx.fxml.FXML;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import app.BDD.CategoriaService;
import app.BDD.VentaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
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
    private PieChart productosPieChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    private VentaService ventaService = new VentaService();
    @FXML
    private BorderPane mainBorderPane;
    @FXML GridPane gridPane;
    private Node vistaInicial;
    @FXML
    private StackPane mainContent;
    private CategoriaService categoriaService = new CategoriaService();
    @FXML
    private Label lblIngresosDia, lblIngresosMes;
    
    @FXML
    private ComboBox<String> cbMeses;

    

    @FXML
    public void initialize(){
        super.initialize();
        vistaInicial = mainContent;
        cargarMeses();
        ingresosDelDia();
        // cargarGraficoCategorias();
        cargarVentasPorDia();
        // cargarProductosVendidos();
        productosMasVendidos();

        
        
    }

    // FUNCIONA Y SE USA
    public void ingresosDelDia(){
        double ingresos = ventaService.obtenerIngresosDelDia();
        lblIngresosDia.setText(String.format("S/ %.2f", ingresos));
    }

    public void productosMasVendidos(){
        List<Pair<String, Integer>> productosMasVendidos = ventaService.obtenerProductosMasVendidos();
        productosPieChart.getData().clear(); // Limpiar datos previos

        for (Pair<String, Integer> producto : productosMasVendidos) {
            PieChart.Data data = new PieChart.Data(producto.getKey(), producto.getValue());
            productosPieChart.getData().add(data);
        }
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

    public void cargarGraficoCategorias(LocalDate fromDate, LocalDate toDate) {

        ObservableList<PieChart.Data> data;

        data = categoriaService.obtenerVentasPorCategoriaFiltradas(fromDate, toDate);

    // Actualizar el gráfico
        pieChart.setData(data);
    }    

    public void cargarGraficoCategorias() {

        ObservableList<PieChart.Data> data;
        // Cargo todos los datos de mi base de datos
        data = categoriaService.obtenerVentasPorCategoria();
        pieChart.setData(data);
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
        barChart.setData(ventaService.obtenerProductosVendidos());

        // Rotar las etiquetas del eje X para que no se sobrepongan
        CategoryAxis xAxis = (CategoryAxis) barChart.getXAxis();
        xAxis.setTickLabelRotation(45); // Gira las etiquetas 45 grados
    }

    // private void cargarDatosMensuales() {
    //     barChartMes.getData().clear();
    //     XYChart.Series<String, Number> series = new XYChart.Series<>();
    //     series.setName("Ingresos Mensuales");

    //     // Obtener datos de ingresos mensuales (de tu servicio)
    //     VentaService.obtenerIngresosMensuales().forEach((mes, total) -> {
    //         series.getData().add(new XYChart.Data<>(mes, total));
    //     });

    //     barChartMes.getData().add(series);
    // }

    // private void cargarDatosSemanalPorMes(String mesSeleccionado) {
    //     barChartMes.getData().clear();
    //     XYChart.Series<String, Number> series = new XYChart.Series<>();
    //     series.setName("Ingresos semanales de " + mesSeleccionado);

    //     // Obtener datos de ingresos semanales por mes (de tu servicio)
    //     VentaService.obtenerIngresosSemanalesPorMes(mesSeleccionado).forEach((semana, total) -> {
    //         series.getData().add(new XYChart.Data<>(semana, total));
    //     });

    //     barChartMes.getData().add(series);
    // }

    // FUNCIONA Y SE USA
    private void ingresosMensuales(int mesSeleccionado) {
        double ingresosMensuales = ventaService.obtenerIngresosMensuales(mesSeleccionado);
        lblIngresosMes.setText(String.format("S/ %.2f", ingresosMensuales));
    }

    // Metodo que carga la vista del inventario
    @FXML
    public void productManagement(){
        setView("/resources/InventarioView.fxml");
    }

    // Metodo que carga la vista de las categorias
    @FXML
    public void categoryManagement(){
        setView("/resources/CategoriasView.fxml");
    }

    // Metodo que carga la vista de las ventas
    @FXML
    public void seeSales(){
        setView("/resources/VentasView.fxml");
    }

    // Metodo que carga la vista de los reportes
    @FXML
    public void handleReports(){
        mainBorderPane.setCenter(vistaInicial);
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