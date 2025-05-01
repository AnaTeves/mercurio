package app.Controllers;
// Clase que maneja la sesion del usuario
public class SessionManager {
    private static String currentUser;
    private static SessionManager instance;
    private String dniUsuario;

    // Metodo que guarda al usuario que inicio sesion
    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setDniUsuario(String dni) {
        this.dniUsuario = dni;
    }

    public String getDniUsuario() {
        return dniUsuario;
    }

    // Metodo que obtiene al usuario que inicio sesion
    public static String getCurrentUser(){
        return currentUser;
    }

    // Meotodo que limpia el usuario que inicio sesion
    public static void clearSession(){
        currentUser = null;
    }
}
