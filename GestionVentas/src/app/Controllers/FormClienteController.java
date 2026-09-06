package app.Controllers;

import app.BDD.ClienteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class FormClienteController {

    @FXML private TextField nombreField;
    @FXML private TextField dniField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private StackPane mainContentForm;
    @FXML private Button btnGuardar;

    private final ClienteService clientes = new ClienteService();

    @FXML
public void agregarCliente() {
    String nombre = nombreField.getText().trim();
    String dni = dniField.getText().trim();
    String email = emailField.getText().trim();
    String telefono = telefonoField.getText().trim();

    // 1. Validar que los campos no estén vacíos
    if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
        mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Todos los campos deben estar completos.");
        return;
    }

    // 2. Validar si el DNI ya está registrado en la base de datos
    if (clientes.buscarPorDni(dni).isPresent()) {
        mostrarAlerta(Alert.AlertType.ERROR, "DNI Ya Registrado", "El DNI ingresado ya pertenece a otro cliente.");
        return; // Detiene el proceso de guardado
    }

    // 3. Insertar en BD si superó las validaciones
    clientes.addCliente(nombre, dni, email, telefono);
    limpiarCampos();

    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente agregado correctamente.");
    volverAClientes();
}

    @FXML
    public void cancelar() {
        volverAClientes();
    }

    private void limpiarCampos() {
        nombreField.clear();
        dniField.clear();
        emailField.clear();
        telefonoField.clear();
    }

    private void volverAClientes() {
        try {
            // Cambia la ruta según donde tengas tu FXML de lista de clientes
            Node clienteView = FXMLLoader.load(getClass().getResource("/ClienteView.fxml"));
            if (mainContentForm != null) {
                mainContentForm.getChildren().clear();
                mainContentForm.getChildren().add(clienteView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        if (mainContentForm != null && mainContentForm.getScene() != null) {
            alert.initOwner(mainContentForm.getScene().getWindow());
        }

        alert.showAndWait();
    }
}