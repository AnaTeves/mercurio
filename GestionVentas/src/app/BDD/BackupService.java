package app.BDD;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BackupService {

    private static final String NOMBRE_BD = "DB_GESTIONVENTAS";
    
    // Conexión exclusiva a la base de datos de sistema 'master'
    private static final String URL_MASTER = "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false;";
    private static final String USER = "analuzteves";
    private static final String PASSWORD = "1234analuz";

    public boolean crearBackup(File destino) {
        String query = "BACKUP DATABASE [" + NOMBRE_BD + "] TO DISK = ? WITH FORMAT, INIT, NAME = 'Backup Full " + NOMBRE_BD + "'";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, destino.getAbsolutePath());
            stmt.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restaurarBackup(File archivoBak) {
        String setSingleUser = "ALTER DATABASE [" + NOMBRE_BD + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE";
        String restoreDb     = "RESTORE DATABASE [" + NOMBRE_BD + "] FROM DISK = ? WITH REPLACE";
        String setMultiUser  = "ALTER DATABASE [" + NOMBRE_BD + "] SET MULTI_USER";

        // Conexión independiente conectada a 'master'
        try (Connection connMaster = DriverManager.getConnection(URL_MASTER, USER, PASSWORD)) {

            // Forzar el cierre de todas las sesiones activas en DB_GESTIONVENTAS
            try (Statement stmt = connMaster.createStatement()) {
                stmt.execute(setSingleUser);
            }

            // Ejecutar la restauración
            try (PreparedStatement stmt = connMaster.prepareStatement(restoreDb)) {
                stmt.setString(1, archivoBak.getAbsolutePath());
                stmt.execute();
            }

            // Devolver la BDD al modo multiusuario
            try (Statement stmt = connMaster.createStatement()) {
                stmt.execute(setMultiUser);
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            // Intento de rescate si ocurre una falla a mitad del proceso
            try (Connection connMaster = DriverManager.getConnection(URL_MASTER, USER, PASSWORD);
                Statement stmt = connMaster.createStatement()) {
                stmt.execute(setMultiUser);
            } catch (Exception ignored) {}

            return false;
        }
    }

    public String obtenerNombreSugerido() {
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return "Backup_" + NOMBRE_BD + "_" + dtf.format(java.time.LocalDateTime.now()) + ".bak";
    }
}