package app.Controllers;

import app.BDD.CajaService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CierreCajaController {

    @FXML 
    private TextField txtMontoFinal;
    
    private CajaService cajaService = new CajaService();
    private String dniEmpleado;
    private boolean cajaCerradaExitosamente = false;

    /**
     * Se llama desde el controlador anterior para pasar el DNI del usuario logueado.
     */
    public void inicializarDatos(String dni) {
        this.dniEmpleado = dni;
    }

    @FXML
    private void ejecutarCierre() {
        try {
            // 1. Validar y obtener el Monto Real (lo que el cajero cuenta físicamente)
            String inputMonto = txtMontoFinal.getText().replace(",", ".");
            if (inputMonto.isEmpty()) {
                mostrarAlerta("Campo vacío", "Por favor, ingrese el monto final en caja.", Alert.AlertType.WARNING);
                return;
            }
            
            double montoFisicoReal = Double.parseDouble(inputMonto);
            
            if (montoFisicoReal < 0) {
                mostrarAlerta("Monto Inválido", "El monto no puede ser negativo.", Alert.AlertType.ERROR);
                return;
            }

            // 2. Obtener el ID de la caja abierta
            int idCaja = cajaService.obtenerIdCajaAbierta(dniEmpleado);
            if (idCaja == -1) {
                mostrarAlerta("Error", "No se encontró una caja abierta para este usuario.", Alert.AlertType.ERROR);
                return;
            }

            // 3. Calcular lo que el SISTEMA espera que haya
            // Monto Esperado = Monto Inicial + Ventas registradas
            double montoInicial = cajaService.obtenerMontoInicial(idCaja);
            double ventasDelTurno = cajaService.obtenerVentasTotales(idCaja); 
            double montoEsperadoSistema = montoInicial + ventasDelTurno;

            // 4. Calcular la diferencia
            double diferencia = montoFisicoReal - montoEsperadoSistema;

            // 5. Ejecutar el cierre en la Base de Datos
            boolean exito = cajaService.cerrarCaja(montoEsperadoSistema, montoFisicoReal, diferencia, idCaja);

            if (exito) {
                cajaCerradaExitosamente = true;
                
                // Formatear mensaje de resumen
                String resumen = String.format(
                    "Cierre procesado correctamente.\n\n" +
                    "Monto Inicial: $%.2f\n" +
                    "Ventas del Turno: $%.2f\n" +
                    "---------------------------\n" +
                    "Esperado en Sistema: $%.2f\n" +
                    "Contado Físicamente: $%.2f\n" +
                    "---------------------------\n" +
                    "Diferencia: $%.2f (%s)",
                    montoInicial, ventasDelTurno, montoEsperadoSistema, montoFisicoReal, 
                    diferencia, (diferencia >= 0 ? "Sobrante" : "Faltante")
                );

                mostrarAlerta("Cierre Exitoso", resumen, Alert.AlertType.INFORMATION);
                
                // Cerrar la ventana actual
                Stage stage = (Stage) txtMontoFinal.getScene().getWindow();
                stage.close();
            } else {
                mostrarAlerta("Error BD", "No se pudo actualizar el registro de cierre en la base de datos.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Por favor ingrese un número válido (ej: 1500.50).", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error inesperado", "Ocurrió un error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public boolean isCajaCerrada() {
        return cajaCerradaExitosamente;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}