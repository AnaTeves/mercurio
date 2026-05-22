package app.Models;

public class DetalleVenta {
    private String nombre;
    private int cantidad;
    private float precioUnitario;
    private int id_venta;
    private int id_producto;
    private float subtotal;  // precioUnitario * cantidad
    // private float total;

    // private final FloatProperty precio_unitario = new SimpleFloatProperty();


    public DetalleVenta(String nombre, int cantidad, float precioUnitario, int id_producto){
        this.nombre = nombre; 
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.id_producto = id_producto;                          
    }

    public DetalleVenta(int cantidad, float precioUnitario, int id_producto){
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.id_producto = id_producto;
    }

    // Constructor completo
    public DetalleVenta(int cantidad, float precioUnitario, int id_venta, int id_producto) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.id_venta = id_venta;
        this.id_producto = id_producto; 
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    // public FloatProperty precioUnitarioProperty(){
    //     return precio_unitario;
    // }

    // public Float getPrecioUnitario(){
    //     return precio_unitario.get();
    // }

    public void setPrecioUnitario(float precioUnitario){
        this.precioUnitario = precioUnitario;
    }
    
    public int getId_producto() {
        return id_producto;
    }

    public void setProducto(int id_producto) {
        this.id_producto = id_producto;
    }

    public int getIdVenta() {
        return id_venta;
    }

    public void setVenta(int id_venta) {
        this.id_venta = id_venta;
    }

    public float getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecio(float precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void setProducto(float precio) {
        this.precioUnitario = precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public float getSubtotal() {
        return cantidad * precioUnitario;
    }
}