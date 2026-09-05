package app.Models;

public class ArqueoCaja {

    private int idCaja;
    private String nombreVendedor;
    private String dniVendedor;
    private String fechaApertura;
    private String fechaCierre;
    private double montoInicial;
    private double montoSistema;
    private double montoReal;
    private double diferencia;
    private String estado;

    public ArqueoCaja(int idCaja, String nombreVendedor, String dniVendedor, 
                      String fechaApertura, String fechaCierre, double montoInicial, 
                      double montoSistema, double montoReal, double diferencia, String estado) {
        this.idCaja = idCaja;
        this.nombreVendedor = nombreVendedor;
        this.dniVendedor = dniVendedor;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = fechaCierre;
        this.montoInicial = montoInicial;
        this.montoSistema = montoSistema;
        this.montoReal = montoReal;
        this.diferencia = diferencia;
        this.estado = estado;
    }

    // Getters
    public int getIdCaja() { return idCaja; }
    public String getNombreVendedor() { return nombreVendedor; }
    public String getDniVendedor() { return dniVendedor; }
    public String getFechaApertura() { return fechaApertura; }
    public String getFechaCierre() { return fechaCierre; }
    public double getMontoInicial() { return montoInicial; }
    public double getMontoSistema() { return montoSistema; }
    public double getMontoReal() { return montoReal; }
    public double getDiferencia() { return diferencia; }
    public String getEstado() { return estado; }
}