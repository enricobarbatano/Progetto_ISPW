package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.view.shared.PrenotazioneViewUtils;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PrenotazioneFXMLController {

    // =========================================================
    // DEPENDENCIES
    // =========================================================
    private GUIGraphicControllerPrenotazione controller;
    private SessioneUtenteBean sessione;

    // =========================================================
    // STATE
    // =========================================================
    private int lastCampoId;
    private float lastImporto;

    // =========================================================
    // UI
    // =========================================================
    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listCampi;
    @FXML private ListView<String> listSlots;

    @FXML private TextField txtDurata;
    @FXML private TextField txtMetodo;
    @FXML private TextField txtCredenziale;

    @FXML private ComboBox<String> comboOra;
    @FXML private DatePicker datePicker;

    @FXML private Label lblRiepilogo;
    @FXML private Label lblImporto;
    @FXML private Label lblEsitoPagamento;

    // STEP BOX (IMPORTANTISSIMO per wizard)
    @FXML private VBox boxSearch;
    @FXML private VBox boxSlots;
    @FXML private VBox boxPagamento;
    @FXML private VBox boxEsito;

    // =========================================================
    // INIT
    // =========================================================
    public void init(GUIGraphicControllerPrenotazione controller,
                     SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Inizializzazione JavaFX
     */
    @FXML
    public void initialize() {

        // ✅ blocca input manuale data (evita null)
        if (datePicker != null) {
            datePicker.setEditable(false);
        }

        // ✅ seleziona default ora
        if (comboOra != null && comboOra.getItems() != null && !comboOra.getItems().isEmpty()) {
            comboOra.getSelectionModel().selectFirst();
        }
    }

    // =========================================================
    // RENDER
    // =========================================================
    public void render(Map<String, Object> params) {

        if (params == null) return;

        renderMessaggi(params);
        renderCampi(params);
        renderSlot(params);
        renderRiepilogo(params);
        renderPagamento(params);
        updateStepUI(params);
    }

    private void renderMessaggi(Map<String, Object> params) {
        lblError.setText(string(params.get(GraphicControllerUtils.KEY_ERROR)));
        lblSuccess.setText(string(params.get(GraphicControllerUtils.KEY_SUCCESSO)));
    }

    private void renderCampi(Map<String, Object> params) {
        if (params.get(GraphicControllerUtils.KEY_CAMPI) instanceof List<?> campi) {
            listCampi.getItems().setAll(
                    campi.stream().map(Object::toString).toList()
            );
        }
    }

    private void renderSlot(Map<String, Object> params) {
        if (params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) instanceof List<?> slots) {
            listSlots.getItems().setAll(
                    slots.stream().map(Object::toString).toList()
            );
        }
    }

    private void renderRiepilogo(Map<String, Object> params) {

        if (!(params.get(GraphicControllerUtils.KEY_RIEPILOGO) instanceof Map<?, ?> riep)) {
            return;
        }

        lblRiepilogo.setText(string(riep.get(GraphicControllerUtils.KEY_RIEPILOGO)));

        Object imp = riep.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);
        if (imp instanceof Number n) {
            lastImporto = n.floatValue();
        }

        lblImporto.setText(String.valueOf(lastImporto));
    }

    private void renderPagamento(Map<String, Object> params) {

        if (!(params.get(GraphicControllerUtils.KEY_PAGAMENTO) instanceof Map<?, ?> pay)) {
            return;
        }

        String esito = PrenotazioneViewUtils.formatEsitoPagamento(
                pay.get(GraphicControllerUtils.KEY_SUCCESSO),
                pay.get(GraphicControllerUtils.KEY_STATO),
                pay.get(GraphicControllerUtils.KEY_MESSAGGIO)
        );

        lblEsitoPagamento.setText(esito);
    }

    /**
     * Gestione wizard UI (MOSTRA STEP CORRETTO)
     */
    private void updateStepUI(Map<String, Object> params) {

        boolean hasSlots = params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) != null;
        boolean hasRiep  = params.get(GraphicControllerUtils.KEY_RIEPILOGO) != null;
        boolean hasPay   = params.get(GraphicControllerUtils.KEY_PAGAMENTO) != null;

        show(boxSearch, !hasSlots && !hasRiep && !hasPay);
        show(boxSlots, hasSlots && !hasRiep);
        show(boxPagamento, hasRiep && !hasPay);
        show(boxEsito, hasPay);
    }

    // =========================================================
    // EVENTI
    // =========================================================

    @FXML
    public void onListaCampi() {
        controller.richiediListaCampi();
    }

    @FXML
    public void onCerca() {

        clear();

        Integer idCampo = extractCampo();
        String data = extractData();
        String ora = extractOra();
        Integer durata = extractDurata();

        if (idCampo == null || data == null || ora == null || durata == null) {
            return;
        }

        lastCampoId = idCampo;

        controller.cercaDisponibilita(idCampo, data, ora, durata);
    }

    @FXML
    public void onSelezionaSlot() {

        String slot = listSlots.getSelectionModel().getSelectedItem();

        if (slot == null) {
            error("Seleziona uno slot");
            return;
        }

        var info = PrenotazioneViewUtils.parseSlot(slot);

        if (info == null) {
            error("Formato slot non valido");
            return;
        }

        controller.creaPrenotazione(
                lastCampoId,
                info.data(),
                info.oraInizio(),
                info.oraFine(),
                sessione
        );
    }

    @FXML
    public void onPaga() {

        String metodo = clean(txtMetodo);
        String cred = clean(txtCredenziale);

        if (metodo.isBlank() || cred.isBlank()) {
            error("Dati pagamento mancanti");
            return;
        }

        controller.procediAlPagamento(metodo, cred, lastImporto, sessione);
    }

    @FXML
    public void onReset() {

        lastCampoId = 0;
        lastImporto = 0;

        txtDurata.clear();
        txtMetodo.clear();
        txtCredenziale.clear();

        datePicker.setValue(null);
        comboOra.setValue(null);

        listSlots.getItems().clear();

        clear();

        // torna step iniziale
        show(boxSearch, true);
        show(boxSlots, false);
        show(boxPagamento, false);
        show(boxEsito, false);
    }

    @FXML
    public void onIndietroSlots() {
        show(boxSlots, true);
        show(boxPagamento, false);
    }

    @FXML
    public void onHome() {
        controller.tornaAllaHome();
    }

    // =========================================================
    // HELPERS INPUT
    // =========================================================

    private Integer extractCampo() {

        String selected = listCampi.getSelectionModel().getSelectedItem();

        if (selected == null) {
            error("Seleziona un campo");
            return null;
        }

        try {
            return Integer.parseInt(selected.split("-")[0].trim());
        } catch (NumberFormatException e) {
            error("Errore ID campo");
            return null;
        }
    }

    private String extractData() {
        if (datePicker.getValue() == null) {
            error("Seleziona una data");
            return null;
        }
        return datePicker.getValue().toString();
    }

    private String extractOra() {
        String ora = comboOra.getValue();
        if (ora == null) {
            error("Seleziona un orario");
            return null;
        }
        return ora;
    }

    private Integer extractDurata() {
        try {
            return Integer.parseInt(txtDurata.getText()); // ✅ minuti corretti
        } catch (NumberFormatException e) {
            error("Durata non valida");
            return null;
        }
    }

    // =========================================================
    // UTIL
    // =========================================================

    private void show(VBox box, boolean v) {
        if (box != null) {
            box.setVisible(v);
            box.setManaged(v);
        }
    }

    private void error(String msg) {
        lblError.setText(msg);
        lblSuccess.setText("");
    }

    private void clear() {
        lblError.setText("");
        lblSuccess.setText("");
    }

    private String clean(TextField f) {
        return (f != null && f.getText() != null) ? f.getText().trim() : "";
    }

    private String string(Object o) {
        return o != null ? o.toString() : "";
    }
}