package app.Controllers;
import app.BDD.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import app.Models.Usuario;
import java.io.IOException;

/* Controlador del usuario */
public class UserController {
    @FXML
    private TableView<Usuario> tableView;
    @FXML
    private TableColumn<Usuario, String> nombreCol;
    @FXML
    private TableColumn<Usuario, String> dniCol;
    @FXML
    private TableColumn<Usuario, String> emailCol;
    @FXML
    private TableColumn<Usuario, String> tipoUsuarioCol;
    @FXML
    private TableColumn<Usuario, String> estadoCol;
    private ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    @FXML
    private StackPane mainContent;
    @FXML
    private TextField buscarUser;
    private UserService users = new UserService();

    @FXML
    public void initialize() {
        // Cargo las columnas de la tabla, las propiedades son las definidas en mi clase Usuario
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nomYape"));
        dniCol.setCellValueFactory(new PropertyValueFactory<>("dni"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        tipoUsuarioCol.setCellValueFactory(new PropertyValueFactory<>("idPerfil"));
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        // Creo una nueva columna para manejar la activacion y desactivacion del usuario
        TableColumn<Usuario, String> actionCol = new TableColumn<>("Acciones");
        actionCol.setCellFactory(new Callback<TableColumn<Usuario, String>, TableCell<Usuario, String>>() {
            @Override
            public TableCell<Usuario, String> call(final TableColumn<Usuario, String> param) {
                final TableCell<Usuario, String> cell = new TableCell<Usuario, String>() {
                    private final Button btn = new Button("Activar/Desactivar"); // Definicion del boton
                    // Defino la accion del boton
                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            cambiarEstadoUsuario(usuario); // Llamo al metodo que cambia el estado
                        });
                    }
                    // Actualiza el contenido de la columna
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { // Verifica si la tabla esta vacia
                            setGraphic(null);
                        } else { // Muestra un boton de activacion o desactivacion
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        });
        tableView.getColumns().add(actionCol); // Agrega la nueva columna
        cargarDatosDesdeBD(); // Cargar datos desde la base de datos
    }

    /* Funcion que carga los datos de los usuarios desde la base de datos */
    private void cargarDatosDesdeBD() {
        usuarios = users.loadUsers();
        tableView.setItems(usuarios);
    }

    /* Funcion que cambia el estado del usuario */
    private void cambiarEstadoUsuario(Usuario usuario) {
        String nuevoEstado = usuario.getEstado().equals("activo") ? "inactivo" : "activo"; // Alterna el estado del usuario
        usuario.setEstado(nuevoEstado); // Asigna el nuevo estado
        users.updateUsuario(usuario); // Actualiza el estado del usuario en la base de datos
        cargarDatosDesdeBD(); // Vuelve a cargar los datos para reflejar el cambio en la tabla
    }

    // Metodo que muestra una alerta
    @FXML
    public void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Metodo que busca usuario por su dni
    @FXML
    public void buscarUsuario() {
        String dni = buscarUser.getText();
        // Si el campo esta vacio muestra mensaje de error
        if (dni.isEmpty()) { 
            System.out.println("Ingrese un DNI para buscar");
            return;
        }
        // Llama al método para buscar el usuario en la base de datos
        Usuario usuario = users.searchUser(dni);
        if (usuario != null){ // Si encontramos un usuario
            tableView.getItems().clear(); // Limpiamos la tabla
            tableView.getItems().add(usuario); // Mostramos el usuario
        } else { // Si no encontramos un usuario    
            mostrarAlerta("Error", "Usuario no encontrado");
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

    // Metodo que carga la vista del formulario para añadir un usuario
    @FXML
    public void añadirUsuario(){
        setView("/resources/FormUser.fxml");
    }

    // Metodo que carga la vista inicial de la gestion de usuarios
    @FXML
    public void recarga() {
        setView("/resources/UserView.fxml");
    }
}