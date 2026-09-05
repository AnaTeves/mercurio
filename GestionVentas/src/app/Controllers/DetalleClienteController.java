package app.Controllers;

import app.BDD.ClienteService;
import app.Models.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class DetalleClienteController extends ComunesController {

    @FXML private TextField txtId;
    @FXML private TextField nombreField;
    @FXML private TextField dniField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;

    @FXML private Button btnModificar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;

    private Cliente cliente;
    private final ClienteService clienteService = new ClienteService();
    private boolean modoEdicion = false;

    /**
     * Recibe el cliente desde la vista anterior (ej. ClientesView)
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        cargarDatos();
    }

    private void cargarDatos() {
        if (cliente == null) return;

        if (txtId != null) {
            txtId.setText(String.valueOf(cliente.getId())); 
        }
        nombreField.setText(cliente.getNombre());    
        dniField.setText(cliente.getDni());    
        emailField.setText(cliente.getEmail());
        telefonoField.setText(cliente.getTelefono());

        // Asegurar que inicie desactivado
        setCamposHabilitados(false);
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
        // Validaciones de campos obligatorios
        String nombre = nombreField.getText().trim();
        String dni = dniField.getText().trim();
        String email = emailField.getText().trim();
        String telefono = telefonoField.getText().trim();

        if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Todos los campos son obligatorios.");
            return;
        }

        // Actualizar los datos del objeto cliente
        cliente.setNombre(nombre);     
        cliente.setDni(dni);      
        cliente.setEmail(email);
        cliente.setTelefono(telefono);

        // Guardar en la Base de Datos
        boolean exito = clienteService.actualizarCliente(cliente);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente actualizado correctamente en la base de datos.");
            setCamposHabilitados(false);
            btnModificar.setText("Modificar");
            btnModificar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
            btnCancelar.setVisible(false);
            modoEdicion = false;
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error BD", "No se pudo actualizar el cliente.");
        }
    }

    @FXML
    private void handleCancelar() {
        // Restaurar los datos originales del cliente en pantalla
        cargarDatos();
        setCamposHabilitados(false);
        btnModificar.setText("Modificar");
        btnModificar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        btnCancelar.setVisible(false);
        modoEdicion = false;
    }

    private void setCamposHabilitados(boolean habilitar) {
        // txtId permanece siempre inhabilitado (Primary Key)
        if (txtId != null) txtId.setDisable(true);
        nombreField.setDisable(!habilitar);
        dniField.setDisable(!habilitar);
        emailField.setDisable(!habilitar);
        telefonoField.setDisable(!habilitar);
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
        setView("/resources/ClientesView.fxml");
    }
}