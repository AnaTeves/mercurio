package app.BDD;
import app.Models.Venta;
import app.Models.DetalleVenta;
import app.Models.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VentaService {

    public Connection connection;

    public VentaService() {
    }

    public List<Venta> getAllVentas() {
        List<Venta> ventas = new ArrayList<>();
        String query = """
            SELECT 
                V.fecha_venta, 
                V.total_venta, 
                U.DNI AS dniUsuario, 
                C.documento AS dniCliente
            FROM 
                VENTA V
            JOIN USUARIO U ON V.id_usuario = U.id_usuario
            JOIN CLIENTE C ON V.id_cliente = C.id_cliente;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(query)) {

            while (resultSet.next()) {
                Timestamp fechaVenta = resultSet.getTimestamp("fecha_venta");
                float totalVenta = resultSet.getFloat("total_venta");
                String dniUsuario = resultSet.getString("dniUsuario");
                String dniCliente = resultSet.getString("dniCliente");
                System.out.println(fechaVenta + " " + totalVenta + " " + dniUsuario + " " + dniCliente);
                ventas.add(new Venta(fechaVenta, totalVenta, dniUsuario, dniCliente));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventas;
    }

    public ObservableList<DetalleVenta> loadVentas(){
        ObservableList<DetalleVenta> ventas = FXCollections.observableArrayList(); // Creamos una lista observable
        String query = "SELECT cantidad, precio_unitario, id_producto FROM DETALLE_VENTA"; // Consulta SQL que selecciona las columnas de la tabla
        // Abrimos la conexion a la base de datos y ejecutamos la consulta
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);){
            // Recorremos el resultado de la consulta y lo añadimos a la lista observable
            while(rs.next()){
                int cantidad = rs.getInt("cantidad");
                float precio = rs.getFloat("precio_unitario");
                int id_producto = rs.getInt("id_producto");

                DetalleVenta venta = new DetalleVenta(cantidad, precio, id_producto);
                ventas.add(venta);
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ventas; // Retorna la lista
    }

    public List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();
    
        String query = "SELECT id_producto, nombre, descripcion, precio_venta, stock, estado, id_categoria FROM PRODUCTO WHERE estado = 1";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id_producto");
                String nombre = resultSet.getString("nombre");
                String descripcion = resultSet.getString("descripcion");
                float precio = resultSet.getFloat("precio_venta");
                int stock = resultSet.getInt("stock");
                Boolean estado = resultSet.getBoolean("estado");
                int id_categoria = resultSet.getInt("id_categoria");

                Producto producto = new Producto(id, nombre, descripcion, precio, stock, estado, id_categoria);
                productos.add(producto);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productos;
    }

    // Método para registrar la venta y devolver el id generado
    public void registrarVenta(Venta venta, List<DetalleVenta> detallesVenta) throws SQLException {
        // SQL para insertar una venta
        String sqlVenta = "INSERT INTO VENTA(fecha_venta, total_venta, id_usuario, id_cliente, id_caja) VALUES (?, ?, ?, ?, ?)";
        // Conexion a la base de datos
        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);
        
            int idVenta = 0;

            try (PreparedStatement statement = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                // Setear los parámetros de la venta
                statement.setTimestamp(1, venta.getFechaVenta());  // Fecha de la venta
                statement.setFloat(2, venta.getTotalVenta());  // Total de la venta
                statement.setInt(3, venta.getIdusuario());  // ID del usuario
                statement.setInt(4, venta.getIdcliente());  // ID del cliente
                statement.setInt(5, venta.getIdcaja());  // ID de la caja
                
                // Ejecutar la consulta de inserción
                int affectedRows = statement.executeUpdate();
                System.out.println("Filas afectadas al insertar venta: " + affectedRows);

                if(affectedRows == 0){
                    throw new SQLException("No se pudo insertar la venta.");
                }

                try(ResultSet rs = statement.getGeneratedKeys()) {
                    if(rs.next()) {
                        idVenta = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la venta.");
                    }
                }

                registrarDetallesVenta(conn, idVenta, detallesVenta);
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void descontarStock(int idProducto, int cantidad) throws SQLException {
        String query = "UPDATE PRODUCTO SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
        try(Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, cantidad);
            statement.setInt(2, idProducto);
            statement.setInt(3, cantidad);

            int rosAffected = statement.executeUpdate();
            if(rosAffected == 0){
                throw new SQLException("No se pudo actualizar el stock.");
            }
        }
    }

    public int obtenerIdUsuario(String dniUsuario) {
        String sql = "SELECT id_usuario FROM USUARIO WHERE DNI = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, dniUsuario);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt("id_usuario");
            } 

        } catch (SQLException e) {
            return -1; // En caso de error en la consulta
        }

        return -1;
    }

    public int obtenerIdCliente(String dniCliente) {
        String sql = "SELECT id_cliente FROM CLIENTE WHERE documento = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, dniCliente);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt("id_cliente");
            } 

        } catch (SQLException e) {
            return -1; // En caso de error en la consulta
        }

        return -1;
    }

    // Método auxiliar para registrar los detalles de la venta
    public void registrarDetallesVenta(Connection conn, int idVenta, List<DetalleVenta> detallesVenta) throws SQLException {
        String sqlDetalleVenta = "INSERT INTO DETALLE_VENTA (cantidad, precio_unitario, id_venta, id_producto) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement statement = conn.prepareStatement(sqlDetalleVenta)) {
            // Iterar sobre los detalles de la venta
            for (DetalleVenta detalle : detallesVenta) {
                statement.setInt(1, detalle.getCantidad());  
                statement.setFloat(2, detalle.getPrecioUnitario());  
                statement.setInt(3, idVenta);  
                statement.setInt(4, detalle.getId_producto() );  
                statement.addBatch();  // Agregar a la batch
            }
            statement.executeBatch();  // Ejecutar el batch de inserciones
        }
    }

    public Map<String, Integer> obtenerVentasPorDiaDeLaSemana() {
        String query = """
                SELECT DATENAME(WEEKDAY, fecha_venta) AS dia, COUNT(*) AS total_ventas
                FROM VENTA
                GROUP BY DATENAME(WEEKDAY, fecha_venta)
                ORDER BY 
                    CASE DATENAME(WEEKDAY, fecha_venta)
                        WHEN 'Monday' THEN 1
                        WHEN 'Tuesday' THEN 2
                        WHEN 'Wednesday' THEN 3
                        WHEN 'Thursday' THEN 4
                        WHEN 'Friday' THEN 5
                        WHEN 'Saturday' THEN 6
                        WHEN 'Sunday' THEN 7
                    END;
                """;

        Map<String, Integer> ventasPorDia = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String dia = resultSet.getString("dia");
                int totalVentas = resultSet.getInt("total_ventas");
                ventasPorDia.put(dia, totalVentas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ventasPorDia;
    }

    public Map<String, Integer> obtenerVentasPorVendedor(String vendedorSeleccionado) {
        Map<String, Integer> ventasPorVendedor = new HashMap<>();
        
        // Consulta SQL para filtrar las ventas por el vendedor seleccionado
        String query = """
                SELECT DATENAME(WEEKDAY, v.fecha_venta) AS dia, COUNT(*) AS total_ventas
                FROM VENTA v
                JOIN USUARIO u ON v.id_usuario = u.id_usuario 
                WHERE u.nombreyape = ? 
                GROUP BY DATENAME(WEEKDAY, v.fecha_venta)
                ORDER BY 
                    CASE DATENAME(WEEKDAY, v.fecha_venta)
                        WHEN 'Monday' THEN 1
                        WHEN 'Tuesday' THEN 2
                        WHEN 'Wednesday' THEN 3
                        WHEN 'Thursday' THEN 4
                        WHEN 'Friday' THEN 5
                        WHEN 'Saturday' THEN 6
                        WHEN 'Sunday' THEN 7
                    END;
                """;
    
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            
            // Establece el valor del parámetro del vendedor seleccionado
            statement.setString(1, vendedorSeleccionado);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                // Itera sobre los resultados y agrega al mapa
                while (resultSet.next()) {
                    String dia = resultSet.getString("dia");
                    int totalVentas = resultSet.getInt("total_ventas");      
                    
                    ventasPorVendedor.put(dia, totalVentas);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return ventasPorVendedor;
    }
    
    public ObservableList<XYChart.Series<String, Number>> obtenerProductosVendidos() {
        ObservableList<XYChart.Series<String, Number>> barChartData = FXCollections.observableArrayList();

        // Crear la serie para los datos
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cantidad de Productos Vendidos");

        String query = "SELECT p.nombre AS producto, SUM(dv.cantidad) AS cantidad_vendida " +
                    "FROM VENTA v " +
                    "JOIN DETALLE_VENTA dv ON v.id_venta = dv.id_venta " +
                    "JOIN PRODUCTO p ON dv.id_producto = p.id_producto " +
                    "GROUP BY p.nombre " +
                    "ORDER BY cantidad_vendida DESC";

        try (Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String producto = rs.getString("producto");
                int cantidadVendida = rs.getInt("cantidad_vendida");

                 // Abreviar el nombre del producto
                String nombreAbreviado = abreviarNombreProducto(producto);

                // Añadir los datos a la serie del gráfico de barras
                series.getData().add(new XYChart.Data<>(nombreAbreviado, cantidadVendida));
            }

            barChartData.add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return barChartData;
    }

    private String abreviarNombreProducto(String nombre) {
        int maxLongitud = 15; // Longitud máxima del nombre en el gráfico
        if (nombre.length() > maxLongitud) {
            return nombre.substring(0, maxLongitud) + "..."; // Añadir "..." al final si el nombre es demasiado largo
        }
        return nombre;
    }

    //FUNCIONA Y SE USA
    public double obtenerIngresosMensuales(int mesSeleccionado) {
        String sql = "SELECT SUM(total_venta) AS total_ingresos FROM VENTA WHERE MONTH(fecha_venta) = ?";
        double ingresosMensuales = 0;
    
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setInt(1, mesSeleccionado);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total_ingresos");
                ingresosMensuales += total;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingresosMensuales;
    }

    public Map<String, Double> obtenerIngresosPorMes() {
        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
        String query = "SELECT FORMAT(fecha_venta, 'yyyy-MM') AS mes, SUM(total_venta) AS total "
                    + "FROM VENTA "
                    + "GROUP BY FORMAT(fecha_venta, 'yyyy-MM'), YEAR(fecha_venta), MONTH(fecha_venta) "
                    + "ORDER BY YEAR(fecha_venta), MONTH(fecha_venta)";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ingresosPorMes.put(rs.getString("mes"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingresosPorMes;
    }
    
    private static String obtenerNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes - 1];
    }

    public static Map<String, Double> obtenerIngresosSemanalesPorMes(String mesNombre) {
        int mesNumero = Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
                            .indexOf(mesNombre) + 1;

        String sql = "SELECT DATEPART(WEEK, fecha_venta) AS semana, SUM(total_venta) AS total_ingresos " +
                    "FROM VENTA WHERE MONTH(fecha_venta) = ? GROUP BY DATEPART(WEEK, fecha_venta) ORDER BY semana";
        Map<String, Double> ingresosSemanales = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mesNumero);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String semana = "Semana " + rs.getInt("semana");
                double total = rs.getDouble("total_ingresos");
                ingresosSemanales.put(semana, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingresosSemanales;
    }

    public Map<String, Object> obtenerReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String query = """
            SELECT COUNT(*) AS total_ventas, SUM(total_venta) AS monto_total
            FROM VENTA
            WHERE fecha_venta BETWEEN ? AND ?;
        """;

        try (Connection conn = DatabaseConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(LocalTime.MAX)));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("total_ventas", rs.getInt("total_ventas"));
                resultado.put("monto_total", rs.getDouble("monto_total"));
                return resultado;
            }
        }
        return null;
    }

    public ObservableList<PieChart.Data> obtenerProductosMasVendidos(LocalDate inicio, LocalDate fin) throws SQLException {
        String query = """
                SELECT TOP 5 p.nombre, SUM(dv.cantidad) AS total
                FROM VENTA v
                INNER JOIN DETALLE_VENTA dv ON v.id_venta = dv.id_venta
                INNER JOIN PRODUCTO p ON dv.id_producto = p.id_producto
                WHERE v.fecha_venta BETWEEN ? AND ?
                GROUP BY p.nombre
                ORDER BY total DESC;
                """;

                ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(fin.atTime(LocalTime.MAX)));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int total = rs.getInt("total");
                data.add(new PieChart.Data(nombre, total));
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }   
        return data;
    }

    public List<Pair<String, Integer>> obtenerProductosMasVendidos() {
        List<Pair<String, Integer>> lista = new ArrayList<>();
        String query = "SELECT TOP 5 p.nombre, SUM(dv.cantidad) AS total_vendido "
                    + "FROM detalle_venta dv "
                    + "JOIN producto p ON dv.id_producto = p.id_producto "
                    + "GROUP BY p.nombre "
                    + "ORDER BY total_vendido DESC;";

        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int totalVendido = rs.getInt("total_vendido");
                lista.add(new Pair<>(nombre, totalVendido));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Pair<String, Integer>> obtenerTodosProductosConVentas() {
        List<Pair<String, Integer>> lista = new ArrayList<>();
        String query = "SELECT p.nombre, ISNULL(SUM(dv.cantidad), 0) AS total_vendido "
                    + "FROM producto p "
                    + "LEFT JOIN detalle_venta dv ON p.id_producto = dv.id_producto "
                    + "GROUP BY p.nombre "
                    + "ORDER BY total_vendido ASC;";

        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int totalVendido = rs.getInt("total_vendido");
                lista.add(new Pair<>(nombre, totalVendido));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    // FUNCIONA Y SE USA
    public double obtenerIngresosDelDia(){
        double ingresosDelDia = 0;
        String query = "SELECT SUM(total_venta) AS total_ingresos FROM VENTA WHERE CAST(fecha_venta AS DATE) = CAST(GETDATE() AS DATE);";

        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                ingresosDelDia = rs.getDouble("total_ingresos");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingresosDelDia;
    }

    // Método para el autocompletado: Busca por coincidencia de nombre o código
    public List<Producto> buscarProductosPorFiltro(String filtro) {
        List<Producto> productos = new ArrayList<>();
        
        // Ahora filtramos por estado = 1 y mantenemos la búsqueda insensible a mayúsculas
        String query = "SELECT id_producto, nombre, descripcion, precio_venta, stock, estado, id_categoria " +
                    "FROM PRODUCTO WHERE estado = 1 AND (UPPER(nombre) LIKE UPPER(?) OR CAST(id_producto AS VARCHAR) LIKE ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            
            String parametro = "%" + filtro + "%";
            statement.setString(1, parametro);
            statement.setString(2, parametro);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id_producto");
                    String nombre = resultSet.getString("nombre");
                    String descripcion = resultSet.getString("descripcion");
                    
                    // RECUERDA: Si antes cambiaste "precio" por otro nombre (ej. precio_unitario), 
                    // hazlo aquí también. Si en tu BDD se llama "precio", déjalo así.
                    float precio = resultSet.getFloat("precio_venta"); 
                    
                    int stock = resultSet.getInt("stock");
                    Boolean estado = resultSet.getBoolean("estado"); // Aunque sea 1, get string funciona, o usa getInt()
                    int id_categoria = resultSet.getInt("id_categoria");

                    Producto producto = new Producto(id, nombre, descripcion, precio, stock, estado, id_categoria);
                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    // Método para obtener las ventas del día de un usuario específico
    public List<Venta> obtenerVentasDelDiaPorUsuario(String dniUsuario) {
        List<Venta> ventas = new ArrayList<>();
        String query = """
            SELECT 
                V.fecha_venta, 
                V.total_venta, 
                U.DNI AS dniUsuario, 
                C.documento AS dniCliente
            FROM 
                VENTA V
            JOIN USUARIO U ON V.id_usuario = U.id_usuario
            JOIN CLIENTE C ON V.id_cliente = C.id_cliente
            WHERE U.DNI = ? 
            AND CAST(V.fecha_venta AS DATE) = CAST(GETDATE() AS DATE)
            ORDER BY V.fecha_venta DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, dniUsuario);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Timestamp fechaVenta = resultSet.getTimestamp("fecha_venta");
                float totalVenta = resultSet.getFloat("total_venta");
                String dniUsuarioResult = resultSet.getString("dniUsuario");
                String dniCliente = resultSet.getString("dniCliente");
                
                ventas.add(new Venta(fechaVenta, totalVenta, dniUsuarioResult, dniCliente));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventas;
    }
}
