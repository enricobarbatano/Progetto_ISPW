package com.ispw.view.gui.fxml;

import java.time.LocalDate;
import java.time.ZoneId;
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

/**
 * Controller FXML per il flusso di prenotazione.
 *
 * RESPONSABILITÀ:
 * - mostrare campi e slot disponibili;
 * - leggere input utente da ListView, DatePicker, ComboBox e TextField;
 * - gestire il wizard grafico ricerca -> slot -> pagamento -> esito;
 * - delegare al graphic controller usando dati semplici.
 *
 * NON:
 * - crea bean;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce navigazione tramite navigator;
 * - contiene logica applicativa.
 *
 * Nota:
 * il file prenotazione.fxml contiene anche txtIdCampo nascosto.
 * Il campo viene aggiornato quando l'utente seleziona un campo dalla lista,
 * così l'id resta allineato allo stato grafico.
 */
public class PrenotazioneFXMLController {

    private static final String MSG_CONTROLLER_NON_DISPONIBILE =
            "Controller prenotazione non disponibile";

    private GUIGraphicControllerPrenotazione controller;
    private SessioneUtenteBean sessione;

    private int lastCampoId;
    private float lastImporto;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private VBox boxSearch;
    @FXML private VBox boxSlots;
    @FXML private VBox boxPagamento;
    @FXML private VBox boxEsito;

    @FXML private ListView<String> listCampi;
    @FXML private ListView<String> listSlots;

    @FXML private TextField txtIdCampo;
    @FXML private TextField txtDurata;
    @FXML private TextField txtMetodo;
    @FXML private TextField txtCredenziale;

    @FXML private ComboBox<String> comboOra;
    @FXML private DatePicker datePicker;

    @FXML private Label lblRiepilogo;
    @FXML private Label lblImporto;
    @FXML private Label lblEsitoPagamento;

