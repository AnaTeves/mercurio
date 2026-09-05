package app.Controllers;

import java.io.IOException;

import app.BDD.InventService;
import app.Models.Producto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DetalleProductoController extends ComunesController {

    @FXML private TextField txtId;
    @FXML private TextField nombreField;
    @FXML private TextArea descripcionField;
    @FXML private TextField precioField;
    @FXML private TextField stockField;
    @FXML private TextField categoriaField;
    @FXML private CheckBox chkEstado;

    @FXML private Button btnModificar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;

    private Producto producto;
    private final InventService inventService = new InventService();
    private boolean modoEdicion = false;

    /**
     * Recibe el producto desde la vista anterior (ej. InventarioView)
     */
    public void setProducto(Producto producto) {
        this.producto = producto;
        cargarDatos();
    }

    private void cargarDatos() {
        if (producto == null) return;

        txtId.setText(String.valueOf(producto.getId()));
        nombreField.setText(producto.getNombre());
        descripcionField.setText(producto.getDescripcion());
        precioField.setText(String.valueOf(producto.getPrecio()));
        stockField.setText(String.valueOf(producto.getStock()));
        categoriaField.setText(String.valueOf(producto.getId_categoria()));
        chkEstado.setSelected(producto.getEstado());
    }

    /**
     * Alterna entre habilitar edición y guardar los datos
     */
    @FXML
    private void handleModificarGuardar() {
        if (!modoEdicion) {
            // Entrar a Modo Edición
            setCamposHabilitados(true);
            btnModificar.setText("Guardar");
            btnModificar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
            btnCancelar.setVisible(true);
            modoEdicion = true;
        } else {
            // Intentar Guardar Cambios
            guardarCambios();
        }
    }

    private void guardarCambios() {
        try {
            // Validaciones de formato
            String nombre = nombreField.getText().trim();
            String descripcion = descripcionField.getText().trim();

            if (nombre.isEmpty() || descripcion.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "El nombre y la descripción son obligatorios.");
                return;
            }

            float precio = Float.parseFloat(precioField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            boolean estado = chkEstado.isSelected();

            int idCategoria;
            try {
                idCategoria = Integer.parseInt(categoriaField.getText().trim());
            } catch (NumberFormatException e) {
                // Si ingresó el nombre de la categoría en lugar del ID
                idCategoria = inventService.obtenerIdCategoria(categoriaField.getText().trim());
                if (idCategoria == -1) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Categoría No Válida", "Ingresa un ID de categoría válido o una categoría existente.");
                    return;
                }
            }

            // Actualizar el objeto producto
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setEstado(estado);
            producto.setId_categoria(idCategoria);

            // Guardar en la Base de Datos
            boolean exito = inventService.actualizarProducto(producto);

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente en la base de datos.");
                setCamposHabilitados(false);
                btnModificar.setText("Modificar");
                btnModificar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
                btnCancelar.setVisible(false);
                modoEdicion = false;
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error BD", "No se pudo actualizar el producto.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Incorrecto", "El Precio y el Stock deben ser números válidos.");
        }
    }

    @FXML
    private void handleCancelar() {
        // Restaurar los datos originales del objeto en pantalla
        cargarDatos();
        setCamposHabilitados(false);
        btnModificar.setText("Modificar");
        btnModificar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        btnCancelar.setVisible(false);
        modoEdicion = false;
    }

    private void setCamposHabilitados(boolean habilitar) {
        // txtId permanece siempre inhabilitado (Primary Key)
        nombreField.setDisable(!habilitar);
        descripcionField.setDisable(!habilitar);
        precioField.setDisable(!habilitar);
        stockField.setDisable(!habilitar);
        categoriaField.setDisable(!habilitar);
        chkEstado.setDisable(!habilitar);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void handleVolver() {
        setView("/resources/InventarioView.fxml");
    }
}