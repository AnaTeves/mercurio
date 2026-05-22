package app.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
// Controlador que maneja metodos comunes entre los perfiles
public class ComunesController {
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private StackPane mainContent;
    @FXML
    private Button logoutButton;
    // Instanciamos el controlador para cerrar la sesion
    private LogoutController logoutController = new LogoutController();

    @FXML
    public void initialize(){
        //if (logoutButton != null) {
          //  logoutButton.setOnAction(e -> handleLogout());
        //}
    }

    // Metodo que carga una vista en el contenedor central
    @FXML
    public void setView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Mostramos mensaje de error en caso de que no se pueda cargar la vista
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista");
            alert.setContentText("Verifica que la ruta del archivo FXML sea correcta.");
            alert.showAndWait();
        }
    }

    // Método que delega la lógica al LogoutController
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText("¿Estás seguro de que deseas cerrar sesión?");
        alert.setContentText("Selecciona una opción:");

        // Esperar la respuesta del usuario
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Cierra la ventana actual
                Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                logoutController.handleLogout(currentStage);
            }
        });
    }
}
