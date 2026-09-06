package app.Controllers;

import app.BDD.InventService;
import app.BDD.AuditoriaService;
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
import app.Models.Usuario;

public class FormInventarioController {

    @FXML private TextField nombreField;
    @FXML private TextField descripcionField;
    @FXML private TextField precioField;
    @FXML private TextField stockField;
    @FXML private MenuButton categoriaMenuButton;
    @FXML private MenuButton estadoMenuButton;
    @FXML private StackPane mainContentForm;

    private int estadoSeleccionado = -1; // -1 indica no seleccionado

    private final InventService inventario = new InventService();
    private final InventarioController inventarioController = new InventarioController();
    private final CategoriaService categoriaService = new CategoriaService();
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
            estadoMenuButton.setText(producto.getEstado() ? "Activo" : "Inactivo");
            estadoSeleccionado = producto.getEstado() ? 1 : 0;
            categoriaMenuButton.setText(String.valueOf(producto.getId_categoria()));
        } else {
            limpiarCampos();
        }
    }
    
    @FXML
    public void agregarProducto(){
        String nombre = nombreField.getText().trim();
        String descripcion = descripcionField.getText().trim();
        String precioStr = precioField.getText().trim();
        String stockStr = stockField.getText().trim();
        String categoriaDescripcion = categoriaMenuButton.getText();

        // 1. Validar que los campos de texto no estén vacíos
        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            inventarioController.mostrarAlerta("Error", "Todos los campos deben estar completos.");
            return;
        }

        // 2. Validar que la categoría sea válida
        int categoria = inventario.obtenerIdCategoria(categoriaDescripcion);
        if (categoria == -1) {
            inventarioController.mostrarAlerta("Error", "Debe seleccionar una categoría válida.");
            return;
        }

        // 3. Convertir de forma segura los valores numéricos
        float precio;
        int stock;
        try {
            precio = Float.parseFloat(precioStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            inventarioController.mostrarAlerta("Error", "Ingresa valores numéricos válidos en Precio y Stock.");
            return;
        }

        // 4. Determinar estado
        boolean activo = (estadoSeleccionado == 1);

        // 5. Guardar el producto en la BD
        inventario.addProducto(nombre, descripcion, precio, stock, activo, categoria);
        inventarioController.mostrarAlerta("Éxito", "Producto agregado correctamente.");
        limpiarCampos();

        // 6. Registrar en auditoría
        Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
        if (usuarioActual != null) {
            int idUsuario = usuarioActual.getIdUsuario();
            AuditoriaService.registrar(
                idUsuario,
                "Productos",
                "Agregar Producto",
                "Producto agregado: " + nombre
            );
        } else {
            System.out.println("Advertencia: No hay un usuario activo en la sesión.");
        }
    }

    private void limpiarCampos(){
        nombreField.clear();
        descripcionField.clear();
        precioField.clear();
        stockField.clear();
        estadoMenuButton.setText("Estado");
        categoriaMenuButton.setText("Categoría");
        estadoSeleccionado = -1;
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
            List<Categoria> categorias = categoriaService.obtenerCategoriasDesdeBD();

            for (Categoria categoria : categorias) {
                MenuItem item = new MenuItem(categoria.getNombre());
                item.setOnAction(event -> seleccionarCategoria(categoria));
                categoriaMenuButton.getItems().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seleccionarCategoria(Categoria categoria) {
        categoriaMenuButton.setText(categoria.getNombre());
    }

    @FXML
    public void cancelar(){
        try {
            Node inventarioView = FXMLLoader.load(getClass().getResource("/resources/InventarioView.fxml"));
            mainContentForm.getChildren().clear();
            mainContentForm.getChildren().add(inventarioView);
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