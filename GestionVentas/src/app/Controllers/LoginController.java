package app.Controllers;

import app.BDD.UserService;
import app.BDD.CajaService;
import app.BDD.AuditoriaService; // AUDITORÍA: Se agrega el servicio
import app.Models.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.Optional;

// Control que maneja el inicio de sesion 
public class LoginController {
    @FXML
    private TextField dniField;
    @FXML
    private PasswordField passwordField;
    
    private UserService userService = new UserService(); 
    private SessionManager sessionManager = SessionManager.getInstance();
    private CajaService cajaService = new CajaService();

    @FXML protected void handleLogin(ActionEvent event) {
    String dni = dniField.getText().trim();
    String password = passwordField.getText().trim();

    String perfilDesripcion = userService.validateUser(dni, password);
    
    if (perfilDesripcion != null) {
        Usuario dataUser = userService.searchUser(dni);

        // DIAGNÓSTICO: Revisa tu consola. Si imprime 0, el problema está en searchUser()
        System.out.println("ID Usuario obtenido: " + dataUser.getIdUsuario());

        sessionManager.setCurrentUser(dataUser); 

        // AUDITORÍA: Registrar ANTES de cambiar de ventana o abrir caja
        AuditoriaService.registrar(
            dataUser.getIdUsuario(), 
            "Login", 
            "Inicio de Sesión", 
            "Ingreso al sistema (" + perfilDesripcion + ")"
        );

        // Redirección de vistas después de guardar la auditoría
        if (perfilDesripcion.equals("Empleado")) {
            procesarAperturaDeCaja(dataUser, perfilDesripcion);
        } else {
            loadDashboard(perfilDesripcion); 
        }

    } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Usuario o contraseña inválidos");
        alert.show();
    }
}

    // Método que maneja la lógica de la caja
    private void procesarAperturaDeCaja(Usuario usuarioLogueado, String perfilDesripcion) {
        String dni = usuarioLogueado.getDni();

        // A. Verificamos si ya tiene una caja abierta
        if (cajaService.isCajaAbierta(dni)) {
            System.out.println("La caja ya estaba abierta. Ingresando al sistema...");
            loadDashboard(perfilDesripcion);
            return;
        }

        // B. Pedimos el monto inicial
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Apertura de Caja");
        dialog.setHeaderText("¡Bienvenido/a, " + usuarioLogueado.getNomYape() + "!");
        dialog.setContentText("Ingrese el dinero inicial en caja (Cambio): $");

        Optional<String> result = dialog.showAndWait();

        // C. Evaluamos qué ingresó
        if (result.isPresent()) {
            try {
                String montoTexto = result.get().replace(",", ".");
                double montoInicial = Double.parseDouble(montoTexto);

                // Guardamos en BD
                boolean exito = cajaService.abrirCaja(dni, montoInicial);

                if (exito) {
                    // AUDITORÍA 2: Registrar la apertura de caja
                    AuditoriaService.registrar(
                        usuarioLogueado.getIdUsuario(),
                        "Caja",
                        "Apertura de Caja",
                        "Apertura de caja con monto inicial de $" + montoInicial
                    );

                    System.out.println("Caja abierta con éxito: $" + montoInicial);
                    loadDashboard(perfilDesripcion);
                } else {
                    mostrarAlertaError("No se pudo registrar la caja en la base de datos.");
                }

            } catch (NumberFormatException e) {
                mostrarAlertaError("El monto ingresado no es válido. Debe ser un número.");
                procesarAperturaDeCaja(usuarioLogueado, perfilDesripcion);
            }
        } else {
            System.out.println("Apertura de caja cancelada.");
            sessionManager.setCurrentUser(null);
        }
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Apertura");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void loadDashboard(String userRole) {
        try {
            Stage stage = (Stage) dniField.getScene().getWindow(); 
            Parent root;
            
            if(userRole.equals("Administrador")) {
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardAdmin.fxml"));
            } else if(userRole.equals("Gerente")) { 
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardGerente.fxml"));
            } else { 
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardEmpleado.fxml"));
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) dniField.getScene().getWindow();
        stage.close();
    }
}