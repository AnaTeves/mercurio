package app.Controllers;
import java.io.IOException;
import app.BDD.UserService;
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
    @FXML
    private TextField nomYapeField;
    @FXML
    private TextField dniField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField repeatPasswordField;
    @FXML
    private MenuButton perfilMenuButton;
    @FXML
    private StackPane mainContentForm;
    private UserService users = new UserService(); // Creamos una instancia la cual nos permite interactuar con la base de datos del usuario
    private UserController userController = new UserController(); // Creamos una instancia del controlador del usuario

    @FXML
    public void initialize() {
        perfilSelection(); // Llamamos al metodo que muestra la seleccion del tipo de perfil a almacenar
    }
    
    /** Metodo que agrega un nuevo usuario */
    @FXML
    public void agregarUsuario() {
        /* Extraigo los datos del formulario */
        String nomYape = nomYapeField.getText();
        String contraseña = passwordField.getText();    
        String repetirContraseña = repeatPasswordField.getText();
        String dni = dniField.getText();
        String email = emailField.getText();
        String perfilDescripcion = perfilMenuButton.getText();
        int idPerfil = users.obtenerIdPerfil(perfilDescripcion);
        /* Verifico que todos los campos estén completos */
        if (nomYape.isEmpty() || dni.isEmpty() || email.isEmpty() || contraseña.isEmpty() || repetirContraseña.isEmpty() || idPerfil == -1) {
            userController.mostrarAlerta("Error", "Todos los campos deben estar completos.");
            return;
        }
        /* Verifico que las contraseñas ingresadas en el formulario sean iguales */
        if(contraseña.equals(repetirContraseña)){ // En caso de que las contraseñas coincidan, evualuamos el dni
            /* Verifico si el DNI ya existe en la base de datos */
            boolean existeDNI = users.dniExist(dni);
            if (existeDNI){ // Si existe me lanza un mensaje de error
                userController.mostrarAlerta("Error", "El DNI ya esta registrado.");
                passwordField.clear(); // Elimino los campos de las contraseñas ingresadas
                repeatPasswordField.clear();
                return;
            } else { // Añade al usuario a la base de datos
                users.addUser(nomYape, dni, email, idPerfil, contraseña); // Llama al método que inserta el usuario en la base de datos
                limpiarCampos(); // Limpiamos los campos del formulario
                userController.mostrarAlerta("Éxito", "Usuario agregado correctamente."); // Emite una alerta de exito
            }
        } else { // Si las contraseñas no coinciden lanza un mensaje de error
            userController.mostrarAlerta("Error", "Las contraseñas no coinciden.");
            passwordField.clear(); // Elimino los campos de las contraseñas ingresadas
            repeatPasswordField.clear();
        }
    }
    
    /* Metodo para seleccionar el perfil */
    private void perfilSelection() {
        for (MenuItem item : perfilMenuButton.getItems()) {
            item.setOnAction(event -> {
                String perfilSeleccionado = item.getText(); // Extraigo los nombres de los perfiles
                perfilMenuButton.setText(perfilSeleccionado); // Muestro el perfil
            });
        }
    }

    /* Metodo para limpiar los campos del formulario despues de añadir un usuario */
    private void limpiarCampos() {
        nomYapeField.clear();
        dniField.clear();
        emailField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
    }

    /* Metodo que me devuelve a la vista de gestion de usuarios */
    @FXML
    public void cancelar(){
            try {
            Node usuarioview = FXMLLoader.load(getClass().getResource("/resources/UserView.fxml"));
            mainContentForm.getChildren().clear(); // Limpiar contenido actual
            mainContentForm.getChildren().add(usuarioview); // Cargar vista de usuarios
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