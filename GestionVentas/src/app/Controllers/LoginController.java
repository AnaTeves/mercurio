package app.Controllers;

import app.BDD.UserService;
import app.BDD.CajaService; // NUEVO
import app.Models.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog; // NUEVO
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.Optional; // NUEVO

// Control que maneja el inicio de sesion 
public class LoginController {
    @FXML
    private TextField dniField;
    @FXML
    private PasswordField passwordField;
    
    private UserService userService = new UserService(); 
    private SessionManager sessionManager = SessionManager.getInstance();
    private CajaService cajaService = new CajaService(); // NUEVO: Instanciamos el servicio de caja

    @FXML
    protected void handleLogin(ActionEvent event) {
        String dni = dniField.getText().trim();
        String password = passwordField.getText().trim();

        String perfilDesripcion = userService.validateUser(dni, password);
        
        if (perfilDesripcion != null) {
            // 1. Buscamos y guardamos el usuario en la sesión global
            Usuario dataUser = userService.searchUser(dni);
            sessionManager.setCurrentUser(dataUser); 

            // 2. NUEVO: Verificamos si es un Empleado para pedirle la caja. 
            // Si quieres que TODOS abran caja, simplemente quita este "if" y deja solo procesarAperturaDeCaja.
            if (perfilDesripcion.equals("Empleado")) {
                procesarAperturaDeCaja(dataUser, perfilDesripcion);
            } else {
                // Si es Admin o Gerente, entra directo
                loadDashboard(perfilDesripcion); 
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Usuario o contraseña invalidos");
            alert.show();
        }
    }

    // NUEVO: Método que maneja la lógica de la caja
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
                // Cambiamos comas por puntos por si el usuario escribe "1500,50"
                String montoTexto = result.get().replace(",", ".");
                double montoInicial = Double.parseDouble(montoTexto);

                // Guardamos en BD
                boolean exito = cajaService.abrirCaja(dni, montoInicial);

                if (exito) {
                    System.out.println("Caja abierta con éxito: $" + montoInicial);
                    loadDashboard(perfilDesripcion); // Cargamos la vista SOLO si tuvo éxito
                } else {
                    mostrarAlertaError("No se pudo registrar la caja en la base de datos.");
                }

            } catch (NumberFormatException e) {
                mostrarAlertaError("El monto ingresado no es válido. Debe ser un número.");
                // Volvemos a llamar al método para que lo intente de nuevo
                procesarAperturaDeCaja(usuarioLogueado, perfilDesripcion);
            }
        } else {
            // Si el usuario hace clic en "Cancelar", no lo dejamos entrar.
            System.out.println("Apertura de caja cancelada.");
            sessionManager.setCurrentUser(null); // Borramos la sesión
        }
    }

    // NUEVO: Método auxiliar para mostrar errores
    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Apertura");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Metodo que carga las distintas vistas segun el tipo de usuario
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

    // Metodo que maneja el evento del boton Cancelar
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) dniField.getScene().getWindow();
        stage.close();
    }
}