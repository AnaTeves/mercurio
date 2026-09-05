package app.Controllers;

import java.io.IOException;
import java.util.List;
import app.BDD.ClienteService;
import app.Models.Cliente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClientsListController {

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colDni;
    
    private String clienteSeleccionado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));

        cargarClientesActivos();
    }

    private void cargarClientesActivos() {
        List<Cliente> clientes = ClienteService.obtenerClientesActivos();
        tablaClientes.getItems().setAll(clientes);
    }

    @FXML
    public void seleccionarCliente() {
        Cliente cliente = tablaClientes.getSelectionModel().getSelectedItem();
        if (cliente != null) {
            clienteSeleccionado = cliente.getDni();
            ((Stage) tablaClientes.getScene().getWindow()).close();
        }
    }

    @FXML
    public void abrirFormularioCliente() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/FormClient.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Registrar Nuevo Cliente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Recargar lista al volver
            cargarClientesActivos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClienteSeleccionado() {
        return clienteSeleccionado;
    }
}