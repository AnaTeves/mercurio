package app.BDD;
import app.Models.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// Clase que maneja la interaccion con la base de datos
public class InventService {

    public Producto buscarProductoPorId(int id) {
    Producto producto = null;
    String sql = "SELECT id_producto, nombre, descripcion, precio_venta, precio_costo, stock, estado, id_categoria FROM PRODUCTO WHERE id_producto = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int productoId = rs.getInt("id_producto");
                    String nombre = rs.getString("nombre");
                    String descripcion = rs.getString("descripcion");
                    float precio = rs.getFloat("precio_venta");
                    int stock = rs.getInt("stock");
                    boolean estado = rs.getBoolean("estado");
                    int id_categoria = rs.getInt("id_categoria");
                    producto = new Producto(productoId, nombre, descripcion, precio, precio_costo, stock, estado, id_categoria);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return producto;
    }

    // Metodo que carga todos los productos desde la base de datos
    public ObservableList<Producto> loadProducts(){
        ObservableList<Producto> productos = FXCollections.observableArrayList(); // Creamos una lista observable de productos
        String query = "SELECT id_producto,nombre, descripcion, precio_venta, precio_costo, stock, estado, id_categoria FROM PRODUCTO"; // Consulta SQL que selecciona las columnas de la tabla producto
        // Abrimos la conexion a la base de datos y ejecutamos la consulta
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);){
            // Recorremos el resultado de la consulta y lo añadimos a la lista observable
            while(rs.next()){
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String desc = rs.getString("descripcion");
                float precio = rs.getFloat("precio_venta");
                int stock = rs.getInt("stock");
                boolean estado = rs.getBoolean("estado");
                int id_categoria = rs.getInt("id_categoria");

                Producto producto = new Producto(id, nombre, desc, precio, precio_costo, stock, estado, id_categoria);
                productos.add(producto);
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos; // Retorna la lista de los productos
    }

    // Método para buscar productos por nombre
    public List<Producto> buscarProductoPorNombre(String termino) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTO WHERE LOWER(nombre) LIKE LOWER(?) AND estado = 1"; // Búsqueda insensible a mayúsculas

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termino + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecio(rs.getFloat("precio_venta"));
                producto.setStock(rs.getInt("stock"));
                producto.setEstado(rs.getBoolean("estado"));
                producto.setId_categoria(rs.getInt("id_categoria"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    // Metodo para agregar un producto a la base de datos
    public void addProducto(String nombre, String descripcion, float precio, int stock, boolean estado, int id_categoria){
        String sql = "INSERT INTO PRODUCTO(nombre, descripcion, precio_venta, stock, estado, id_categoria) VALUES (?, ?, ?, ?, ?, ?)"; // Consulta SQL para insertar un nuevo producto

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);){
                // Asignamos los valores a los parametros de la consulta
                stmt.setString(1, nombre);
                stmt.setString(2, descripcion);
                stmt.setFloat(3, precio);
                stmt.setInt(4, stock);
                stmt.setBoolean(5, estado);
                stmt.setInt(6, id_categoria);
                stmt.executeUpdate(); // Ejecuta la consulta para insertar al nuevo producto en la base de datos
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizarProducto(Producto producto){
        String sql = "UPDATE PRODUCTO SET nombre = ?, descripcion = ?, precio_venta = ?, stock = ?, id_categoria = ?, estado = ? WHERE id_producto = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);){

                stmt.setString(1, producto.getNombre());
                stmt.setString(2, producto.getDescripcion());
                stmt.setFloat(3, producto.getPrecio());
                stmt.setInt(4, producto.getStock());
                stmt.setInt(5, producto.getId_categoria());
                stmt.setBoolean(6, producto.getEstado());
                stmt.setInt(7, producto.getId());

                int filasActualizadas = stmt.executeUpdate();

                if(filasActualizadas > 0){
                    // Emitir dialog
                    System.out.println("Producto actualizado correctamente.");
                }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int obtenerIdCategoria(String categoriaDescripcion) {
        String sql = "SELECT id_categoria FROM CATEGORIA WHERE nombre = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, categoriaDescripcion);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt("id_categoria");
            } 

        } catch (SQLException e) {
            return -1; // En caso de error en la consulta
        }

        return -1;
    }

    public boolean obtenerEstado(String estado){
        String sql = "SELECT estado FROM PRODUCTO WHERE estado = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return rs.getBoolean("estado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Integer> obtenerProductosBajoStock(){
        Map<String, Integer> productos = new HashMap<>();
        String sql = "SELECT nombre, stock FROM PRODUCTO WHERE stock <= 5";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int stock = rs.getInt("stock");
                productos.put(nombre, stock);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }
}