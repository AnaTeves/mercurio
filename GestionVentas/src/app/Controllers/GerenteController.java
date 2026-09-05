package app.Controllers;

import app.BDD.DatabaseConnection;
import app.BDD.VentaService;
import app.Models.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    @FXML private GridPane gridPane;
    @FXML private ComboBox<String> comboReporte;
    @FXML private DatePicker fechaInicio, fechaFin;
    @FXML private PieChart graficoReporte;
    @FXML private ComboBox<String> comboMeses;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    private boolean actualizandoFiltros = false;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        ComunesController.setMainBorderPane(mainBorderPane);

        // 1. Cargar componentes del Dashboard
        cargarProductosStockBajo();
        cargarRankingProductos();
        cargarProductosMasVendidos();
        inicializarComboVendedores();

        // 2. Cargar meses en el ComboBox
        comboMeses.setItems(FXCollections.observableArrayList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));

        // 3. Evento: Al seleccionar un MES -> Limpiar Fechas y filtrar
        comboMeses.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !actualizandoFiltros) {
                actualizandoFiltros = true;
                dpDesde.setValue(null);
                dpHasta.setValue(null);
                actualizandoFiltros = false;
                aplicarFiltroComparativo();
            }
        });

        // 4. Evento: Al seleccionar FECHAS -> Limpiar Mes y filtrar
        dpDesde.valueProperty().addListener((obs, oldVal, newVal) -> manejarCambioFecha(newVal));
        dpHasta.valueProperty().addListener((obs, oldVal, newVal) -> manejarCambioFecha(newVal));

        // 5. Evento: Cambio de Vendedor
        comboVendedores.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroComparativo());
    }

    private void manejarCambioFecha(LocalDate newVal) {
        if (newVal != null && !actualizandoFiltros) {
            actualizandoFiltros = true;
            comboMeses.setValue(null);
            actualizandoFiltros = false;
            aplicarFiltroComparativo();
        }
    }

    private void aplicarFiltroComparativo() {
        String vendedor = comboVendedores.getValue();
        if (vendedor == null || vendedor.isEmpty()) return;

        String mesSeleccionado = comboMeses.getValue();
        Integer numeroMes = (mesSeleccionado != null) ? comboMeses.getItems().indexOf(mesSeleccionado) + 1 : null;
        
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        cargarComparativoVendedores(vendedor, numeroMes, desde, hasta);
    }

    // Sobrecarga por compatibilidad si requieres llamada simple
    private void cargarComparativoVendedores(String vendedor) {
        cargarComparativoVendedores(vendedor, null, null, null);
    }

    private void cargarComparativoVendedores(String vendedor, Integer mes, LocalDate desde, LocalDate hasta) {
        try {
            Map<String, Integer> ventasPorVendedor = ventaService.obtenerVentasPorVendedor(vendedor, mes, desde, hasta);
            vendedoresBarChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventas de " + vendedor);

            for (Map.Entry<String, Integer> entry : ventasPorVendedor.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            vendedoresBarChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inicializarComboVendedores() {
        comboVendedores.setItems(FXCollections.observableArrayList(obtenerNombresVendedores()));
        // El listener de cambio de selección se gestiona directamente en initialize()
    }

    private List<String> obtenerNombresVendedores() {
        List<String> vendedores = new ArrayList<>();
        String query = "SELECT nombreyape FROM USUARIO WHERE id_perfil = 3";
        
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

    private void cargarRankingProductos() {
        try {
            List<Pair<String, Integer>> productosRanking = ventaService.obtenerProductosMasVendidos();
            rankingProductosPieChart.getData().clear();
            
            for (Pair<String, Integer> producto : productosRanking) {
                PieChart.Data data = new PieChart.Data(producto.getKey(), producto.getValue());
                rankingProductosPieChart.getData().add(data);
            }
            
            double sum = 0;
            for (PieChart.Data d : rankingProductosPieChart.getData()) {
                sum += d.getPieValue();
            }
            
            for (PieChart.Data d : rankingProductosPieChart.getData()) {
                double porcentaje = (sum > 0) ? (d.getPieValue() / sum) * 100 : 0;
                d.setName(d.getName() + " (" + String.format("%.2f", porcentaje) + "%)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarProductosMasVendidos() {
        productosMasVendidosBarChart.setData(ventaService.obtenerProductosVendidos());
        
        CategoryAxis xAxis = (CategoryAxis) productosMasVendidosBarChart.getXAxis();
        xAxis.setTickLabelsVisible(false);

        for (XYChart.Series<String, Number> series : productosMasVendidosBarChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " vendidos");
                Tooltip.install(data.getNode(), tooltip);
            }
        }

        cargarRankingListados();
    }

    private void cargarRankingListados() {
        try {
            List<Pair<String, Integer>> productosTodosConVentas = ventaService.obtenerTodosProductosConVentas();
            List<Pair<String, Integer>> productosMasVendidos = ventaService.obtenerProductosMasVendidos();

            topMasVendidosContainer.getChildren().clear();
            topMenosVendidosContainer.getChildren().clear();

            for (int i = 0; i < Math.min(5, productosMasVendidos.size()); i++) {
                Pair<String, Integer> producto = productosMasVendidos.get(i);
                Label label = new Label((i + 1) + ". " + producto.getKey() + " (" + producto.getValue() + ")");
                label.setStyle("-fx-font-size: 10px;");
                topMasVendidosContainer.getChildren().add(label);
            }

            for (int i = 0; i < Math.min(5, productosTodosConVentas.size()); i++) {
                Pair<String, Integer> producto = productosTodosConVentas.get(i);
                Label label = new Label((i + 1) + ". " + producto.getKey() + " (" + producto.getValue() + ")");
                label.setStyle("-fx-font-size: 10px;");
                topMenosVendidosContainer.getChildren().add(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
            topMasVendidosContainer.getChildren().clear();
            topMenosVendidosContainer.getChildren().clear();
            topMasVendidosContainer.getChildren().add(new Label("Error al cargar datos"));
            topMenosVendidosContainer.getChildren().add(new Label("Error al cargar datos"));
        }
    }

    @FXML public void handleInventario() { setView("/resources/InventarioView.fxml"); }
    @FXML public void handleAnularVentas() { setView("/resources/VentasView.fxml"); }
    @FXML public void handleArqueoCaja() { setView("/resources/ArqueoCajaView.fxml"); }
    @FXML public void handleGestionPrecios() { setView("/resources/InventarioView.fxml"); }
    @FXML public void handleReports() { mainBorderPane.setCenter(mainContent); }
    @FXML public void cerrarSesion() { handleLogout(); }
    @FXML public void handleCategorias() { setView("/resources/CategoriasView.fxml"); }

    @FXML
    public void showAlert(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}