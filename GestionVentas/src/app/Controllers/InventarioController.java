package app.Controllers;
import app.BDD.InventService;
import app.Models.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.List;
import javafx.util.Callback;
import javafx.scene.control.TableCell;
import javafx.event.ActionEvent;
import javafx.scene.control.Dialog;

// Controlador del inventario
public class InventarioController {
    @FXML
    private TableView<Producto> tableProducts;
    @FXML
    private TableColumn<Producto, String> nombreCol;
    @FXML
    private TableColumn<Producto, String> descripcionCol;
    @FXML
    private TableColumn<Producto, Float> precioCol;
    @FXML
    private TableColumn<Producto, Integer> codigoCol;
    @FXML
    private TableColumn<Producto, Boolean> estadoCol;
    @FXML
    private TableColumn<Producto, Integer> categoriaCol;
    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    @FXML
    private StackPane mainContent;
    @FXML
    private Button btnBuscarProd;
    @FXML
    private TextField buscarProducto;
    @FXML
    private Button btnAñadir;

    // Creamos una instancia del controlador de la base de datos
    private InventService inventService = new InventService();
    Dialog<String> dialog = new Dialog<>();

    // Metodo para configurar los permisos dependiendo del perfil ?????
    public void configPermisos(String perfil){
        switch (perfil) {
            case "Administrador":
                configPermisosAdmin();
                break;
            case "Gerente":
                configPermisosGerente();
                break;
            case "Vendedor":
                configPermisosEmpleado();
                break;
            default:
                break;
        }
    }
    
    // Configuracion de los permisos del administrador
    private void configPermisosAdmin(){
        btnBuscarProd.setDisable(false);
        btnAñadir.setDisable(false);
        tableProducts.setEditable(false);
    }

    // Configurarion de los permisos del gerente
    private void configPermisosGerente(){
        btnBuscarProd.setDisable(false);
        btnAñadir.setDisable(false);
        tableProducts.setEditable(false);
    }

    // Configuracion de los permisos del empleado
    private void configPermisosEmpleado(){
        btnBuscarProd.setDisable(false);
        btnAñadir.setDisable(true);
        tableProducts.setEditable(true);
    }

