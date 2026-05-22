package app.Controllers;

import app.Models.Usuario;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    private Usuario currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
    }

    public Usuario getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
    }
}