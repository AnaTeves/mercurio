package app.Models;

public class Usuario {
    private String nomYape;
    private String dni;
    private String email;
    private int idPerfil;
    private String estado;
    private String contraseña;
    private String tipoPerfil;

    public Usuario(String nomYape, String dni, String email, int idPerfil, String estado, String contraseña){
        this.nomYape = nomYape;
        this.dni = dni;
        this.email = email;
        this.idPerfil = idPerfil;
        this.estado = estado;
        this.contraseña = contraseña;
    }

    // Sobre carga de constructor
    public Usuario(String nomYape, String dni, String email, int idPerfil, String estado){
        this.nomYape = nomYape;
        this.dni = dni;
        this.email = email;
        this.idPerfil = idPerfil;
        this.estado = estado;
    }

    public Usuario(String nomYape, String dni, String email, String estado){
        this.nomYape = nomYape;
        this.dni = dni;
        this.email = email;
        this.estado = estado;
    }

    public Usuario(String nombre, String dni){
        this.nomYape = nombre;
        this.dni = dni;
    }

    public void setContraseña(String contraseña){
        this.contraseña = contraseña;
    }

    public String getContraseña(){
        return contraseña;
    }

    public void setNombre(String nomYape){
        this.nomYape = nomYape;
    }

    public String getNomYape() {
        return nomYape;
    }

    public void setDni(String dni){
        this.dni = dni;
    }

    public String getDni() {
        return dni;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }  

    public void setIdPerfil(int idPerfil){
        this.idPerfil = idPerfil;
    }

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setEstado(String estado){
        this.estado = estado;
    }

    public String getEstado(){
        return estado;
    }

    public String getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }
}