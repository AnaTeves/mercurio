package app.Controllers;

import app.BDD.UserService;
import app.BDD.CajaService;
import app.BDD.AuditoriaService;
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

// Control que maneja el inicio de sesión 
public class LoginController {
    @FXML private TextField dniField;
    @FXML private PasswordField passwordField;
    
    private UserService userService = new UserService(); 
    private SessionManager sessionManager = SessionManager.getInstance();
    private CajaService cajaService = new CajaService();

    @FXML 
    protected void handleLogin(ActionEvent event) {
        String dni = dniField.getText().trim();
        String password = passwordField.getText().trim();

        // 1. Validar campos vacíos antes de autenticar
        if (dni.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor, complete todos los campos para ingresar.");
            return;
        }

        // 2. Validar credenciales
        String perfilDescripcion = userService.validateUser(dni, password);
        
        if (perfilDescripcion != null) {
            Usuario dataUser = userService.searchUser(dni);

            sessionManager.setCurrentUser(dataUser); 

            // AUDITORÍA: Inicio de sesión exitoso
            AuditoriaService.registrar(
                dataUser.getIdUsuario(), 
                "Login", 
                "Inicio de Sesión", 
                "Ingreso exitoso al sistema (" + perfilDescripcion + ")"
            );

            // Redirección de vistas
            if (perfilDescripcion.equals("Empleado")) {
                procesarAperturaDeCaja(dataUser, perfilDescripcion);
            } else {
                loadDashboard(perfilDescripcion); 
            }

        } else {
            // 3. AUDITORÍA: Registrar intento fallido
            Usuario usuarioExistente = userService.searchUser(dni);
            int idUsuario = (usuarioExistente != null) ? usuarioExistente.getIdUsuario() : 0;
            String detalle = (usuarioExistente != null) 
                ? "Contraseña incorrecta (DNI: " + dni + ")" 
                : "DNI no registrado (DNI: " + dni + ")";

            try {
                AuditoriaService.registrar(idUsuario, "Login", "Intento Fallido", detalle);
            } catch (Exception e) {
                System.err.println("Error al registrar auditoría de intento fallido: " + e.getMessage());
            }

            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "Usuario o contraseña inválidos.");
        }
    }

    // Método auxiliar para alertas reutilizables
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método que maneja la lógica de la caja
    private void procesarAperturaDeCaja(Usuario usuarioLogueado, String perfilDescripcion) {
        String dni = usuarioLogueado.getDni();

        if (cajaService.isCajaAbierta(dni)) {
            System.out.println("La caja ya estaba abierta. Ingresando al sistema...");
            loadDashboard(perfilDescripcion);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Apertura de Caja");
        dialog.setHeaderText("¡Bienvenido/a, " + usuarioLogueado.getNomYape() + "!");
        dialog.setContentText("Ingrese el dinero inicial en caja (Cambio): $");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                String montoTexto = result.get().replace(",", ".");
                double montoInicial = Double.parseDouble(montoTexto);

                boolean exito = cajaService.abrirCaja(dni, montoInicial);

                if (exito) {
                    AuditoriaService.registrar(
                        usuarioLogueado.getIdUsuario(),
                        "Caja",
                        "Apertura de Caja",
                        "Apertura de caja con monto inicial de $" + montoInicial
                    );

                    System.out.println("Caja abierta con éxito: $" + montoInicial);
                    loadDashboard(perfilDescripcion);
                } else {
                    mostrarAlertaError("No se pudo registrar la caja en la base de datos.");
                }

            } catch (NumberFormatException e) {
                mostrarAlertaError("El monto ingresado no es válido. Debe ser un número.");
                procesarAperturaDeCaja(usuarioLogueado, perfilDescripcion);
            }
        } else {
            System.out.println("Apertura de caja cancelada.");
            sessionManager.setCurrentUser(null);
        }
    }

    private void mostrarAlertaError(String mensaje) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error de Apertura", mensaje);
    }

    private void loadDashboard(String userRole) {
        try {
            Stage stage = (Stage) dniField.getScene().getWindow(); 
            Parent root;
            
            if (userRole.equals("Administrador")) {
                root = FXMLLoader.load(getClass().getResource("/resources/mainViews/DashboardAdmin.fxml"));
            } else if (userRole.equals("Gerente")) { 
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