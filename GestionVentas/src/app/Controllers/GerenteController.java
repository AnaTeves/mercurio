package app.Controllers;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import app.BDD.VentaService;
import app.BDD.DatabaseConnection;
import app.Models.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Tooltip;
import java.time.LocalDate;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javafx.scene.control.Label;
import javafx.util.Pair;

public class GerenteController extends ComunesController {

    private VentaService ventaService = new VentaService();
    @FXML private BarChart<String, Number> ventasPorDiaChart; 
    @FXML private LineChart<String, Number> ventasLineChart;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private BarChart<String, Number> vendedoresBarChart;
    @FXML private BarChart<String, Number> productosMasVendidosBarChart;
    @FXML private PieChart rankingProductosPieChart;
    @FXML private TableView<Producto> tablaStockBajo;
    @FXML private TableColumn<Producto, String> productoStockCol;
    @FXML private TableColumn<Producto, Integer> stockActualCol;
    @FXML private TableColumn<Producto, String> estadoStockCol;
    @FXML private VBox topMasVendidosContainer;
    @FXML private VBox topMenosVendidosContainer;
    @FXML private ComboBox<String> comboVendedores;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private StackPane mainContent;
    @FXML private BorderPane mainBorderPane;
    @FXML GridPane gridPane;
    @FXML private ComboBox<String> comboReporte;
    @FXML private DatePicker fechaInicio, fechaFin;
    @FXML private PieChart graficoReporte;

    @FXML
    public void initialize(){
        super.initialize();
        cargarProductosStockBajo();
        cargarRankingProductos();
        cargarProductosMasVendidos();
        inicializarComboVendedores();
    }

    private void inicializarComboVendedores() {
        // Cargar lista de vendedores desde la base de datos
        comboVendedores.setItems(FXCollections.observableArrayList(obtenerNombresVendedores()));
        
        comboVendedores.setOnAction(event -> {
            String vendedorSeleccionado = comboVendedores.getValue();
            if (vendedorSeleccionado != null) {
                cargarComparativoVendedores(vendedorSeleccionado);
            }
        });
    }

    private List<String> obtenerNombresVendedores() {
        List<String> vendedores = new ArrayList<>();
        String query = "SELECT nombreyape FROM USUARIO WHERE id_perfil = 3"; // Asumiendo que 3 es el perfil de vendedor
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                vendedores.add(rs.getString("nombreyape"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vendedores;
    }

    private void cargarProductosStockBajo() {
        try {
            List<Producto> productos = ventaService.obtenerProductos();
            ObservableList<Producto> productosStockBajo = FXCollections.observableArrayList();
            
            // Filtrar productos con stock bajo (menos de 10 unidades)
            for (Producto producto : productos) {
                if (producto.getStock() < 10) {
                    productosStockBajo.add(producto);
                }
            }
            
            productoStockCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            stockActualCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
            estadoStockCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
            
            tablaStockBajo.setItems(productosStockBajo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarComparativoVendedores(String vendedorSeleccionado) {
        try {
            Map<String, Integer> ventasPorVendedor = ventaService.obtenerVentasPorVendedor(vendedorSeleccionado);
            vendedoresBarChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventas de " + vendedorSeleccionado);
            
            for (Map.Entry<String, Integer> entry : ventasPorVendedor.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            vendedoresBarChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarRankingProductos() {
        try {
            List<Pair<String, Integer>> productosRanking = ventaService.obtenerProductosMasVendidos();
            rankingProductosPieChart.getData().clear();
            
            for (Pair<String, Integer> producto : productosRanking) {
                PieChart.Data data = new PieChart.Data(producto.getKey(), producto.getValue());
                rankingProductosPieChart.getData().add(data);
            }
            
            // Calcular porcentajes
            double sum = 0;
            for (PieChart.Data d : rankingProductosPieChart.getData()) {
                sum += d.getPieValue();
            }
            
            for (PieChart.Data d : rankingProductosPieChart.getData()) {
                double porcentaje = (d.getPieValue() / sum) * 100;
                d.setName(d.getName() + " (" + String.format("%.2f", porcentaje) + "%)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarProductosMasVendidos() {
        productosMasVendidosBarChart.setData(ventaService.obtenerProductosVendidos());
        
        // Ocultar las etiquetas del eje X
        CategoryAxis xAxis = (CategoryAxis) productosMasVendidosBarChart.getXAxis();
        xAxis.setTickLabelsVisible(false);

        // Agregar tooltips a cada barra
        for (XYChart.Series<String, Number> series : productosMasVendidosBarChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " vendidos");
                Tooltip.install(data.getNode(), tooltip);
            }
        }

        // Cargar listados de top 5 más y menos vendidos
        cargarRankingListados();
    }

    private void cargarRankingListados() {
        try {
            // Obtener todos los productos con sus ventas (ordenados ascendentemente por ventas)
            List<Pair<String, Integer>> productosTodosConVentas = ventaService.obtenerTodosProductosConVentas();

            // Obtener productos más vendidos (ordenados descendentemente)
            List<Pair<String, Integer>> productosMasVendidos = ventaService.obtenerProductosMasVendidos();

            // Limpiar contenedores
            topMasVendidosContainer.getChildren().clear();
            topMenosVendidosContainer.getChildren().clear();

            // Top 5 más vendidos (primeros 5 de la lista de más vendidos)
            for (int i = 0; i < Math.min(5, productosMasVendidos.size()); i++) {
                Pair<String, Integer> producto = productosMasVendidos.get(i);
                Label label = new Label((i + 1) + ". " + producto.getKey() + " (" + producto.getValue() + ")");
                label.setStyle("-fx-font-size: 10px;");
                topMasVendidosContainer.getChildren().add(label);
            }

            // Top 5 menos vendidos (primeros 5 de la lista ordenada ascendentemente)
            for (int i = 0; i < Math.min(5, productosTodosConVentas.size()); i++) {
                Pair<String, Integer> producto = productosTodosConVentas.get(i);
                Label label = new Label((i + 1) + ". " + producto.getKey() + " (" + producto.getValue() + ")");
                label.setStyle("-fx-font-size: 10px;");
                topMenosVendidosContainer.getChildren().add(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, mostrar mensaje de error
            topMasVendidosContainer.getChildren().clear();
            topMenosVendidosContainer.getChildren().clear();
            topMasVendidosContainer.getChildren().add(new Label("Error al cargar datos"));
            topMenosVendidosContainer.getChildren().add(new Label("Error al cargar datos"));
        }
    }

    /* Funcion que carga la vista de inventario */
    @FXML
    public void handleInventario(){
        setView("/resources/InventarioView.fxml");
    }

    /* Funcion que carga la vista para anular ventas */
    @FXML
    public void handleAnularVentas(){
        setView("/resources/VentasView.fxml");
    }

    /* Funcion que carga la vista de arqueo de caja */
    @FXML
    public void handleArqueoCaja(){
        setView("/resources/CierreCaja.fxml");
    }

    /* Funcion que carga la vista de gestion de precios */
    @FXML
    public void handleGestionPrecios(){
        setView("/resources/InventarioView.fxml");
    }

    @FXML
    public void handleReports(){
        mainBorderPane.setCenter(mainContent);
    }

    /* Funcion que cierra la sesion */
    @FXML
    public void cerrarSesion(){
        handleLogout();
    }

    @FXML
    public void handleCategorias(){
        setView("/resources/CategoriasView.fxml");
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
        LocalDate fromDateLocal = fechaInicio.getValue();
        LocalDate toDateLocal = fechaFin.getValue();

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