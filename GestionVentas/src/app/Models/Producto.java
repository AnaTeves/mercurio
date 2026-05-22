package app.Models;

public class Producto {
    private String nombre;
    private String descripcion;
    private float precio;
    private int stock;
    private Boolean estado;
    private int id_categoria;
    private int id;

    public Producto(){}

    public Producto(String nombre, String descripcion, float precio, int stock, Boolean estado, int id_categoria){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.estado = estado;
        this.id_categoria = id_categoria;
    }

    public Producto(int id, String nombre, String descripcion, float precio, int stock, Boolean estado, int id_categoria){
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.estado = estado;
        this.id_categoria = id_categoria;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId_categoria() {
        return id_categoria;
    }

    public void setId_categoria(int id_categoria) {
        this.id_categoria = id_categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public float getPrecio() {
        return precio;
    }  

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
    public Boolean getEstado() {
        return estado;
    }

    @Override
    public String toString(){
        return getNombre();
    }
}