    /**
     * Inizializza il controller FXML con il graphic controller e la sessione.
     *
     * @param controller controller grafico della prenotazione
     * @param sessione sessione utente corrente
     */
    public void init(GUIGraphicControllerPrenotazione controller,
                     SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Metodo chiamato automaticamente da JavaFX dopo il caricamento FXML.
     *
     * Imposta valori iniziali e listener grafici.
     */
    @FXML
    public void initialize() {
        setupDatePicker();
        setupComboOra();
        setupCampoSelectionListener();
        setupMetodoPagamentoDefault();
        showInitialStep();
    }

    /**
     * Renderizza lo stato della schermata usando i parametri ricevuti dal navigator.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearMessages();
            showInitialStep();
            return;
        }

        renderMessages(params);
        renderCampi(params);
        renderSlots(params);
        renderRiepilogo(params);
        renderPagamento(params);
        updateStepUI(params);
    }

    // =========================================================
    // EVENTI FXML
    // =========================================================

    /**
     * Richiede la lista dei campi disponibili.
     */
    @FXML
    public void onListaCampi() {
        clearMessages();

        if (controller != null) {
            controller.richiediListaCampi();
        }
    }

    /**
     * Cerca disponibilità in base a campo, data, ora e durata.
     */
    @FXML
    public void onCerca() {
        clearMessages();

        if (controller == null) {
            showError(MSG_CONTROLLER_NON_DISPONIBILE);
            return;
        }

        Integer idCampo = extractCampoId();
        String data = extractData();
        String ora = extractOra();
        Integer durata = extractDurata();

        if (idCampo == null || data == null || ora == null || durata == null) {
            return;
        }

        lastCampoId = idCampo;

        controller.cercaDisponibilita(
                idCampo,
                data,
                ora,
                durata
        );
    }

    /**
     * Seleziona lo slot scelto dall'utente e avvia la creazione della prenotazione.
     */
    @FXML
    public void onSelezionaSlot() {
        clearMessages();

        if (controller == null) {
            showError(MSG_CONTROLLER_NON_DISPONIBILE);
            return;
        }

        String slot = getSelectedSlot();

        if (slot == null) {
            showError("Seleziona uno slot");
            return;
        }

        PrenotazioneViewUtils.SlotInfo info = PrenotazioneViewUtils.parseSlot(slot);

        if (info == null) {
            showError("Formato slot non valido");
            return;
        }

        if (lastCampoId <= 0) {
            showError("Campo non selezionato correttamente");
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

    /**
     * Procede al pagamento della prenotazione.
     */
    @FXML
    public void onPaga() {
        clearMessages();

        if (controller == null) {
            showError(MSG_CONTROLLER_NON_DISPONIBILE);
            return;
        }

        String metodo = safeText(txtMetodo);
        String credenziale = safeText(txtCredenziale);

        if (metodo.isBlank() || credenziale.isBlank()) {
            showError("Dati pagamento mancanti");
            return;
        }

        if (lastImporto <= 0f) {
            showError("Importo pagamento non valido");
            return;
        }

        controller.procediAlPagamento(
                metodo,
                credenziale,
                lastImporto,
                sessione
        );
    }

    /**
     * Reset del wizard di prenotazione.
     */
    @FXML
    public void onReset() {
        clearMessages();

        lastCampoId = 0;
        lastImporto = 0f;

        clearTextField(txtIdCampo);
        clearTextField(txtDurata);
        clearTextField(txtCredenziale);

        if (txtMetodo != null) {
            txtMetodo.setText("PAYPAL");
        }

        if (datePicker != null) {
            datePicker.setValue(LocalDate.now(ZoneId.systemDefault()));
        }

        if (comboOra != null && !comboOra.getItems().isEmpty()) {
            comboOra.getSelectionModel().selectFirst();
        }

        if (listSlots != null) {
            listSlots.getItems().clear();
        }

        showInitialStep();
    }

    /**
     * Torna dallo step pagamento allo step slot.
     */
    @FXML
    public void onIndietroSlots() {
        clearMessages();

        show(boxSearch, false);
        show(boxSlots, true);
        show(boxPagamento, false);
        show(boxEsito, false);
    }

    /**
     * Torna alla home.
     */
    @FXML
    public void onHome() {
        if (controller != null) {
            controller.tornaAllaHome();
        }
    }

    // =========================================================
    // SETUP INIZIALE
    // =========================================================

    /**
     * Configura il DatePicker per usare solo il calendario.
     */
    private void setupDatePicker() {
        if (datePicker == null) {
            return;
        }

        datePicker.setEditable(false);

        if (datePicker.getValue() == null) {
            datePicker.setValue(LocalDate.now(ZoneId.systemDefault()));
        }
    }

    /**
     * Configura la ComboBox degli orari.
     */
    private void setupComboOra() {
        if (comboOra != null && !comboOra.getItems().isEmpty()) {
            comboOra.getSelectionModel().selectFirst();
        }
    }

    /**
     * Imposta il valore di default del metodo di pagamento.
     */
    private void setupMetodoPagamentoDefault() {
        if (txtMetodo != null && safeText(txtMetodo).isBlank()) {
            txtMetodo.setText("PAYPAL");
        }
    }

    /**
     * Aggiorna lo stato locale quando l'utente seleziona un campo dalla lista.
     */
    private void setupCampoSelectionListener() {
        if (listCampi == null) {
            return;
        }

        listCampi.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> handleCampoSelection(newValue));
    }

    /**
     * Gestisce la selezione di un campo.
     *
     * @param selectedItem elemento selezionato dalla lista campi
     */
    private void handleCampoSelection(String selectedItem) {
        Integer id = parseCampoId(selectedItem);

        if (id == null) {
            return;
        }

        lastCampoId = id;

        if (txtIdCampo != null) {
            txtIdCampo.setText(String.valueOf(id));
        }

        clearMessages();
    }

    // =========================================================
    // RENDER HELPERS
    // =========================================================

    /**
     * Renderizza messaggi di errore e successo.
     */
    private void renderMessages(Map<String, Object> params) {
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);

        if (ok == null) {
            ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
        }

        if (ok == null) {
            ok = params.get(GraphicControllerUtils.KEY_MESSAGGIO);
        }

        setLabelText(lblError, err);
        setLabelText(lblSuccess, ok);
    }

    /**
     * Renderizza la lista campi.
     */
    private void renderCampi(Map<String, Object> params) {
        Object raw = params.get(GraphicControllerUtils.KEY_CAMPI);

        if (!(raw instanceof List<?> campi) || listCampi == null) {
            return;
        }

        listCampi.getItems().setAll(
                campi.stream()
                        .map(Object::toString)
                        .toList()
        );
    }

    /**
     * Renderizza gli slot disponibili.
     */
    private void renderSlots(Map<String, Object> params) {
        Object raw = params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI);

        if (!(raw instanceof List<?> slots) || listSlots == null) {
            return;
        }

        listSlots.getItems().setAll(
                slots.stream()
                        .map(Object::toString)
                        .toList()
        );

        if (listSlots.getItems().isEmpty()) {
            showError("Nessuno slot disponibile per i dati selezionati");
        }
    }

