package app.Controllers;

import app.BDD.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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

        // Validar que no haya campos vacíos
        if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Todos los campos deben estar completos.");
            return;
        }

        // Insertar en BD
        clientes.addCliente(nombre, dni, email, telefono);
        limpiarCampos();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente agregado correctamente.");
        
        // Cerrar la ventana modal al finalizar
        cerrarVentana();
    }

    @FXML
    public void cancelar() {
        // En una ventana modal, 'cancelar' o 'volver' simplemente debe cerrar el Stage actual
        cerrarVentana();
    }

    private void limpiarCampos() {
        nombreField.clear();
        dniField.clear();
        emailField.clear();
        telefonoField.clear();
    }

    private void cerrarVentana() {
        if (btnGuardar != null && btnGuardar.getScene() != null) {
            Stage stage = (Stage) btnGuardar.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Asigna la ventana actual como dueña explícita para evitar bloqueos
        if (mainContentForm != null && mainContentForm.getScene() != null) {
            alert.initOwner(mainContentForm.getScene().getWindow());
        }

        alert.showAndWait();
    }
}