package app.BDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CajaService {

    public Connection connection;

    /**
     * Verifica si el usuario actual ya tiene un turno (caja) abierto.
     * @param dniUsuario El DNI del empleado.
     * @return true si tiene una caja abierta (estado = 1), false en caso contrario.
     */
    public boolean isCajaAbierta(String dniUsuario) {
        String query = "SELECT COUNT(*) FROM Caja WHERE DNI = ? AND estado = 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, dniUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0; // Si hay 1 o más, la caja está abierta
                }
            }
        } catch (SQLException e) {
            System.err.println("Error BDD - verificar caja abierta: " + e.getMessage());
        }
        
        return false; 
    }

    /**
     * Inserta un nuevo registro de caja abierta en la base de datos.
     * @param dniUsuario El DNI del empleado que abre la caja.
     * @param montoInicial La cantidad de dinero con la que empieza el turno.
     * @return true si se insertó correctamente, false si hubo un error.
     */
    public boolean abrirCaja(String dniUsuario, double montoInicial) {
        String query = "INSERT INTO Caja (DNI, monto_inicial, estado) VALUES (?, ?, 1)";
        
        // 1. Nuevamente, usamos PreparedStatement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // 2. Seteamos ambos valores
            stmt.setString(1, dniUsuario);
            stmt.setDouble(2, montoInicial);
            
            // 3. Ejecutamos un executeUpdate() porque es un INSERT, no un SELECT
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0; 
            
        } catch (SQLException e) {
            System.err.println("Error BDD - abrir caja: " + e.getMessage());
            return false;
        }
    }


/**
     * Obtiene el ID de la caja que está actualmente abierta para un usuario.
     */
    public int obtenerIdCajaAbierta(String dniUsuario) {
        // OJO: Cambia 'id' por el nombre real de tu clave primaria en la tabla Caja (ej: id_caja)
        String query = "SELECT id_caja FROM Caja WHERE DNI = ? AND estado = 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, dniUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error BDD - obtener ID caja: " + e.getMessage());
        }
        return -1; // Retorna -1 si no encuentra caja abierta
    }

    /**
     * Obtiene el monto inicial con el que se abrió la caja.
     */
    public double obtenerMontoInicial(int idCaja) {
        String query = "SELECT monto_inicial FROM Caja WHERE id_caja = ?"; 
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, idCaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error BDD - obtener monto inicial: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Actualiza la caja para cerrarla, guardando fecha, monto final y diferencia.
     */
    public boolean cerrarCaja(double montoFinalSistema, double montoFinalReal, double diferencia, int idCaja) {
        // Usamos NOW() de SQL para que guarde la fecha y hora exacta del cierre. 
        // Cambia a estado = 0 (cerrada).
        String query = "UPDATE Caja SET fecha_cierre = GETDATE(), monto_final_sistema = ?, monto_final_real = ?, diferencia = ?, estado = 0 WHERE id_caja = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, montoFinalSistema);
            stmt.setDouble(2, montoFinalReal);
            stmt.setDouble(3, diferencia);
            stmt.setInt(4, idCaja);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error BDD - cerrar caja: " + e.getMessage());
            return false;
        }
    }

    public double obtenerVentasTotales(int idCaja) {
        // Sumamos el total de las ventas asociadas a esa caja/empleado
        // Asumiendo que tienes una tabla 'Ventas' con columna 'total' e 'id_caja'
        String query = "SELECT SUM(total_venta) FROM VENTA WHERE id_caja = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, idCaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1); // Retorna la suma de ventas
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular ventas: " + e.getMessage());
        }
        return 0.0;
    }
}