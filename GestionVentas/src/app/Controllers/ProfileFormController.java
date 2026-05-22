package app.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import app.Models.Usuario;

public class ProfileFormController {
    @FXML
    TextField nameField, dnField, emField, passworField;
    
    private Usuario usuario;

    public void initialize(Usuario usuario) {
        this.usuario = usuario;
        cargarDatos();
    }

    private void cargarDatos() {
        nameField.setText(usuario.getNomYape());
        dnField.setText(usuario.getDni());
        emField.setText(usuario.getEmail());
        passworField.setText(usuario.getContraseña());
    }

}
