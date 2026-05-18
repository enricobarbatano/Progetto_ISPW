package com.ispw.view.gui.fxml;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller FXML gestione regole.
 *
 * RESPONSABILITÀ:
 * - mostra lista campi
 * - raccoglie input utente
 * - delega controller
 */
public class RegoleFXMLController {

    private GUIGraphicControllerRegole controller;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listCampi;
    @FXML private TextField txtIdCampo;

    @FXML private CheckBox chkAttivo;
    @FXML private CheckBox chkManutenzione;

    @FXML private TextField txtDurata;
    @FXML private TextField txtApertura;
    @FXML private TextField txtChiusura;
    @FXML private TextField txtPreavviso;

    @FXML private TextField txtValorePenalita;
    @FXML private TextField txtPreavvisoPenalita;

    public void init(GUIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    public void render(Map<String, Object> params) {

        if (params == null) return;

        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok  = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? err.toString() : "");
        lblSuccess.setText(ok != null ? ok.toString() : "");

        if (params.get(GraphicControllerUtils.KEY_CAMPI) instanceof List<?> campi) {
            listCampi.getItems().setAll(
                    campi.stream().map(Object::toString).toList()
            );
        }
    }

    // =========================================================
    // EVENTI
    // =========================================================

    @FXML
    public void onListaCampi() {
        controller.richiediListaCampi();
    }

    @FXML
    public void onAggiornaStato() {

        Integer id = parseInt(txtIdCampo.getText());

        if (id == null) {
            lblError.setText("ID non valido");
            return;
        }

        controller.aggiornaStatoCampo(
                id,
                chkAttivo.isSelected(),
                chkManutenzione.isSelected()
        );
    }

    @FXML
    public void onAggiornaTempistiche() {

        try {
            int durata = Integer.parseInt(txtDurata.getText());
            int preavv = Integer.parseInt(txtPreavviso.getText());

            LocalTime apertura = LocalTime.parse(txtApertura.getText());
            LocalTime chiusura = LocalTime.parse(txtChiusura.getText());

            controller.aggiornaTempistiche(
                    preavv,
                    durata,
                    apertura,
                    chiusura
            );

        } catch (Exception e) {
            lblError.setText("Errore dati tempistiche");
        }
    }

    @FXML
    public void onAggiornaPenalita() {

        try {
            BigDecimal valore = new BigDecimal(txtValorePenalita.getText());
            int preavv = Integer.parseInt(txtPreavvisoPenalita.getText());

            controller.aggiornaPenalita(preavv, valore);

        } catch (Exception e) {
            lblError.setText("Errore penalità");
        }
    }

    @FXML
    public void onHome() {
        controller.tornaAllaHome();
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }
}