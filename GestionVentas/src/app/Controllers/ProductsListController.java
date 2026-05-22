package app.Controllers;

import app.BDD.InventService;
import app.Models.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class ProductsListController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Float> colPrecio;

    private InventService inventService = new InventService();
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    
    // Esta es la variable clave que enviaremos de vuelta a VentaController
    private Producto productoSeleccionado;

    @FXML
    public void initialize() {
        // Configuramos las columnas para que muestren los datos del modelo Producto
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tablaProductos.setItems(listaProductos);
    }

    @FXML
    private void buscarProducto() {
        String termino = txtBuscar.getText().trim();
        if (!termino.isEmpty()) {
            // Usamos tu servicio para buscar en la base de datos
            List<Producto> resultados = inventService.buscarProductoPorNombre(termino);
            listaProductos.setAll(resultados);
        } else {
            // Opcional: Si está vacío, cargar todos los productos
            listaProductos.clear();
        }
    }

    @FXML
    private void seleccionarProducto() {
        productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        
        if (productoSeleccionado != null) {
            // Cerramos la ventana emergente
            Stage stage = (Stage) tablaProductos.getScene().getWindow();
            stage.close();
        } else {
            // Podrías mostrar una alerta rápida aquí pidiendo que seleccione uno
            System.out.println("Por favor seleccione un producto de la tabla.");
        }
    }

    // Este es el método que VentaController llama para obtener el resultado
    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }
}