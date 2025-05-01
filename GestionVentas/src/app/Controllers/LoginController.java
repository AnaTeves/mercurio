package app.Controllers;
import app.BDD.UserService;
import app.Models.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

// Control que maneja el inicio de sesion 
public class LoginController {
    @FXML
    private TextField dniField;
    @FXML
    private PasswordField passwordField;
    private UserService userService = new UserService(); // Instanciamos la clase que maneja la interaccion con la base de datos de los usuarios

    // Método que maneja el evento del boton "ingresar"
    @FXML
    protected void handleLogin(ActionEvent event) {
        String dni = dniField.getText().trim(); // Extraigo el dni ingresado en el formulario
        String password = passwordField.getText().trim(); // Extraigo la contraseña ingresada en el formulario

        String perfilDesripcion = userService.validateUser(dni, password); // Llamo al metodo para validar el usuario
        if (perfilDesripcion != null) {
            SessionManager.setCurrentUser(perfilDesripcion); // Almacena el usuario que inicio sesion
            loadDashboard(perfilDesripcion); // Carga la vista correspondiente segun el tipo de usuario
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Usuario o contraseña invalidos");
            alert.show();
        }
    }

    // // Método para validar al usuario
    // public String authenticateUser(String username, String password) {
    //     if (username.equals("administrador") && password.equals("admin")) {
    //         return "Administrador";
    //     } else if (username.equals("gerente") && password.equals("gerente")) {
    //         return "Gerente";
    //     } else if (username.equals("empleado") && password.equals("empleado")) {
    //         return "Empleado";
    //     }
    //     return null;
    // }

    // Metodo que carga las distintas vistas segun el tipo de usuario
    private void loadDashboard(String userRole) {
        try {
            Stage stage = (Stage) dniField.getScene().getWindow(); // Obtengo la ventana actual
            Parent root;
            
            if(userRole.equals("Administrador")) {
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardAdmin.fxml"));
            } else if(userRole.equals("Gerente")) { 
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardGerente.fxml"));
            } else { 
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardEmpleado.fxml"));
            }

            // Muestra la nueva escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.setFullScreen(true);
            // Centramos la ventana al medio de la pantalla
            // Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            // stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            // stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo que maneja el evento del boton Cancelar
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) dniField.getScene().getWindow();
        stage.close();
    }
}