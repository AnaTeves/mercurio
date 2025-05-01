package app.BDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import app.Models.Categoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import java.sql.Date;
import java.time.LocalDate;

// Clase que maneja la interaccion con la base de datos de la tabla categoria
public class CategoriaService {

    private Connection connection;

    public CategoriaService(){
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Categoria> obtenerCategoriasDesdeBD() {
        List<Categoria> categorias = new ArrayList<>();
        String query = "SELECT id_categoria, nombre, descripcion, estado FROM CATEGORIA WHERE estado = 'activa'"; // Solo categorías activas

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idCategoria = rs.getInt("id_categoria");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                String estado = rs.getString("estado");

                Categoria categoria = new Categoria(idCategoria, nombre, descripcion, estado);
                categorias.add(categoria);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }
    
    // Metodo que carga las categorias de la base de datos
    public ObservableList<Categoria> loadCategorias(){
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(); // Creamos una lista observable de categorias
        String query = "SELECT id_categoria, nombre, descripcion, estado FROM CATEGORIA"; // Consulta SQL que selecciona las columnas de la tabla categoria
        // Abrimos la conexion a la base de datos y ejecutamos la consulta
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);){
            // Recorremos el resultado de la consulta y lo añadimos a la lista observable
            while(rs.next()){
                int id = rs.getInt("id_categoria");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                String estado = rs.getString("estado");

                Categoria categoria = new Categoria(id, nombre, descripcion, estado);
                categorias.add(categoria);
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias; // Retorna la lista
    }

    // Metodo que permite buscar una categoria por su nombre
    public Categoria searchCategory(String name){
        String query = "SELECT * FROM CATEGORIA WHERE nombre = ?"; // Consulta sql para buscar una categoria por su nombre
        Categoria category = null; // Creacion de variable para almacenar la categoria encontrada
        // Abrimos la conexion a la base de datos y ejecutamos la consulta sql
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);){
                // Asignamos el nombre de la categoria a la consulta
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery(); // Ejecutamos la consulta
                // Si encuentra un resultado, extrae los datos
                if(rs.next()){
                    int id = rs.getInt("id_categoria");
                    String nombre = rs.getString("nombre");
                    String descripcion = rs.getString("descripcion");
                    String estado = rs.getString("estado");
                    // Añado los datos extraidos a la variable
                    category = new Categoria(id, nombre, descripcion, estado);
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }

    // Metodo que agrega una nueva categoria a la base de datos
    public void addCategoria(String nombre, String descripcion){
        String sql = "INSERT INTO Categoria(nombre, descripcion) VALUES (?, ?)"; // Consulta SQL para insertar una nueva cateogoria

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);){
                // Asignamos los valores a los parametros de la consulta
                stmt.setString(1, nombre);
                stmt.setString(2, descripcion);
                stmt.executeUpdate(); // Ejecuta la consulta para insertar la nueva categoria en la base de datos
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo que actualiza el estado de una categoria especifica
    public void updateCategoria(Categoria categoria) {
        String query = "UPDATE CATEGORIA SET estado = ? WHERE id_categoria = ?";
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, categoria.getEstado());
            preparedStatement.setInt(2, categoria.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public List<ReporteCategoria> obtenerVentasPorCategoria() {
    //     List<ReporteCategoria> reporte = new ArrayList<>();
    //     String sql = "SELECT c.nombre AS categoria, SUM(dv.cantidad) AS cantidad_productos " +
    //         "FROM VENTA v " +
    //         "JOIN DETALLE_VENTA dv ON v.id_venta = dv.id_venta " +
    //         "JOIN PRODUCTO p ON dv.id_producto = p.id_producto " +
    //         "JOIN CATEGORIA c ON p.id_categoria = c.id_categoria " +
    //         "GROUP BY c.nombre " +
    //         "ORDER BY cantidad_productos DESC";
    
    //     try (Connection conn = DatabaseConnection.getConnection();
    //          PreparedStatement pstmt = conn.prepareStatement(sql);
    //          ResultSet rs = pstmt.executeQuery()) {
    //         while (rs.next()) {
    //             String nombreCategoria = rs.getString("categoria");
    //             int cantidadProductos = rs.getInt("cantidad_productos");
    
    //             System.out.println("Categoría: " + nombreCategoria + ", Cantidad: " + cantidadProductos);
    
    //             ReporteCategoria categoria = new ReporteCategoria(nombreCategoria, cantidadProductos);
    //             reporte.add(categoria);
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return reporte;
    // }

    public ObservableList<PieChart.Data> obtenerVentasPorCategoria() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String query = "SELECT c.nombre AS categoria, SUM(dv.cantidad) AS cantidad_productos " +
                       "FROM VENTA v " +
                       "JOIN DETALLE_VENTA dv ON v.id_venta = dv.id_venta " +
                       "JOIN PRODUCTO p ON dv.id_producto = p.id_producto " +
                       "JOIN CATEGORIA c ON p.id_categoria = c.id_categoria " +
                       "GROUP BY c.nombre " +
                       "ORDER BY cantidad_productos DESC";

        try (Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String categoria = rs.getString("categoria");
                int cantidadProductos = rs.getInt("cantidad_productos");

                // Añadir los datos al gráfico de torta
                pieChartData.add(new PieChart.Data(categoria, cantidadProductos));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pieChartData;
    }

    public ObservableList<PieChart.Data> obtenerVentasPorCategoriaFiltradas(LocalDate fromDate, LocalDate toDate) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String query = "SELECT c.nombre AS CATEGORIA, SUM(dv.cantidad) AS cantidad_productos " +
                       "FROM VENTA v " +
                       "JOIN DETALLE_VENTA dv ON v.id_venta = dv.id_venta " +
                       "JOIN PRODUCTO p ON dv.id_producto = p.id_producto " +
                       "JOIN CATEGORIA c ON p.id_categoria = c.id_categoria " +
                       "WHERE v.fecha_venta BETWEEN ? AND ?" +
                       "GROUP BY c.nombre " +
                       "ORDER BY cantidad_productos DESC";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            // Configurar los parámetros del rango de fechas
            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String categoria = rs.getString("categoria");
                int cantidadProductos = rs.getInt("cantidad_productos");

                // Añadir los datos al gráfico de torta
                pieChartData.add(new PieChart.Data(categoria, cantidadProductos));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pieChartData;
    }
    
}