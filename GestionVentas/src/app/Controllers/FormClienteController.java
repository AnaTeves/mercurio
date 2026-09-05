package app.Controllers;
import java.io.IOException;

import app.BDD.ClienteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FormClienteController {
    @FXML
    private TextField nombreField;
    @FXML
    private TextField dniField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telefonoField;
    @FXML
    private StackPane mainContentForm;
    @FXML private Button btnGuardar;

    // Creamos una instancia de user service
    private ClienteService clientes = new ClienteService();
    // Creamos una instancia de user controller
    private ClienteController clienteController = new ClienteController();
    
    // Metodo que agrega un nuevo usuario
    @FXML
    public void agregarCliente() {
        String nombre = nombreField.getText();
        String dni = dniField.getText();
        String email = emailField.getText();
        String telefono = telefonoField.getText();

        // Verifica que todos los campos estén completos
        if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            clienteController.mostrarAlerta("Error", "Todos los campos deben estar completos.");
            return;
        }

        // Llama al método para insertar el usuario en la base de datos
        clientes.addCliente(nombre, dni, email, telefono);
        limpiarCampos();
        clienteController.mostrarAlerta("Éxito", "Usuario agregado correctamente.");
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    private void limpiarCampos() {
        nombreField.clear();
        dniField.clear();
        emailField.clear();
        telefonoField.clear();
    }

    @FXML
    public void volverVista(){
        clienteController.setView("/resources/ClientView.fxml");
    }

    @FXML
    public void cancelar(){
            try {
            Node clientesView = FXMLLoader.load(getClass().getResource("/resources/ClientesView.fxml"));
            mainContentForm.getChildren().clear(); // Limpiar contenido actual
            mainContentForm.getChildren().add(clientesView); // Cargar vista de categorías
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista de clientes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}