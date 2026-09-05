package app.Controllers;

import app.BDD.CajaService;
import app.Models.ArqueoCaja;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArqueoCajaController {

    @FXML private TableView<ArqueoCaja> tablaArqueo;
    @FXML private TableColumn<ArqueoCaja, Integer> colId;
    @FXML private TableColumn<ArqueoCaja, String> colVendedor;
    @FXML private TableColumn<ArqueoCaja, String> colDni;
    @FXML private TableColumn<ArqueoCaja, String> colApertura;
    @FXML private TableColumn<ArqueoCaja, String> colCierre;
    @FXML private TableColumn<ArqueoCaja, Double> colInicial;
    @FXML private TableColumn<ArqueoCaja, Double> colSistema;
    @FXML private TableColumn<ArqueoCaja, Double> colReal;
    @FXML private TableColumn<ArqueoCaja, Double> colDiferencia;
    @FXML private TableColumn<ArqueoCaja, String> colEstado;

    private CajaService cajaService = new CajaService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCaja"));
        colVendedor.setCellValueFactory(new PropertyValueFactory<>("nombreVendedor"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dniVendedor"));
        colApertura.setCellValueFactory(new PropertyValueFactory<>("fechaApertura"));
        colCierre.setCellValueFactory(new PropertyValueFactory<>("fechaCierre"));
        colInicial.setCellValueFactory(new PropertyValueFactory<>("montoInicial"));
        colSistema.setCellValueFactory(new PropertyValueFactory<>("montoSistema"));
        colReal.setCellValueFactory(new PropertyValueFactory<>("montoReal"));
        colDiferencia.setCellValueFactory(new PropertyValueFactory<>("diferencia"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Dar formato a la columna de Diferencia (Rojo = Faltante, Verde = Sobrante)
        colDiferencia.setCellFactory(column -> new TableCell<ArqueoCaja, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", item));
                    if (item < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item > 0) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        cargarTabla();
    }

    @FXML
    public void cargarTabla() {
        ObservableList<ArqueoCaja> lista = cajaService.obtenerHistorialArqueos();
        tablaArqueo.setItems(lista);
    }
}