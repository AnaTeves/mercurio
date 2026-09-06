package app.Models;

public class Auditoria {
    private int idAuditoria;
    private String fechaHora;
    private String usuario;
    private String modulo;
    private String accion;
    private String detalle;

    public Auditoria(int idAuditoria, String fechaHora, String usuario, String modulo, String accion, String detalle) {
        this.idAuditoria = idAuditoria;
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
    }

    public int getIdAuditoria() { return idAuditoria; }
    public String getFechaHora() { return fechaHora; }
    public String getUsuario() { return usuario; }
    public String getModulo() { return modulo; }
    public String getAccion() { return accion; }
    public String getDetalle() { return detalle; }
}