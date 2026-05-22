package app.Controllers;

import app.BDD.CajaService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AperturaCajaController {

    @FXML private TextField txtMontoInicial;
    
    private CajaService cajaService = new CajaService();
    private String dniEmpleado;
    
    // Esta variable le avisará al EmpleadoController si todo salió bien
    private boolean cajaAbiertaExitosamente = false;

    // Recibimos el DNI desde el EmpleadoController antes de mostrar la ventana
    public void inicializarDatos(String dni) {
        this.dniEmpleado = dni;
    }

    @FXML
    private void ejecutarApertura() {
        try {
            // Reemplazamos comas por puntos por si el usuario escribe "1000,50"
            double montoInicial = Double.parseDouble(txtMontoInicial.getText().replace(",", "."));
            
            if (montoInicial < 0) {
                mostrarAlerta("Monto Inválido", "El monto inicial no puede ser negativo.", Alert.AlertType.ERROR);
                return;
            }

            // Llamamos a tu servicio de base de datos
            boolean exito = cajaService.abrirCaja(dniEmpleado, montoInicial);

            if (exito) {
                mostrarAlerta("Apertura Exitosa", "Caja abierta con $" + montoInicial + ". Ya puedes comenzar a operar.", Alert.AlertType.INFORMATION);
                cajaAbiertaExitosamente = true;
                
                // Cerramos esta ventana flotante
                Stage stage = (Stage) txtMontoInicial.getScene().getWindow();
                stage.close();
            } else {
                mostrarAlerta("Error BD", "Hubo un problema al abrir la caja en la base de datos.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Por favor ingrese un número válido.", Alert.AlertType.ERROR);
        }
    }

    // Método que el EmpleadoController usa para saber si debe desbloquear el menú
    public boolean isCajaAbierta() {
        return cajaAbiertaExitosamente;
    }

    // Método de utilidad para no repetir código de alertas
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}