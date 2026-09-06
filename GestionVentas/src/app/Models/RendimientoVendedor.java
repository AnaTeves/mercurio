package app.Models;

public class RendimientoVendedor {
    private String vendedor;
    private String dni;
    private int cantidadVentas;
    private double totalRecaudado;
    private int totalArqueos;
    private int cierresConDiferencia;

    public RendimientoVendedor(String vendedor, String dni, int cantidadVentas, double totalRecaudado, int totalArqueos, int cierresConDiferencia) {
        this.vendedor = vendedor;
        this.dni = dni;
        this.cantidadVentas = cantidadVentas;
        this.totalRecaudado = totalRecaudado;
        this.totalArqueos = totalArqueos;
        this.cierresConDiferencia = cierresConDiferencia;
    }

    public String getVendedor() { return vendedor; }
    public String getDni() { return dni; }
    public int getCantidadVentas() { return cantidadVentas; }
    public double getTotalRecaudado() { return totalRecaudado; }
    public int getTotalArqueos() { return totalArqueos; }
    public int getCierresConDiferencia() { return cierresConDiferencia; }
}