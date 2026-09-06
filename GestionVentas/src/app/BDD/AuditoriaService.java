package app.BDD;

import app.Models.Auditoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;

public class AuditoriaService {

    public static void registrar(int idUsuario, String modulo, String accion, String detalle) {
        String query = "INSERT INTO AUDITORIA (id_usuario, modulo, accion, detalle) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, modulo);
            stmt.setString(3, accion);
            stmt.setString(4, detalle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Auditoria> obtenerRegistros(LocalDate desde, LocalDate hasta, String modulo) {
        ObservableList<Auditoria> lista = FXCollections.observableArrayList();
        StringBuilder query = new StringBuilder("""
            SELECT a.id_auditoria, CONVERT(VARCHAR, a.fecha_hora, 120) AS fecha_hora, 
                   u.nombreyape AS usuario, a.modulo, a.accion, a.detalle
            FROM AUDITORIA a
            INNER JOIN USUARIO u ON a.id_usuario = u.id_usuario
            WHERE 1=1
        """);

        if (desde != null) {
            query.append(" AND CAST(a.fecha_hora AS DATE) >= ? ");
        }
        if (hasta != null) {
            query.append(" AND CAST(a.fecha_hora AS DATE) <= ? ");
        }
        if (modulo != null && !modulo.trim().equalsIgnoreCase("Todos")) {
            query.append(" AND LOWER(TRIM(a.modulo)) = LOWER(TRIM(?)) ");
        }

        query.append(" ORDER BY a.fecha_hora DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (desde != null) {
                stmt.setDate(paramIndex++, Date.valueOf(desde));
            }
            if (hasta != null) {
                stmt.setDate(paramIndex++, Date.valueOf(hasta));
            }
            if (modulo != null && !modulo.trim().equalsIgnoreCase("Todos")) {
                stmt.setString(paramIndex++, modulo.trim());
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Auditoria(
                    rs.getInt("id_auditoria"),
                    rs.getString("fecha_hora"),
                    rs.getString("usuario"),
                    rs.getString("modulo"),
                    rs.getString("accion"),
                    rs.getString("detalle")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Método que requiere AdminController al cargar el Dashboard
    public ObservableList<Auditoria> obtenerUltimosMovimientos(int limite) {
        ObservableList<Auditoria> lista = FXCollections.observableArrayList();
        String query = """
            SELECT TOP (?) a.id_auditoria, CONVERT(VARCHAR, a.fecha_hora, 120) AS fecha_hora, 
                   u.nombreyape AS usuario, a.modulo, a.accion, a.detalle
            FROM AUDITORIA a
            INNER JOIN USUARIO u ON a.id_usuario = u.id_usuario
            ORDER BY a.fecha_hora DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Auditoria(
                    rs.getInt("id_auditoria"),
                    rs.getString("fecha_hora"),
                    rs.getString("usuario"),
                    rs.getString("modulo"),
                    rs.getString("accion"),
                    rs.getString("detalle")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public ObservableList<Auditoria> obtenerTodosLosMovimientos() {
        ObservableList<Auditoria> lista = FXCollections.observableArrayList();
        
        String sql = """
            SELECT a.id_auditoria, CONVERT(VARCHAR, a.fecha_hora, 120) AS fecha_hora, 
                   u.nombreyape AS usuario, a.modulo, a.accion, a.detalle
            FROM AUDITORIA a
            INNER JOIN USUARIO u ON a.id_usuario = u.id_usuario
            ORDER BY a.fecha_hora DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Auditoria(
                    rs.getInt("id_auditoria"),
                    rs.getString("fecha_hora"),
                    rs.getString("usuario"),
                    rs.getString("modulo"),
                    rs.getString("accion"),
                    rs.getString("detalle")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}