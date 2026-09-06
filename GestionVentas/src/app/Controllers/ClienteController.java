package app.Controllers;

import app.BDD.ClienteService;
import app.Models.Cliente;

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
import javafx.scene.control.TableCell;
import java.io.IOException;

public class ClienteController {
    @FXML private TableView<Cliente> tableView;
    @FXML private TableColumn<Cliente, String> nombreCol;
    @FXML private TableColumn<Cliente, String> dniCol;
    @FXML private TableColumn<Cliente, String> emailCol;
    @FXML private TableColumn<Cliente, String> telefonoCol;
    @FXML private TableColumn<Cliente, Void> accionCol;
    @FXML private StackPane mainContent;
    @FXML private TextField buscarCliente;

    private ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private ClienteService client = new ClienteService();
    private CustomAlert customAlert = new CustomAlert();

    @FXML
    public void initialize() {
        // Asignación de propiedades a las columnas
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        dniCol.setCellValueFactory(new PropertyValueFactory<>("dni"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // Cargar datos iniciales desde la BD
        cargarDatosDesdeBD();

        // Búsqueda en tiempo real a medida que el usuario escribe el DNI
        buscarCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarClientes(newValue);
        });

        // Configuración de la columna de acción (Botón Modificar)
        accionCol.setCellFactory(param -> new TableCell<Cliente, Void>() {
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
        });
    }

    private void filtrarClientes(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            cargarDatosDesdeBD();
        } else {
            ObservableList<Cliente> resultados = client.buscarClientesPorDni(criterio);
            tableView.setItems(resultados);
        }
    }

    @FXML
    public void buscarCliente() {
        String dni = buscarCliente.getText().trim();
        if (dni.isEmpty()) {
            cargarDatosDesdeBD();
            customAlert.mostrarAlertaPersonalizada("Aviso", "Ingrese un DNI para filtrar la lista.");
            return;
        }

        ObservableList<Cliente> resultados = client.buscarClientesPorDni(dni);
        if (resultados.isEmpty()) {
            customAlert.mostrarAlertaPersonalizada("Sin Resultados", "No se encontró ningún cliente con ese DNI.");
        }
        tableView.setItems(resultados);
    }

    private void cargarDatosDesdeBD() {
        clientes = client.loadClients();
        tableView.setItems(clientes);
    }

    private void modificarCliente(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/DetalleClienteView.fxml"));
            Node view = loader.load();

            DetalleClienteController controller = loader.getController();
            controller.setCliente(cliente);

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

    @FXML
    public void mostrarAlerta(String titulo, String mensaje) {
        customAlert.mostrarAlertaPersonalizada(titulo, mensaje);
    }

    @FXML
    public void setView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContent.getChildren().clear();
            mainContent.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void recarga() {
        buscarCliente.clear();
        cargarDatosDesdeBD();
    }

    @FXML
    public void añadirCliente(){
        setView("/resources/FormClient.fxml");
    }
}