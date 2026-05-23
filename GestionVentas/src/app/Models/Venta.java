package app.Models;

import java.util.List;

import java.sql.Timestamp;

public class Venta {
    private int idVenta;
    private Timestamp fechaVenta;
    private float totalVenta;
    private String dni_usuario;
    private String  dni_cliente;
    private int id_usuario;
    private int id_cliente;
    private int id_caja;
    private List<DetalleVenta> detallesVenta;  // Lista de detalles asociados a la venta

    // Constructor vacío
    public Venta() {}

    // Constructor completo
    public Venta(Timestamp fechaVenta, float totalVenta, String dni_usuario, String dni_cliente) {
        this.fechaVenta = fechaVenta;
        this.totalVenta = totalVenta;
        this.dni_usuario = dni_usuario;
        this.dni_cliente = dni_cliente;
    }

    public Venta(Timestamp fechaVenta, float totalVenta, int id_usuario, int id_cliente, int id_caja) {
        this.fechaVenta = fechaVenta;
        this.totalVenta = totalVenta;
        this.id_usuario = id_usuario;
        this.id_cliente = id_cliente;
        this.id_caja = id_caja;
    }

    public int getIdcaja(){
        return id_caja;
    }

    public void setIdcaja(int id_caja){
        this.id_caja = id_caja;
    }

    public int getIdusuario(){
        return id_usuario;
    }

    public void setIdusuario(int id_usuario){
        this.id_usuario = id_usuario;
    }

    public int getIdcliente(){
        return id_cliente;
    }

    public void setIdcliente(int id_cliente){
        this.id_cliente = id_cliente;
    }

    // Getters y Setters
    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Timestamp getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(Timestamp fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public float getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(float totalVenta) {
        this.totalVenta = totalVenta;
    }

    public String getDni_usuario() {
        return dni_usuario;
    }

    public void setDni_usuario(String dni_usuario) {
        this.dni_usuario = dni_usuario;
    }

    public String getDni_cliente() {
        return dni_cliente;
    }

    public void setDni_cliente(String dni_cliente) {
        this.dni_cliente = dni_cliente;
    }

    public List<DetalleVenta> getDetallesVenta() {
        return detallesVenta;
    }

    public void setDetallesVenta(List<DetalleVenta> detallesVenta) {
        this.detallesVenta = detallesVenta;
    }

    @Override
public String toString() {
    return "Venta{" +
            "fechaVenta=" + fechaVenta +
            ", totalVenta=" + totalVenta +
            ", usuario=" + getIdusuario() +
            ", cliente=" + getIdcliente() +
            '}';
}
}