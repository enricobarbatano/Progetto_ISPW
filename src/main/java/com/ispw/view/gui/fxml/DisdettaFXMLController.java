package com.ispw.view.gui.fxml;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la gestione delle disdette lato utente.
 *
 * RESPONSABILITÀ:
 * - mostrare le prenotazioni cancellabili;
 * - leggere la prenotazione selezionata dalla lista;
 * - mostrare l'anteprima della disdetta;
 * - inviare la richiesta di disdetta al graphic controller.
 *
 * NON:
 * - crea bean;
 * - crea Map per il logic layer;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce direttamente il routing.
 *
 * Nota:
 * il caricamento della lista avviene tramite il pulsante "Aggiorna Lista",
 * cioè tramite onRicarica(). Non viene eseguito automaticamente dalla GUIView,
 * così si evitano loop di navigazione.
 */
public class DisdettaFXMLController implements Initializable {

    private static final Pattern ID_PRENOTAZIONE_PATTERN = Pattern.compile("#\\s*(\\d+)");

    private GUIGraphicControllerDisdetta controller;
    private SessioneUtenteBean sessione;

    /*
     * Stato locale della schermata.
     * selectedId rappresenta l'id prenotazione selezionato.
     * previewPossibile rappresenta l'ultima anteprima ricevuta.
     * La penale, invece, viene usata solo durante il render e quindi resta locale.
     */
    private Integer selectedId;
    private Boolean previewPossibile;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listPrenotazioni;
    @FXML private TextField txtIdPrenotazione;

    @FXML private Label lblAnteprima;

