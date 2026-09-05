package app.BDD;

import app.Models.ArqueoCaja;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.text.SimpleDateFormat;
import app.Models.ArqueoCaja;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.text.SimpleDateFormat;

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
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dniUsuario);
            stmt.setDouble(2, montoInicial);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el ID de la caja que está actualmente abierta para un usuario.
     */
    public int obtenerIdCajaAbierta(String dniUsuario) {
        String query = "SELECT id_caja FROM Caja WHERE DNI = ? AND estado = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dniUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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

    public ObservableList<ArqueoCaja> obtenerHistorialArqueos() {
        ObservableList<ArqueoCaja> lista = FXCollections.observableArrayList();
        
        String query = "SELECT c.id_caja, u.nombreyape, c.DNI, " +
                       "c.fecha_apertura, c.fecha_cierre, " +
                       "c.monto_inicial, c.monto_final_sistema, c.monto_final_real, " +
                       "c.diferencia, c.estado " +
                       "FROM Caja c " +
                       "JOIN USUARIO u ON c.DNI = u.DNI " +
                       "ORDER BY c.id_caja DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_caja");
                String vendedor = rs.getString("nombreyape");
                String dni = rs.getString("DNI");
                
                Timestamp fApertura = rs.getTimestamp("fecha_apertura");
                Timestamp fCierre = rs.getTimestamp("fecha_cierre");
                String strApertura = (fApertura != null) ? sdf.format(fApertura) : "-";
                String strCierre = (fCierre != null) ? sdf.format(fCierre) : "En proceso";

                double inicial = rs.getDouble("monto_inicial");
                double sistema = rs.getDouble("monto_final_sistema");
                double real = rs.getDouble("monto_final_real");
                double dif = rs.getDouble("diferencia");
                boolean estadoBit = rs.getBoolean("estado");
                
                String estadoStr = estadoBit ? "ABIERTA" : "CERRADA";

                lista.add(new ArqueoCaja(id, vendedor, dni, strApertura, strCierre, inicial, sistema, real, dif, estadoStr));
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar historial de arqueos: " + e.getMessage());
        }

        return lista;
    }
}