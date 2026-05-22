package app.Controllers;
import app.BDD.InventService;
import app.BDD.CategoriaService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import app.Models.Categoria;
import app.Models.Producto;

public class FormInventarioController {

    @FXML
    private TextField nombreField;
    @FXML
    private TextField descripcionField;
    @FXML
    private TextField precioField;
    @FXML
    private TextField stockField;
    @FXML
    private MenuButton categoriaMenuButton;
    @FXML
    private MenuButton estadoMenuButton;
    @FXML
    private StackPane mainContentForm;

    private int estadoSeleccionado;

    private boolean activo;

    private InventService inventario = new InventService();
    private InventarioController inventarioController = new InventarioController();
    private CategoriaService categoriaService = new CategoriaService();
    private Producto productoEdicion;

    @FXML
    public void initialize(){
        estadoSelection();
        cargarCategorias();
    }

    public void setProducto(Producto producto){
        this.productoEdicion = producto;

        if(productoEdicion != null){
            nombreField.setText(producto.getNombre());
            descripcionField.setText(producto.getDescripcion());
            precioField.setText(String.valueOf(producto.getPrecio()));
            stockField.setText(String.valueOf(producto.getStock()));
            estadoMenuButton.setText(String.valueOf(producto.getEstado()));
            categoriaMenuButton.setText(String.valueOf(producto.getId_categoria()));
        } else {
            limpiarCampos();
        }
    }
    
    public void agregarProducto(){
    
            String nombre = nombreField.getText();
            String descripcion = descripcionField.getText();
            float precio = Float.parseFloat(precioField.getText());
            int stock = Integer.parseInt(stockField.getText());
            String categoriaDescripcion = categoriaMenuButton.getText();

            // boolean estado = Boolean.parseBoolean(estadoSeleccionado);
            int categoria = inventario.obtenerIdCategoria(categoriaDescripcion);
            
            if(nombre.isEmpty() || descripcion.isEmpty() || categoria == -1){
                inventarioController.mostrarAlerta("Error", "Todos los campos deben estar completos");
                return;
            }

            if(estadoSeleccionado == 1){
                activo = true;
            } else {
                activo = false;
            }

            try {
                precio = Float.parseFloat(precioField.getText()); // Convertir a float
                stock = Integer.parseInt(stockField.getText()); // Convertir a int
                // estado = Boolean.parseBoolean(estadoMenuButton.getText()); // Convertir a boolean
            } catch (NumberFormatException e) {
                inventarioController.mostrarAlerta("Error", "Por favor, ingresa valores numéricos válidos en Precio y Stock, y un estado válido.");
                return;
            }

            // Llama al metodo para agregar el producto a la base de datos
            inventario.addProducto(nombre, descripcion, precio, stock, activo, categoria);
            inventarioController.mostrarAlerta("Éxito", "Producto agregado correctamente");
            limpiarCampos();
        
    }

    private void limpiarCampos(){
        nombreField.clear();
        descripcionField.clear();
        precioField.clear();
        stockField.clear();
    }

    private void estadoSelection() {
        for (MenuItem item : estadoMenuButton.getItems()) {
            item.setOnAction(event -> {
                String estado = item.getText();
                estadoMenuButton.setText(estado);

                if("Activo".equals(estado)){
                    estadoSeleccionado = 1;
                } else if("Inactivo".equals(estado)){
                    estadoSeleccionado = 0;
                }
            });
        }
    }

    private void cargarCategorias() {
        try {
            // Llama al servicio para obtener la lista de categorías
            List<Categoria> categorias = categoriaService.obtenerCategoriasDesdeBD();

            // Agrega cada categoría como un elemento del menú
            for (Categoria categoria : categorias) {
                MenuItem item = new MenuItem(categoria.getNombre()); // Asumiendo que el modelo `Categoria` tiene un método `getNombre`
                item.setOnAction(event -> seleccionarCategoria(categoria));
                categoriaMenuButton.getItems().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para manejar la selección de categoría
    private void seleccionarCategoria(Categoria categoria) {
        categoriaMenuButton.setText(categoria.getNombre());
    }

    @FXML
    public void cancelar(){
            try {
            Node inventarioView = FXMLLoader.load(getClass().getResource("/resources/InventarioView.fxml"));
            mainContentForm.getChildren().clear(); // Limpiar contenido actual
            mainContentForm.getChildren().add(inventarioView); // Cargar vista de inventario
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista de inventario");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}