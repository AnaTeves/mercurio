package app.Controllers;
import app.BDD.ClienteService;
import app.BDD.InventService;
import app.BDD.VentaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import app.Models.Cliente;
import java.time.LocalDate;
import app.Models.DetalleVenta;
import app.Models.Producto;
import app.Models.Venta;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import java.time.LocalDateTime;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Controlador de ventas
public class VentaController {
    @FXML
    private DatePicker fechaPicker;
    @FXML
    private TextField dniUsuario;
    @FXML
    private ComboBox<Producto> comboBoxProductos;
    @FXML
    private ComboBox<Cliente> comboBoxClientes;
    @FXML
    private TextField cantidad;
    @FXML
    private TableView<DetalleVenta> tablaDetalleVenta;
    @FXML
    private TableColumn<DetalleVenta, String> colProducto;
    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML
    private TableColumn<DetalleVenta, Integer> colIDProducto;
    @FXML
    private TableColumn<DetalleVenta, Float> colSubtotal;
    @FXML
    private TableColumn<DetalleVenta, Float> colTotalVenta;
    private float totalAcumulado = 0.0f;
    @FXML
    private TextField totalVenta;
    @FXML
    private Button registrarVentaBtn;
    @FXML
    private Button agregarProductoBtn;
    @FXML
    private TextField buscarCliente;
    @FXML
    private TextField buscarProducto;
    TableView<Producto> tableView = new TableView<>();

    // NEW 
    @FXML
    private TextField campoCliente;

    private ObservableList<DetalleVenta> detallesVenta = FXCollections.observableArrayList();
    private float totalProducto = 0.0f;
    private VentaService ventaService = new VentaService();
    private ClienteService clienteService = new ClienteService();
    private InventService inventService = new InventService();
    Dialog<String> dialog = new Dialog<>();
    CustomAlert customAlert = new CustomAlert();

    @FXML
    public void initialize() {
        // colIDProducto.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        // colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        // colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        // colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));;

        // tablaDetalleVenta.setItems(detallesVenta);    TEMPORALMENTE COMENTADO
    }

    // Metodo que se ejecuta al presionar el boton buscar
    public void buscarCliente(){
        String dni = buscarCliente.getText(); // Extraigo el dni ingresado en el buscador
        if(dni.isEmpty()){ // Si es vacio
            customAlert.mostrarAlertaPersonalizada("Error", "Ingrese un DNI para buscar el cliente.");
        } else {
            Optional<Cliente> cliente = clienteService.buscarPorDni(dni);  // Busco el cliente en mi base de datos

            if (cliente.isPresent()) { // Si se encuentra el cliente
                mostrarSeleccion(cliente.get()); // Muestro la seleccion del cliente
            } else {
                mostrarRegistro(dni); // Pregunto si deseo registrarlo
            }
        }
    }

    private void mostrarSeleccion(Cliente cliente){
        dialog.setTitle("Seleccionar cliente");
        dialog.setHeaderText("¿Desea seleccionar el cliente " + cliente.getNombre() + "?");

        // Configuracion del contenido
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        Label lblNombre = new Label("Nombre: " + cliente.getNombre());
        Label lblDni = new Label("DNI: " + cliente.getDni());
        Button btnSeleccionar = new Button("Seleccionar");

        content.getChildren().addAll(lblNombre, lblDni, btnSeleccionar);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Acción al seleccionar
        btnSeleccionar.setOnAction(event -> {
            buscarCliente.setText(cliente.getDni());
            dialog.close();
        });

        dialog.showAndWait();
    }

    private void mostrarRegistro(String dni) {
        dialog.setTitle("Cliente no encontrado.");
        dialog.setHeaderText("El cliente con DNI " + dni + " no se encuentra registrado.");

        // Configuracion del contenido
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        Label lblMensajei = new Label("¿Desea registrar un nuevo cliente?");
        Button btnRegistrar = new Button("Registrar");

        content.getChildren().addAll(lblMensajei, btnRegistrar);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Acción al seleccionar
        btnRegistrar.setOnAction(event -> {
            abrirRegistro();
            dialog.close();
        });

        dialog.showAndWait();
    }

    private void abrirRegistro(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/FormClient.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registrar Cliente");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarProducto() {
        String termino = buscarProducto.getText().trim();

        if(termino.isEmpty()){
            return;
        }

        List<Producto> productos = inventService.buscarProductoPorNombre(termino);

        if(productos.isEmpty()){
            return;
        }

        Optional<Producto> productoSeleccionado = mostrarDialogoSeleccion(productos);

        productoSeleccionado.ifPresent(producto -> {
            buscarProducto.setText(producto.getNombre());
        });
    }