    @FXML
    public void initialize() {
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        codigoCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        String perfil = SessionManager.getCurrentUser();
        configPermisos(perfil);

        // Creo una columna para manejar la edicion de los productos
        // TableColumn<Producto, String> editCol = new TableColumn<>("Modificar");
        // editCol.setCellFactory(new Callback<TableColumn<Producto, String>, TableCell<Producto, String>>(){
        //     @Override
        //     public TableCell<Producto, String> call(final TableColumn<Producto, String> param) {
        //         final TableCell<Producto, String> cell = new TableCell<Producto, String>() {
        //             private final Button btn = new Button("Editar"); // Definicion del boton
        //             {
        //                 btn.setOnAction((ActionEvent event) -> {
        //                     Producto producto = getTableView().getItems().get(getIndex());
        //                     abrirFormularioEdicion(producto); // Abre formulario para editar el producto
        //                 });
        //             }

        //             @Override
        //             public void updateItem(String item, boolean empty) {
        //                 super.updateItem(item, empty);
        //                 if (empty) {
        //                     setGraphic(null);
        //                 } else {
        //                     setGraphic(btn);
        //                 }
        //             }
        //         };
        //         return cell;
        //     }
        // });

        // Creo una columna que contiene un boton en cada fila que permite cambiar el estado del producto
        TableColumn<Producto, Boolean> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellFactory(new Callback<TableColumn<Producto, Boolean>, TableCell<Producto, Boolean>>(){
            @Override
            public TableCell<Producto, Boolean> call(final TableColumn<Producto, Boolean> param) {
                final TableCell<Producto, Boolean> cell = new TableCell<Producto, Boolean>() {
                    private final Button btn = new Button(); // Definicion del boton
                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Producto producto = getTableView().getItems().get(getIndex());
                            cambiarEstadoProducto(producto); // Llama funcion que cambia el estado del producto
                            updateItem(producto.getEstado(), false);
                            getTableView().refresh();
                        });
                    }

                    @Override
                    public void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            System.out.println("vacio");
                            setGraphic(null); // No muestra nada si la celda esta vacia
                        } else {
                            Producto producto = getTableView().getItems().get(getIndex());
                            if(producto.getEstado()){
                                btn.setText("Desactivar");
                                btn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                            } else {
                                btn.setText("Activar");
                                btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                            }

                            setGraphic(btn); // Muestra el boton en la celda
                        }
                    }
                };
                return cell;
            }
        });

        // Creo una columna que contiene un boton en cada fila para mostrar mas informacion del producto
        TableColumn<Producto, String> detallesCol = new TableColumn<>("Mas informacion");
        detallesCol.setCellFactory(new Callback<TableColumn<Producto, String>, TableCell<Producto, String>>(){
            @Override
            public TableCell<Producto, String> call(final TableColumn<Producto, String> param) {
                final TableCell<Producto, String> cell = new TableCell<Producto, String>() {
                    private final Button btn = new Button("Ver detalles"); // Definicion del boton
                    {
                        btn.setOnAction((ActionEvent event) -> { // Definimos la accion del boton
                            Producto producto = getTableView().getItems().get(getIndex()); // Obtiene el producto correspondiente a la fila actual
                            verDetalles(producto); 
                        });
                    }
                    /* Metodo que controla la actualizacion de cada celda */
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        });

        // tableProducts.getColumns().add(editCol); // Añade la columna a la tabla
        tableProducts.getColumns().add(estadoCol); // Añade la columna a la tabla
        tableProducts.getColumns().add(detallesCol); // Añade la columna a la tabla
        cargarDatosDesdeBD(); // Carga los datos desde la base de datos

        btnBuscarProd.setOnAction(event -> buscarProducto());  
    }

    // Metodo que modifica el estado del producto
    private void cambiarEstadoProducto(Producto producto){
        boolean nuevoProducto = producto.getEstado() ? false : true;
        producto.setEstado(nuevoProducto);
        inventService.actualizarProducto(producto);
        cargarDatosDesdeBD();
    }

    private void verDetalles(Producto producto) {
        // dialog.setTitle("Detalles del producto");
        // dialog.setHeaderText("Nombre:" + producto.getNombre());
        
        // VBox content = new VBox(10);
        // content.setAlignment(Pos.CENTER);
        // Label descripcion = new Label("Descripcion: " + producto.getDescripcion());
        // Label precio = new Label("Precio: " + producto.getPrecio());
        // Label stock = new Label("Stock: " + producto.getStock());

        // content.getChildren().addAll(descripcion, precio, stock);
        // dialog.getDialogPane().setContent(content);
        // dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // dialog.showAndWait();
        // DetalleProductoController detalleProducto = new DetalleProductoController();
        // detalleProducto.verDetalles(producto);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/DetalleProducto.fxml"));
            Node view = loader.load();
    
            DetalleProductoController controller = loader.getController();
            controller.setProducto(producto); // Pasa el producto al controlador.
    
            mainContent.getChildren().clear();
            mainContent.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la vista de detalles");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void cargarDatosDesdeBD() {
        productos = inventService.loadProducts();
        tableProducts.setItems(productos);
    }

    @FXML
    private void buscarProducto(){
        String termino = buscarProducto.getText();  // Obtener el texto de búsqueda
        List<Producto> resultados = inventService.buscarProductoPorNombre(termino);  // Buscar productos por nombre

        ObservableList<Producto> productos = FXCollections.observableArrayList(resultados);  // Convertir la lista a ObservableList
        tableProducts.setItems(productos);
    }

    public void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void añadirProducto(){
        setView("/resources/FormInventario.fxml");
    }

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

    @FXML
    public void setView(String fxmlPath, Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Obtener el controlador de la vista cargada
            DetalleProductoController controller = loader.getController();
            
            // Establecer el producto en el formulario de edición
            controller.setProducto(producto);

            // Limpiar y cargar la nueva vista en mainContent
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

    // Metodo que recarga a la vista inicial de la gestion de clientes
    @FXML
    public void recarga() {
        setView("/resources/InventarioView.fxml");
    }
}