    /**
     * Metodo chiamato automaticamente da JavaFX dopo il caricamento FXML.
     *
     * Imposta il listener sulla ListView, così quando l'utente seleziona
     * una prenotazione viene aggiornato anche il campo txtIdPrenotazione.
     */
    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        setupPrenotazioneSelectionListener();
    }

    /**
     * Inizializza il controller FXML con il graphic controller e la sessione.
     *
     * @param controller controller grafico della disdetta
     * @param sessione sessione utente corrente
     */
    public void init(GUIGraphicControllerDisdetta controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Renderizza i dati ricevuti dal navigator.
     *
     * Il metodo aggiorna solo le sezioni presenti nel payload.
     * Questo evita di svuotare la lista quando arriva, ad esempio,
     * solo il payload dell'anteprima.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearMessages();
            return;
        }

        renderMessages(params);

        if (params.containsKey(GraphicControllerUtils.KEY_PRENOTAZIONI)) {
            renderPrenotazioni(params.get(GraphicControllerUtils.KEY_PRENOTAZIONI));
        }

        if (params.containsKey(GraphicControllerUtils.KEY_ANTEPRIMA)) {
            renderAnteprima(params.get(GraphicControllerUtils.KEY_ANTEPRIMA));
        }

        if (params.containsKey(GraphicControllerUtils.KEY_ID_PRENOTAZIONE)) {
            renderSelectedId(params.get(GraphicControllerUtils.KEY_ID_PRENOTAZIONE));
        }

        restoreSelection();
    }

    // =========================================================
    // EVENTI FXML
    // =========================================================

    /**
     * Richiede le prenotazioni cancellabili.
     *
     * Questo metodo è richiamato dal pulsante "Aggiorna Lista".
     */
    @FXML
    public void onRicarica() {
        clearMessages();

        if (controller != null) {
            controller.richiediPrenotazioniCancellabili(sessione);
        }
    }

    /**
     * Richiede l'anteprima della disdetta per la prenotazione selezionata.
     */
    @FXML
    public void onAnteprima() {
        clearMessages();

        if (controller == null) {
            showError("Controller disdetta non disponibile");
            return;
        }

        Integer idPrenotazione = resolveIdPrenotazione();

        if (idPrenotazione == null) {
            showError("ID non valido: seleziona dalla lista o inseriscilo manualmente.");
            return;
        }

        selectedId = idPrenotazione;
        controller.richiediAnteprimaDisdetta(idPrenotazione, sessione);
    }

    /**
     * Invia la richiesta di disdetta.
     *
     * La view passa al graphic controller soltanto l'id prenotazione
     * e la sessione. La logica applicativa resta nel layer sottostante.
     */
    @FXML
    public void onInviaRichiesta() {
        clearMessages();

        if (controller == null) {
            showError("Controller disdetta non disponibile");
            return;
        }

        Integer idPrenotazione = resolveIdPrenotazione();

        if (idPrenotazione == null) {
            showError("Seleziona una prenotazione valida.");
            return;
        }

        if (Boolean.FALSE.equals(previewPossibile)) {
            showError("Disdetta non consentita per questa prenotazione.");
            return;
        }

        selectedId = idPrenotazione;
        controller.confermaDisdetta(idPrenotazione, sessione);
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
    // SETUP UI
    // =========================================================

    /**
     * Collega la selezione della ListView all'id prenotazione corrente.
     */
    private void setupPrenotazioneSelectionListener() {
        if (listPrenotazioni == null) {
            return;
        }

        listPrenotazioni.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> handlePrenotazioneSelection(newValue));
    }

    /**
     * Gestisce la selezione di una prenotazione nella lista.
     *
     * @param selectedItem riga selezionata nella ListView
     */
    private void handlePrenotazioneSelection(String selectedItem) {
        Integer id = parseIdFromPrenotazioneString(selectedItem);

        if (id == null) {
            return;
        }

        selectedId = id;

        if (txtIdPrenotazione != null) {
            txtIdPrenotazione.setText(String.valueOf(id));
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

        setLabelText(lblError, err);
        setLabelText(lblSuccess, ok);
    }

    /**
     * Renderizza la lista delle prenotazioni cancellabili.
     */
    private void renderPrenotazioni(Object rawList) {
        if (listPrenotazioni == null) {
            return;
        }

        listPrenotazioni.getItems().clear();

        if (rawList instanceof List<?> prenotazioni) {
            listPrenotazioni.getItems().setAll(
                    prenotazioni.stream()
                            .map(Object::toString)
                            .toList()
            );
        }

        if (listPrenotazioni.getItems().isEmpty()) {
            listPrenotazioni.getItems().add("(nessuna prenotazione cancellabile)");
        }
    }

    /**
     * Renderizza l'anteprima della disdetta.
     */
    private void renderAnteprima(Object rawAnteprima) {
        previewPossibile = null;
        float previewPenale = 0.0f;

        if (!(rawAnteprima instanceof Map<?, ?> anteprima)) {
            setLabelText(lblAnteprima, "");
            return;
        }

        Object possibile = anteprima.get(GraphicControllerUtils.KEY_POSSIBILE);
        Object penale = anteprima.get(GraphicControllerUtils.KEY_PENALE);

        previewPossibile = possibile instanceof Boolean b ? b : null;

        if (penale instanceof Number n) {
            previewPenale = n.floatValue();
        }

        String possibileText = formatPreviewPossibile(previewPossibile);

        if (lblAnteprima != null) {
            lblAnteprima.setText(
                    String.format("Disdetta possibile: %s | Penale: %.2f EUR",
                            possibileText,
                            previewPenale)
            );
        }
    }

    /**
     * Converte l'esito booleano dell'anteprima in testo leggibile.
     */
    private String formatPreviewPossibile(Boolean possibile) {
        if (possibile == null) {
            return "-";
        }

        if (Boolean.TRUE.equals(possibile)) {
            return "Sì";
        }

        return "No";
    }

    /**
     * Renderizza l'id prenotazione selezionato da payload.
     */
    private void renderSelectedId(Object rawId) {
        if (!(rawId instanceof Number number)) {
            return;
        }

        int id = number.intValue();

        if (id <= 0) {
            return;
        }

        selectedId = id;

        if (txtIdPrenotazione != null) {
            txtIdPrenotazione.setText(String.valueOf(id));
        }
    }

    /**
     * Ripristina la selezione visiva nella lista, se possibile.
     */
    private void restoreSelection() {
        if (selectedId == null || selectedId <= 0 || listPrenotazioni == null) {
            return;
        }

        int index = findIndexById(selectedId, listPrenotazioni.getItems());

        if (index >= 0) {
            listPrenotazioni.getSelectionModel().select(index);
        }
    }

    // =========================================================
    // INPUT HELPERS
    // =========================================================

    /**
     * Risolve l'id prenotazione usando prima lo stato locale,
     * poi il TextField e infine la selezione della lista.
     */
    private Integer resolveIdPrenotazione() {
        if (selectedId != null && selectedId > 0) {
            return selectedId;
        }

        Integer manualId = parsePositiveInt(safeText(txtIdPrenotazione));

        if (manualId != null) {
            return manualId;
        }

        String selected = listPrenotazioni != null
                ? listPrenotazioni.getSelectionModel().getSelectedItem()
                : null;

        return parseIdFromPrenotazioneString(selected);
    }

    /**
     * Estrae l'id da stringhe che contengono un formato tipo "#25".
     */
    private Integer parseIdFromPrenotazioneString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Matcher matcher = ID_PRENOTAZIONE_PATTERN.matcher(raw);

        if (!matcher.find()) {
            return null;
        }

        return parsePositiveInt(matcher.group(1));
    }

    /**
     * Converte una stringa in intero positivo.
     */
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

    /**
     * Cerca nella lista l'indice della prenotazione con l'id indicato.
     */
    private int findIndexById(int id, List<String> items) {
        if (items == null) {
            return -1;
        }

        for (int i = 0; i < items.size(); i++) {
            Integer parsed = parseIdFromPrenotazioneString(items.get(i));

            if (parsed != null && parsed == id) {
                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // UTILITY UI
    // =========================================================

    /**
     * Restituisce testo pulito da un TextField.
     */
    private String safeText(TextField field) {
        return field != null && field.getText() != null
                ? field.getText().trim()
                : "";
    }

    /**
     * Imposta testo su una Label.
     */
    private void setLabelText(Label label, Object value) {
        if (label != null) {
            label.setText(value != null ? value.toString() : "");
        }
    }

    /**
     * Mostra un messaggio di errore locale.
     */
    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
        }

        if (lblSuccess != null) {
            lblSuccess.setText("");
        }
    }

    /**
     * Pulisce i messaggi locali.
     */
    private void clearMessages() {
        if (lblError != null) {
            lblError.setText("");
        }

        if (lblSuccess != null) {
            lblSuccess.setText("");
        }
    }
}
