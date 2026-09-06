package app.Controllers;

import java.io.IOException;
import app.BDD.UserService;
import app.Models.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/* Controlador que maneja el formulario para registrar un nuevo usuario */
public class FormUserController {

    @FXML private TextField nomYapeField;
    @FXML private TextField dniField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private MenuButton perfilMenuButton;
    @FXML private StackPane mainContentForm;

    private UserService users = new UserService();
    private UserController userController = new UserController();

    @FXML
    public void initialize() {
        perfilSelection();
    }
    
    /** Método que agrega un nuevo usuario */
    @FXML
    public void agregarUsuario() {
        /* Extraigo y limpio los datos del formulario */
        String nomYape = nomYapeField.getText().trim();
        String contraseña = passwordField.getText();    
        String repetirContraseña = repeatPasswordField.getText();
        String dni = dniField.getText().trim();
        String email = emailField.getText().trim();
        String perfilDescripcion = perfilMenuButton.getText();
        int idPerfil = users.obtenerIdPerfil(perfilDescripcion);

        /* Verifico que todos los campos estén completos */
        if (nomYape.isEmpty() || dni.isEmpty() || email.isEmpty() || contraseña.isEmpty() || repetirContraseña.isEmpty() || idPerfil == -1) {
            userController.mostrarAlerta("Error", "Todos los campos deben estar completos.");
            return;
        }

        /* Verifico que las contraseñas coincidan */
        if (contraseña.equals(repetirContraseña)) {
            
            /* Verifico si el DNI ya existe */
            if (users.dniExist(dni)) {
                userController.mostrarAlerta("Error", "El DNI ya está registrado.");
                passwordField.clear();
                repeatPasswordField.clear();
                return;
            } else {
                // Obtenemos el usuario activo desde SessionManager
                Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();

                if (usuarioActual == null) {
                    userController.mostrarAlerta("Error", "No hay una sesión activa válida.");
                    return;
                }

                int idUsuarioLogueado = usuarioActual.getIdUsuario();
                
                // Registramos el nuevo usuario y la auditoría
                users.addUser(nomYape, dni, email, idPerfil, contraseña, idUsuarioLogueado);
                
                limpiarCampos();
                userController.mostrarAlerta("Éxito", "Usuario agregado correctamente.");
            }
        } else {
            userController.mostrarAlerta("Error", "Las contraseñas no coinciden.");
            passwordField.clear();
            repeatPasswordField.clear();
        }
    }
    
    /* Método para seleccionar el perfil */
    private void perfilSelection() {
        for (MenuItem item : perfilMenuButton.getItems()) {
            item.setOnAction(event -> {
                String perfilSeleccionado = item.getText();
                perfilMenuButton.setText(perfilSeleccionado);
            });
        }
    }

    /* Método para limpiar los campos del formulario después de añadir un usuario */
    private void limpiarCampos() {
        nomYapeField.clear();
        dniField.clear();
        emailField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
        perfilMenuButton.setText("Seleccionar perfil");
    }

    /* Método que me devuelve a la vista de gestión de usuarios */
    @FXML
    public void cancelar() {
        try {
            Node usuarioview = FXMLLoader.load(getClass().getResource("/resources/UserView.fxml"));
            mainContentForm.getChildren().clear();
            mainContentForm.getChildren().add(usuarioview);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista de usuarios");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}