    /**
     * Renderizza il riepilogo della prenotazione.
     */
    private void renderRiepilogo(Map<String, Object> params) {
        Object raw = params.get(GraphicControllerUtils.KEY_RIEPILOGO);

        if (!(raw instanceof Map<?, ?> riepilogo)) {
            return;
        }

        Object testo = riepilogo.get(GraphicControllerUtils.KEY_RIEPILOGO);
        Object importo = riepilogo.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);

        setLabelText(lblRiepilogo, testo);

        if (importo instanceof Number n) {
            lastImporto = n.floatValue();
        }

        if (lblImporto != null) {
            lblImporto.setText(String.format("%.2f EUR", lastImporto));
        }
    }

    /**
     * Renderizza l'esito del pagamento.
     */
    private void renderPagamento(Map<String, Object> params) {
        Object raw = params.get(GraphicControllerUtils.KEY_PAGAMENTO);

        if (!(raw instanceof Map<?, ?> pagamento)) {
            return;
        }

        String esito = PrenotazioneViewUtils.formatEsitoPagamento(
                pagamento.get(GraphicControllerUtils.KEY_SUCCESSO),
                pagamento.get(GraphicControllerUtils.KEY_STATO),
                pagamento.get(GraphicControllerUtils.KEY_MESSAGGIO)
        );

        if (lblEsitoPagamento != null) {
            lblEsitoPagamento.setText(esito);
        }
    }

    /**
     * Aggiorna lo step visibile del wizard.
     */
    private void updateStepUI(Map<String, Object> params) {
        boolean hasSlots = params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) != null;
        boolean hasRiepilogo = params.get(GraphicControllerUtils.KEY_RIEPILOGO) != null;
        boolean hasPagamento = params.get(GraphicControllerUtils.KEY_PAGAMENTO) != null;

        show(boxSearch, !hasSlots && !hasRiepilogo && !hasPagamento);
        show(boxSlots, hasSlots && !hasRiepilogo && !hasPagamento);
        show(boxPagamento, hasRiepilogo && !hasPagamento);
        show(boxEsito, hasPagamento);
    }

    // =========================================================
    // ESTRAZIONE INPUT
    // =========================================================

    private Integer extractCampoId() {
        if (lastCampoId > 0) {
            return lastCampoId;
        }

        Integer fromText = parsePositiveInt(safeText(txtIdCampo));

        if (fromText != null) {
            return fromText;
        }

        String selected = listCampi != null
                ? listCampi.getSelectionModel().getSelectedItem()
                : null;

        Integer fromList = parseCampoId(selected);

        if (fromList == null) {
            showError("Seleziona un campo");
        }

        return fromList;
    }

    private String extractData() {
        if (datePicker == null || datePicker.getValue() == null) {
            showError("Seleziona una data");
            return null;
        }

        return datePicker.getValue().toString();
    }

    private String extractOra() {
        String ora = comboOra != null ? comboOra.getValue() : null;

        if (ora == null || ora.isBlank()) {
            showError("Seleziona un orario");
            return null;
        }

        return ora.trim();
    }

    private Integer extractDurata() {
        Integer durata = parsePositiveInt(safeText(txtDurata));

        if (durata == null) {
            showError("Durata non valida");
        }

        return durata;
    }

    private String getSelectedSlot() {
        if (listSlots == null) {
            return null;
        }

        return listSlots.getSelectionModel().getSelectedItem();
    }

    // =========================================================
    // PARSING E UTILITY
    // =========================================================

    private Integer parseCampoId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim();

        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        String firstPart = normalized.split("-")[0].trim();
        String numericPart = firstPart.replaceAll("\\D", "");

        return parsePositiveInt(numericPart);
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void show(VBox box, boolean visible) {
        if (box != null) {
            box.setVisible(visible);
            box.setManaged(visible);
        }
    }

    private void showInitialStep() {
        show(boxSearch, true);
        show(boxSlots, false);
        show(boxPagamento, false);
        show(boxEsito, false);
    }

    private String safeText(TextField field) {
        return field != null && field.getText() != null
                ? field.getText().trim()
                : "";
    }

    private void clearTextField(TextField field) {
        if (field != null) {
            field.clear();
        }
    }

    private void setLabelText(Label label, Object value) {
        if (label != null) {
            label.setText(value != null ? value.toString() : "");
        }
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
        }

        if (lblSuccess != null) {
            lblSuccess.setText("");
        }
    }

    private void clearMessages() {
        if (lblError != null) {
            lblError.setText("");
        }

        if (lblSuccess != null) {
            lblSuccess.setText("");
        }
    }
}

