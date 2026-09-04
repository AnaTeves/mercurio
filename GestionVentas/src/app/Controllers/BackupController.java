package app.Controllers;

import java.io.File;

import app.BDD.BackupService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class BackupController {

    @FXML private StackPane mainContentForm;
    @FXML private Label lblEstado;

    private final BackupService backupService = new BackupService();

    @FXML
    public void handleGenerarBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Copia de Seguridad");
        fileChooser.setInitialFileName(backupService.obtenerNombreSugerido());
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Copia de seguridad de SQL Server (*.bak)", "*.bak")
        );

        Stage stage = (Stage) mainContentForm.getScene().getWindow();
        File archivoDestino = fileChooser.showSaveDialog(stage);

        if (archivoDestino != null) {
            lblEstado.setText("Generando copia de seguridad...");
            lblEstado.setStyle("-fx-text-fill: #333;");

            boolean exito = backupService.crearBackup(archivoDestino);

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La copia de seguridad de DB_GESTIONVENTAS se creó correctamente.");
                lblEstado.setText("Último backup realizado con éxito.");
                lblEstado.setStyle("-fx-text-fill: green;");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al intentar crear el respaldo.");
                lblEstado.setText("Error al generar el backup.");
                lblEstado.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    public void handleRestaurarBackup() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Restauración");
        confirmacion.setHeaderText("¿Restaurar la base de datos DB_GESTIONVENTAS?");
        confirmacion.setContentText("Atención: Los datos actuales se reemplazarán por completo con el respaldo seleccionado.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo .bak de Respaldo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Copia de seguridad de SQL Server (*.bak)", "*.bak")
        );

        Stage stage = (Stage) mainContentForm.getScene().getWindow();
        File archivoOrigen = fileChooser.showOpenDialog(stage);

        if (archivoOrigen != null) {
            lblEstado.setText("Restaurando base de datos...");
            lblEstado.setStyle("-fx-text-fill: #333;");

            boolean exito = backupService.restaurarBackup(archivoOrigen);

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La base de datos DB_GESTIONVENTAS fue restaurada correctamente.");
                lblEstado.setText("Base de datos restaurada con éxito.");
                lblEstado.setStyle("-fx-text-fill: green;");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un error al restaurar. Verifique que SQL Server tenga permisos para leer el archivo.");
                lblEstado.setText("Error al restaurar el backup.");
                lblEstado.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}