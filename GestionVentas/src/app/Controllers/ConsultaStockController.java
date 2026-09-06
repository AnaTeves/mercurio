package app.Controllers;

import app.Models.Producto;
import app.BDD.InventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ConsultaStockController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Boolean> colEstado; // Se agrega la columna de estado

    private EmpleadoController vendedorController; 
    private final InventService productoService = new InventService();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarProductos();
        configurarFiltroBusqueda();
        resaltarStockCritico();
        configurarFormatoEstado();
    }

    public void setVendedorController(EmpleadoController vendedorController) {
        this.vendedorController = vendedorController;
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("id_categoria"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void cargarProductos() {
        listaProductos.clear();
        listaProductos.addAll(productoService.loadProducts());
    }

    private void configurarFiltroBusqueda() {
        FilteredList<Producto> listaFiltrada = new FilteredList<>(listaProductos, p -> true);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(producto -> {
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String filtro = newValue.toLowerCase().trim();

                boolean coincideNombre = producto.getNombre().toLowerCase().contains(filtro);
                boolean coincideId = String.valueOf(producto.getId()).contains(filtro);

                return coincideNombre || coincideId;
            });
        });

        SortedList<Producto> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(listaOrdenada);
    }

    private void resaltarStockCritico() {
        colStock.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);

                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(stock.toString());
                    if (stock == 0) {
                        setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (stock <= 5) {
                        setStyle("-fx-background-color: #ffe0b2; -fx-text-fill: #e65100; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    private void configurarFormatoEstado() {
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean estado, boolean empty) {
                super.updateItem(estado, empty);

                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (estado) {
                        setText("Activo");
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-alignment: CENTER;"); // Texto verde
                    } else {
                        setText("Inactivo");
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-alignment: CENTER;"); // Texto rojo
                    }
                }
            }
        });
    }
}