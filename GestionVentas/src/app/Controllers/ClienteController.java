package app.Controllers;
import app.BDD.ClienteService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import app.Models.Cliente;
import javafx.scene.control.TableCell;
import java.io.IOException;

public class ClienteController {
    @FXML
    private TableView<Cliente> tableView;
    @FXML
    private TableColumn<Cliente, String> nombreCol;
    @FXML
    private TableColumn<Cliente, String> dniCol;
    @FXML
    private TableColumn<Cliente, String> emailCol;
    @FXML
    private TableColumn<Cliente, String> telefonoCol;
    private ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    @FXML
    private StackPane mainContent;
    @FXML
    private TextField buscarCliente;
    @FXML private TableColumn<Cliente, Void> accionCol;
    // Creamos una instancia de user service
    private ClienteService client = new ClienteService();
    CustomAlert customAlert = new CustomAlert();

    @FXML
    public void initialize() {
        // Las propiedades son las definidas en mi clase Cliente
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        dniCol.setCellValueFactory(new PropertyValueFactory<>("dni"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        cargarDatosDesdeBD(); // Cargar datos desde la base de datos
        accionCol.setCellFactory(param -> {
            return new TableCell<Cliente, Void>() {
                private final Button btn = new Button("Modificar");
                {
                    btn.setOnAction((ActionEvent event) -> {
                        Cliente cliente = getTableView().getItems().get(getIndex());
                        modificarCliente(cliente);
                    });
                }
                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                    }
                }
            };
        });
    }

    private void modificarCliente(Cliente cliente) {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/DetalleClienteView.fxml"));
                Node view = loader.load();
        
                DetalleClienteController controller = loader.getController();
                controller.setCliente(cliente); // Pasa el producto al controlador.
        
                mainContent.getChildren().clear();
                mainContent.getChildren().add(view);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No se pudo cargar la vista.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
        }
    }

    private void cargarDatosDesdeBD() {
        clientes = client.loadClients();
        tableView.setItems(clientes);
    }

    // Metodo que muestra una alerta
    @FXML
    public void mostrarAlerta(String titulo, String mensaje) {
        customAlert.mostrarAlertaPersonalizada(titulo, mensaje);
    }

    // Metodo que busca un cliente por su dni
    @FXML
    public void buscarCliente() {
        String dni = buscarCliente.getText(); // Extraigo el dni ingresado en el buscador
        if (dni.isEmpty()) { // Si el campo esta vacio muestra mensaje de error
            customAlert.mostrarAlertaPersonalizada("Error", "Ingrese un DNI para buscar el cliente.");
            return;
        }
        // Llama al método para buscar el cliente en la base de datos
        Cliente cliente = client.searchClient(dni);
        if (cliente != null){ // Si encontramos un cliente
            tableView.getItems().clear(); // Limpiamos la tabla
            tableView.getItems().add(cliente); // Mostramos el cliente
        } else { // Si no encontramos un cliente    
            customAlert.mostrarAlertaPersonalizada("Error", "Cliente no encontrado.");
        }
    }

    // Metodo para cargar una vista en el mainContent
    @FXML
    public void setView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContent.getChildren().clear(); // Limpia el contenido actual
            mainContent.getChildren().add(view); // Agrega la nueva vista
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Metodo que recarga a la vista inicial de la gestion de clientes
    @FXML
    public void recarga() {
        setView("/resources/ClientesView.fxml");
    }

    // Metodo que carga la vista del formulario para añadir un cliente
    @FXML
    public void añadirCliente(){
        setView("/resources/FormClient.fxml");
    }
}