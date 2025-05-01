package app.BDD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.Models.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// Clase que maneja la interaccion con la base de datos de la tabla cliente
public class ClienteService {

    public Connection connection;

    public ClienteService(){
        this.connection = DatabaseConnection.getConnection();
    }

    // public List<Cliente> obtenerCleintesDesdeBD() {
    //     List<Cliente> clientes = new ArrayList<>();
    //     String query = "SELECT id_cliente, nomYape, documento, email, telefono FROM CLIENTE"; // Solo categorías activas

    //     try (PreparedStatement stmt = connection.prepareStatement(query)) {
    //         ResultSet rs = stmt.executeQuery();
    //         while (rs.next()) {
    //             int idCliente = rs.getInt("id_cliente");
    //             String nombre = rs.getString("nombre");
    //             String documento = rs.getString("documento");
    //             String email = rs.getString("email");
    //             String telefono = rs.getString("telefono");

    //             Cliente cliente = new Cliente(idCliente, nombre, documento, email, telefono);
    //             clientes.add(cliente);
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return clientes;
    // }

    public static List<Cliente> obtenerClientesActivos() {
        List<Cliente> clientes = new ArrayList<>();
        String query = "SELECT id_cliente, nomYape, documento, email FROM CLIENTE WHERE estado = 'activo'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(new Cliente(rs.getInt("id_cliente"), rs.getString("nomYape"), rs.getString("documento"), rs.getString("email")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return clientes;
    }

    // Metodo que carga los clientes de la base de datos en una tabla
    public ObservableList<Cliente> loadClients(){
        ObservableList<Cliente> clientes = FXCollections.observableArrayList(); // Creamos una lista observable de clientes
        String query = "SELECT nomYape, documento, email, telefono FROM CLIENTE"; // Consulta SQL que selecciona las columnas de la tabla cliente
        // Abrimos la conexion a la base de datos y ejecutamos la consulta
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);){
            // Recorremos el resultado de la consulta y lo añadimos a la lista observable
            while(rs.next()){
                String nombre = rs.getString("nomYape");
                String documento = rs.getString("documento");
                String email = rs.getString("email");
                String telefono = rs.getString("telefono");

                Cliente cliente = new Cliente(nombre, documento, email, telefono);
                clientes.add(cliente);
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientes; // Retorna la lista
    }

    public List<Cliente> obtenerClientes() {
        List<Cliente> clientes = new ArrayList<>();
    
        String query = "SELECT id_cliente, nomYape, documento, email, telefono FROM CLIENTE";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String nombre = resultSet.getString("nomYape");
                String dni = resultSet.getString("documento");
                String email = resultSet.getString("email");
                String telefono = resultSet.getString("telefono");

                Cliente cliente = new Cliente(nombre, dni, email, telefono);
                clientes.add(cliente);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clientes;
    }

    // Metodo que busca a un cliente por su DNI
    public Cliente searchClient(String dni){
        String query = "SELECT * FROM CLIENTE WHERE documento = ?"; // Consulta SQL para buscar al cliente por su DNI
        Cliente cliente = null; // Creacion de variable para almacenar el cliente encontrado
        // Abrimos la conexion a la base de datos y ejecutamos la consulta
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);){
            // Asignamos el dni del cliente a la consulta
            stmt.setString(1, dni); // Asigna el valor dni al parametro de la consulta
            ResultSet rs = stmt.executeQuery();
            // Si encuentra un resultado, extrae los datos
            if(rs.next()){
                String nombre = rs.getString("nomYape");
                String documento = rs.getString("documento");
                String email = rs.getString("email");
                String telefono = rs.getString("telefono"); 
                // Añado los datos extraidos a la variable
                cliente = new Cliente(nombre, documento, email, telefono);
            }
        } catch (SQLException e) {
            e.printStackTrace();            
        }
        return cliente;
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM CLIENTE WHERE documento = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setDni(rs.getString("documento"));
                cliente.setNombre(rs.getString("nomYape"));
                // Aquí se devuelve un Optional con el cliente encontrado
                return Optional.of(cliente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Si no se encuentra nada, se devuelve un Optional vacío
        return Optional.empty();
    }


    // Metodo que agrega un nuevo cliente a la base de datos
    public void addCliente(String nombre, String dni, String email, String telefono){
        String sql = "INSERT INTO Cliente(nomYape, documento, email, telefono) VALUES (?, ?, ?, ?)"; // Consulta SQL para insertar una nueva cateogoria

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);){
                // Asignamos los valores a los parametros de la consulta
                stmt.setString(1, nombre);
                stmt.setString(2, dni);
                stmt.setString(3, email);
                stmt.setString(4, telefono);
                stmt.executeUpdate(); // Ejecuta la consulta para insertar la nueva categoria en la base de datos
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public void updateCliente(Cliente cliente) {
    //     String query = "UPDATE CLIENTE SET estado = ? WHERE DNI = ?";
    //     try (Connection connection = DatabaseConnection.getConnection();
    //         PreparedStatement preparedStatement = connection.prepareStatement(query)) {
    //         preparedStatement.setString(1, usuario.getEstado());
    //         preparedStatement.setString(2, usuario.getDni());
    //         preparedStatement.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }
}