package com.ispw.view.gui.fxml;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class RegoleFXMLController {

    private GUIGraphicControllerRegole controller;
    private SessioneUtenteBean sessione;

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

    public void init(GUIGraphicControllerRegole controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    public void render(Map<String, Object> params) {
        Object err = params != null ? params.get(GraphicControllerUtils.KEY_ERROR) : null;
        Object ok = null;
        if (params != null) {
            ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
            if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
        }
        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        listCampi.getItems().clear();
        Object raw = params != null ? params.get(GraphicControllerUtils.KEY_CAMPI) : null;
        if (raw instanceof List<?> l) {
            for (Object o : l) listCampi.getItems().add(String.valueOf(o));
        }
    }

    @FXML private void onListaCampi() {
        clearLocalError();
        if (controller == null) return;
        controller.richiediListaCampi();
    }

    @FXML private void onSelezionaCampo() {
        clearLocalError();
        if (controller == null) return;
        Integer id = parsePositiveInt(txtIdCampo.getText());
        if (id == null) { lblError.setText("Id campo non valido"); return; }
        controller.selezionaCampo(id);
    }

    @FXML private void onAggiornaStato() {
        clearLocalError();
        if (controller == null) return;
        Integer id = parsePositiveInt(txtIdCampo.getText());
        if (id == null) { lblError.setText("Id campo non valido"); return; }

        Map<String, Object> payload = new HashMap<>();
        payload.put(GraphicControllerUtils.KEY_ID_CAMPO, id);
        payload.put(GraphicControllerUtils.KEY_ATTIVO, chkAttivo.isSelected());
        payload.put(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE, chkManutenzione.isSelected());
        controller.aggiornaStatoCampo(payload);
    }

    @FXML private void onAggiornaTempistiche() {
        clearLocalError();
        if (controller == null) return;

        Integer durata = parsePositiveInt(txtDurata.getText());
        Integer preavv = parseNonNegativeInt(txtPreavviso.getText(), null);
        if (durata == null || preavv == null) { lblError.setText("Durata/Preavviso non validi"); return; }

        try {
            LocalTime apertura = LocalTime.parse(txtApertura.getText().trim());
            LocalTime chiusura = LocalTime.parse(txtChiusura.getText().trim());

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI, durata);
            payload.put(GraphicControllerUtils.KEY_ORA_APERTURA, apertura);
            payload.put(GraphicControllerUtils.KEY_ORA_CHIUSURA, chiusura);
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, preavv);
            controller.aggiornaTempistiche(payload);

        } catch (RuntimeException ex) {
            lblError.setText("Formato ora non valido (HH:mm)");
        }
    }

    @FXML private void onAggiornaPenalita() {
        clearLocalError();
        if (controller == null) return;

        try {
            BigDecimal valore = new BigDecimal(txtValorePenalita.getText().trim());
            Integer preavv = parseNonNegativeInt(txtPreavvisoPenalita.getText(), 0);

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_VALORE_PENALITA, valore);
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, preavv);
            controller.aggiornaPenalita(payload);

        } catch (RuntimeException ex) {
            lblError.setText("Valore penalità non valido");
        }
    }

    @FXML private void onHome() {
        clearLocalError();
        if (controller == null) return;
        controller.tornaAllaHome();
    }

    private void clearLocalError() { lblError.setText(""); }

    private Integer parsePositiveInt(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;
        try {
            int v = Integer.parseInt(t);
            return v > 0 ? v : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseNonNegativeInt(String raw, Integer def) {
        if (raw == null) return def;
        String t = raw.trim();
        if (t.isEmpty()) return def;
        try {
            int v = Integer.parseInt(t);
            return v >= 0 ? v : def;
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
