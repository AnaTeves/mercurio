package app.Controllers;
import app.BDD.CategoriaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import app.Models.Categoria;
import java.io.IOException;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

/* Controlador de la tabla categorias */
public class CategoriaController {
    @FXML
    private TableView<Categoria> tableView;
    @FXML
    private TableColumn<Categoria, Integer> idCol;
    @FXML
    private TableColumn<Categoria, String> nombreCol;
    @FXML
    private TableColumn<Categoria, String> descCol;
    @FXML
    private TableColumn<Categoria, String> estadoCol;
    private ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    @FXML
    private StackPane mainContent;
    @FXML
    private TextField buscarCategoria;
    private CategoriaService categoriaService = new CategoriaService();
    @FXML
    private VBox vbox;
    CustomAlert customAlert = new CustomAlert();

    @FXML
    public void initialize() {
        // Configuramos las columnas de la table view
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        // Creo una nueva columna para manejar la activacion y desactivacion de las categorias
        TableColumn<Categoria, String> actionCol = new TableColumn<>("Estado");
        actionCol.setCellFactory(new Callback<TableColumn<Categoria, String>, TableCell<Categoria, String>>() {
            @Override
            public TableCell<Categoria, String> call(final TableColumn<Categoria, String> param) {
                final TableCell<Categoria, String> cell = new TableCell<Categoria, String>() {
                    private final Button btn = new Button(); // Definicion del boton
                    {
                        btn.setOnAction((ActionEvent event) -> { // Defino la accion al presionar el boton
                            Categoria categoria = getTableView().getItems().get(getIndex());
                            cambiarEstadoCategoria(categoria); // Llamo a la funcion que cambia el estado de la categoria
                        });
                    }
                    // Actualiza el contenido de la columna
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { // Verifica si la tabla esta vacia
                            setGraphic(null); // No muestra nada
                        } else { // Muestra un boton de activacion o desactivacion
                            Categoria categoria = getTableView().getItems().get(getIndex());
                            if (categoria.getEstado().equals("activa")) { // Si la categoria se encuentra activa
                                btn.setText("Desactivar"); // El boton permite desactivar
                                btn.setStyle("-fx-background-color: #fbb09d; -fx-text-fill: black;");
                            } else { // Si la categoria se encuentra inactiva
                                btn.setText("Activar"); // El boton permite activar
                                btn.setStyle("-fx-background-color: #b6dfaa;; -fx-text-fill: black;");
                            }
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        });
        tableView.getColumns().add(actionCol); // Agrega la nueva columna
        tableView.prefWidthProperty().bind(vbox.widthProperty()); // Vincula el ancho de la table view con el vbox 
        cargarDatosDesdeBD(); // Cargo datos desde la base de datos
    }

    private void cargarDatosDesdeBD() {
        categorias = categoriaService.loadCategorias(); // Cargo todas las categorias desde la base de datos
        tableView.setItems(categorias); // Asigno las categorias a la tabla
    }

    // Método que cambia el estado de la categoria
    private void cambiarEstadoCategoria(Categoria categoria) {
        String nuevaCategoria = categoria.getEstado().equals("activa") ? "inactiva" : "activa";
        categoria.setEstado(nuevaCategoria); // Asigna el nuevo estado a la categoria
        categoriaService.updateCategoria(categoria); // Actualiza en la base de datos
        cargarDatosDesdeBD(); // Vuelve a cargar los datos para reflejar el cambio en la tabla
    }

    @FXML
    public void mostrarAlerta(String titulo, String mensaje) {
        // Alert alert = new Alert(AlertType.INFORMATION);
        // alert.setTitle(titulo);
        // alert.setHeaderText(null);
        // alert.setContentText(mensaje);
        // alert.showAndWait();
        customAlert.mostrarAlertaPersonalizada(titulo, mensaje);
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

    // Metodo que carga la vista del formulario para añadir una categoria
    @FXML
    public void añadirCategoria(){
        setView("/resources/FormCategoria.fxml");
    }

    // Metodo que permite buscar una categoria
    @FXML
    public void buscarCategoria() {
        String name = buscarCategoria.getText(); // Extraigo el nombre ingresado en el buscador
        if(name.isEmpty()) { // Si el campo esta vacio
            customAlert.mostrarAlertaPersonalizada("Error", "Ingrese el nombre de una categoria.");
            return;
        }
        Categoria categoria = categoriaService.searchCategory(name); 
        if(categoria != null) {
            tableView.getItems().clear(); // Limpiamos la tabla
            tableView.getItems().add(categoria); // Mostramos la categoria
        } else {
            customAlert.mostrarAlertaPersonalizada("Error", "Categoria no encontrada.");
        }
    }

    // Metodo que recarga a la vista inicial de la gestion de categorias
    @FXML
    public void recarga() {
        setView("/resources/CategoriasView.fxml");
    }
}
