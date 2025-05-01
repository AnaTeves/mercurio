package app;
import app.BDD.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargamos el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/resources/FormUser.fxml"));

        DatabaseConnection.getConnection();

        // Creamos la escena
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

        primaryStage.initStyle(StageStyle.UNDECORATED); // Quita la barra superior del titulo
        primaryStage.setScene(scene);
        primaryStage.show();
        // primaryStage.setFullScreen(true);   
        // Centramos la ventana al medio de la pantalla
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }

    public static void main(String[] args) {
        launch(args);
        
    }
}