    public Optional<Producto> mostrarDialogoSeleccion(List<Producto> productos) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Producto");
        dialog.setHeaderText("Seleccione un producto de la lista");

        // Crear tabla para mostrar productos
        TableColumn<Producto, Integer> colId = new TableColumn<>("ID");
        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        TableColumn<Producto, Float> colPrecio = new TableColumn<>("Precio");
        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        tableView.getColumns().addAll(List.of(colId, colNombre, colPrecio, colStock));
        tableView.setItems(FXCollections.observableArrayList(productos));

        // Configurar contenido del dialog
        VBox content = new VBox(tableView);
        content.setSpacing(10);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        // Botones del dialog
        ButtonType seleccionarButtonType = new ButtonType("Seleccionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(seleccionarButtonType, ButtonType.CANCEL);

        // Obtener producto seleccionado al presionar "Seleccionar"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == seleccionarButtonType) {
                return tableView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // Mostrar el dialog y esperar respuesta
        return dialog.showAndWait();
    }


    // private void cargarDatosDesdeBD() {
    //     detallesVenta = ventaService.loadVentas();
    //     tablaDetalleVenta.setItems(detallesVenta);
    // }

    // Metodo que se ejecuta al presionar el boton "Agregar"
    @FXML
    private void agregarProducto() {
        try{
            String cliente = buscarCliente.getText();
            Producto productoSeleccionado = tableView.getSelectionModel().getSelectedItem();

        if (cliente.isEmpty() || productoSeleccionado == null) {
            mostrarAlerta("Error", "Por favor complete todos los campos.");
            return;
        }
        // Crear un nuevo detalle de venta
        DetalleVenta detalle = new DetalleVenta(productoSeleccionado.getNombre(), 1, productoSeleccionado.getPrecio(), productoSeleccionado.getId());

        // Agregar detalle a la lista y actualizar tabla
        detallesVenta.add(detalle);

        totalProducto += detalle.getSubtotal();
        totalAcumulado += totalProducto;

        totalVenta.setText(String.format("%.2f", totalAcumulado));

        tablaDetalleVenta.getItems().add(detalle);
        tablaDetalleVenta.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Complete todos los campos.");
        }
    }

    @FXML
    private void registrarVenta() {

        try {
            // Validar campos de entrada
            LocalDate fechaVenta = fechaPicker.getValue();
            Cliente clienteSeleccionado = comboBoxClientes.getSelectionModel().getSelectedItem();
            String dni_Usuario = dniUsuario.getText();

            String dniCliente = clienteSeleccionado.getDni();

            LocalDateTime localDate = fechaVenta.atStartOfDay();
            Timestamp timestamp = Timestamp.valueOf(localDate);
            
            if (fechaVenta == null || dniCliente.isEmpty() || dni_Usuario.isEmpty()) {
                // Mostrar mensaje de error si hay campos vacíos
                mostrarAlerta("Error", "Por favor complete todos los campos.");
                return;
            }

            // Obtener los IDs de cliente y usuario a partir de sus DNI
            int idCliente = ventaService.obtenerIdCliente(dniCliente);
            int idUsuario = ventaService.obtenerIdUsuario(dni_Usuario);

            if (idCliente == -1 || idUsuario == -1) {
                mostrarAlerta("Error", "Cliente o usuario no encontrados.");
                return;
            }

            for(DetalleVenta detalle : detallesVenta){
                ventaService.descontarStock(detalle.getId_producto(), detalle.getCantidad());
            }

            // Crear el objeto Venta y registrar en base de datos
            Venta nuevaVenta = new Venta(timestamp, totalAcumulado, idUsuario, idCliente);
            System.out.println(nuevaVenta);
            ventaService.registrarVenta(nuevaVenta, detallesVenta);

            mostrarAlerta("Éxito", "Venta registrada con éxito.");
            limpiarCampos();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al registrar la venta.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        dniUsuario.clear();
        detallesVenta.clear();
        totalAcumulado = 0.0f;
        tablaDetalleVenta.getItems().clear();
    }



    public void openTheWindowClients(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/ClientsList.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Seleccionar Cliente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Obtener el cliente seleccionado de la ventana emergente
            ClientsListController controller = loader.getController();
            String clienteSeleccionado = controller.getClienteSeleccionado();
            
            if (clienteSeleccionado != null) {
                campoCliente.setText(clienteSeleccionado);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}