package app.BDD;

import app.Controllers.SessionManager;
import app.Models.Producto;
import app.Models.Usuario;

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
                    float precio_costo = rs.getFloat("precio_costo");
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

    public ObservableList<Producto> loadProducts(){
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        String query = "SELECT id_producto, nombre, descripcion, precio_venta, precio_costo, stock, estado, id_categoria FROM PRODUCTO";
        
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
            
            while(rs.next()){
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String desc = rs.getString("descripcion");
                float precio = rs.getFloat("precio_venta");
                float precio_costo = rs.getFloat("precio_costo");
                int stock = rs.getInt("stock");
                boolean estado = rs.getBoolean("estado");
                int id_categoria = rs.getInt("id_categoria");

                Producto producto = new Producto(id, nombre, desc, precio, precio_costo, stock, estado, id_categoria);
                productos.add(producto);
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    public List<Producto> buscarProductoPorNombre(String termino) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTO WHERE LOWER(nombre) LIKE LOWER(?) AND estado = 1"; 

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

    public void addProducto(String nombre, String descripcion, float precio, int stock, boolean estado, int id_categoria){
        String sql = "INSERT INTO PRODUCTO(nombre, descripcion, precio_venta, stock, estado, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setFloat(3, precio);
            stmt.setInt(4, stock);
            stmt.setBoolean(5, estado);
            stmt.setInt(6, id_categoria);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza un producto y registra en Auditoría cualquier cambio en precio_venta, precio_costo o stock.
     */
    public boolean actualizarProducto(Producto producto, int idUsuario) {
        String sqlSelect = "SELECT nombre, precio_venta, precio_costo, stock FROM PRODUCTO WHERE id_producto = ?";
        String sqlUpdate = "UPDATE PRODUCTO SET nombre = ?, descripcion = ?, precio_venta = ?, stock = ?, estado = ?, id_categoria = ?, precio_costo = ? WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Obtener valores previos
            String nombreAnt = "";
            float precioVentaAnt = 0;
            float precioCostoAnt = 0;
            int stockAnt = 0;
            boolean existe = false;

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, producto.getId());
                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next()) {
                        nombreAnt = rs.getString("nombre");
                        precioVentaAnt = rs.getFloat("precio_venta");
                        precioCostoAnt = rs.getFloat("precio_costo");
                        stockAnt = rs.getInt("stock");
                        existe = true;
                    }
                }
            }

            if (!existe) return false;

            // 2. Ejecutar la actualización del producto
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setString(1, producto.getNombre());
                stmtUpdate.setString(2, producto.getDescripcion());
                stmtUpdate.setFloat(3, producto.getPrecio());
                stmtUpdate.setInt(4, producto.getStock());
                stmtUpdate.setBoolean(5, producto.getEstado());
                stmtUpdate.setInt(6, producto.getId_categoria());
                stmtUpdate.setFloat(7, producto.getPrecio_costo());
                stmtUpdate.setInt(8, producto.getId());

                int filasActualizadas = stmtUpdate.executeUpdate();
                if (filasActualizadas == 0) return false;
            }

            // 3. Registrar auditorías si hubo cambios (usando el módulo "Productos")
            if (precioVentaAnt != producto.getPrecio()) {
                String detalle = String.format("Producto '%s' (ID %d) cambió precio venta de $%.2f a $%.2f",
                        nombreAnt, producto.getId(), precioVentaAnt, producto.getPrecio());
                AuditoriaService.registrar(idUsuario, "Productos", "Modificación Precio Venta", detalle);
            }

            if (precioCostoAnt != producto.getPrecio_costo()) {
                String detalle = String.format("Producto '%s' (ID %d) cambió precio costo de $%.2f a $%.2f",
                        nombreAnt, producto.getId(), precioCostoAnt, producto.getPrecio_costo());
                AuditoriaService.registrar(idUsuario, "Productos", "Modificación Precio Costo", detalle);
            }

            if (stockAnt != producto.getStock()) {
                String detalle = String.format("Producto '%s' (ID %d) cambió stock de %d a %d unidades",
                        nombreAnt, producto.getId(), stockAnt, producto.getStock());
                AuditoriaService.registrar(idUsuario, "Productos", "Modificación Stock", detalle);
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sobrecarga de actualizarProducto para llamadas que solo envían el Producto.
     * Obtiene automáticamente el ID del usuario en sesión actual.
     */
    public boolean actualizarProducto(Producto producto) {
        Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
        int idUsuario = (usuarioActual != null) ? usuarioActual.getIdUsuario() : 0;
        return actualizarProducto(producto, idUsuario);
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
            return -1;
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