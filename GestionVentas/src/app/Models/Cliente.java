package app.Models;

public class Cliente {
    private String nombre;
    private String dni;
    private String email;
    private String telefono;
    private int id_cliente;

    public Cliente(String nombre, String dni, String email, String telefono){
        this.nombre = nombre;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
    }

    public Cliente(int id, String nombre, String dni, String email){
        this.nombre = nombre;
        this.dni = dni;
        this.email = email;
        this.id_cliente = id;
    }

    public Cliente(){}

    public int getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public Cliente(String dni){
        this.dni = dni;
    }

    public void setTelefono(String telefono){
        this.telefono = telefono;
    }

    public String getTelefono(){
        return telefono;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
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

    @Override
    public String toString(){
        return getDni() + " - " + getNombre();
    }
}