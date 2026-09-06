package app.Controllers;

import app.BDD.VentaService;
import app.Models.RendimientoVendedor;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class DetalleVendedoresController {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView<RendimientoVendedor> tablaVendedores;
    @FXML private TableColumn<RendimientoVendedor, String> colVendedor;
    @FXML private TableColumn<RendimientoVendedor, String> colDni;
    @FXML private TableColumn<RendimientoVendedor, Integer> colCantidadVentas;
    @FXML private TableColumn<RendimientoVendedor, Double> colTotalRecaudado;
    @FXML private TableColumn<RendimientoVendedor, Integer> colTotalArqueos;
    @FXML private TableColumn<RendimientoVendedor, Integer> colCierresDiferencia;

    private VentaService ventaService = new VentaService();
    private GerenteController gerenteController;

    @FXML
    public void initialize() {
        // Enlace de las columnas FXML con los campos de RendimientoVendedor
        colVendedor.setCellValueFactory(new PropertyValueFactory<>("vendedor"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colCantidadVentas.setCellValueFactory(new PropertyValueFactory<>("cantidadVentas"));
        colTotalRecaudado.setCellValueFactory(new PropertyValueFactory<>("totalRecaudado"));
        colTotalArqueos.setCellValueFactory(new PropertyValueFactory<>("totalArqueos"));
        colCierresDiferencia.setCellValueFactory(new PropertyValueFactory<>("cierresConDiferencia"));

        colTotalRecaudado.setCellFactory(tc -> new javafx.scene.control.TableCell<RendimientoVendedor, Double>() {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("$ %,.2f", item));
            }
        }
    });

        cargarDatos(null, null);
    }

    public void setGerenteController(GerenteController gerenteController) {
        this.gerenteController = gerenteController;
    }

    private void cargarDatos(LocalDate desde, LocalDate hasta) {
        tablaVendedores.setItems(ventaService.obtenerRendimientoVendedores(desde, hasta));
    }

    @FXML
    private void handleFiltrar() {
        cargarDatos(dpDesde.getValue(), dpHasta.getValue());
    }

    @FXML
    private void handleLimpiarFiltros() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        cargarDatos(null, null);
    }

    @FXML
    private void handleVolver() {
        if (gerenteController != null) {
            gerenteController.handleReports();
        }
    }
}