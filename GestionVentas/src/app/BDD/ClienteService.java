package app.BDD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public ObservableList<Cliente> loadClients() {
    ObservableList<Cliente> lista = FXCollections.observableArrayList();
    String sql = "SELECT id_cliente, nomYape, documento, email, telefono FROM Cliente"; // Incluir id_cliente

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Cliente cliente = new Cliente();
            cliente.setId(rs.getInt("id_cliente"));       // <-- AQUÍ SE ASIGNA EL ID
            cliente.setNombre(rs.getString("nomYape"));
            cliente.setDni(rs.getString("documento"));
            cliente.setEmail(rs.getString("email"));
            cliente.setTelefono(rs.getString("telefono"));

            lista.add(cliente);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
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
    public Cliente searchClient(String dni) {
    String sql = "SELECT id_cliente, nomYape, documento, email, telefono FROM Cliente WHERE documento = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, dni);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id_cliente"));   // <-- AQUÍ TAMBIÉN
                cliente.setNombre(rs.getString("nomYape"));
                cliente.setDni(rs.getString("documento"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefono(rs.getString("telefono"));
                return cliente;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
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
        String sql = "INSERT INTO Cliente(nomYape, documento, email, telefono) VALUES (?, ?, ?, ?)"; 

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);){
                // Asignamos los valores a los parametros de la consulta
                stmt.setString(1, nombre);
                stmt.setString(2, dni);
                stmt.setString(3, email);
                stmt.setString(4, telefono);
                stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo que actualiza los datos de un cliente en la base de datos
    public boolean actualizarCliente(Cliente cliente) {
    String sql = "UPDATE Cliente SET nomYape = ?, documento = ?, email = ?, telefono = ? WHERE id_cliente = ?";
    System.out.println(cliente.getId());

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNombre());    
            stmt.setString(2, cliente.getDni()); 
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getTelefono());
            stmt.setInt(5, cliente.getId());   

            int filasActualizadas = stmt.executeUpdate();
            return filasActualizadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}