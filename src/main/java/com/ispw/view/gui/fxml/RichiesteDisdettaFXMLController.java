package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la gestione delle richieste di disdetta lato gestore.
 *
 * RESPONSABILITÀ:
 * - mostrare la lista delle richieste di disdetta pending;
 * - leggere la richiesta selezionata dalla lista;
 * - raccogliere una nota opzionale del gestore;
 * - delegare approvazione e rifiuto al graphic controller.
 *
 * NON:
 * - crea bean;
 * - crea Map per il logic layer;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce direttamente la navigazione.
 *
 * Nota:
 * la formattazione delle richieste è responsabilità della view.
 * Il controller grafico può continuare a passare stringhe ottenute dal bean/entity,
 * mentre questo controller FXML le rende leggibili per l'utente.
 */
public class RichiesteDisdettaFXMLController {

    private static final Pattern ID_RICHIESTA_PATTERN =
            Pattern.compile("Richiesta\\s*#?\\s*(\\d+)");

    private static final Pattern ID_PRENOTAZIONE_PATTERN =
            Pattern.compile("pren\\s*#?\\s*(\\d+)");

    private static final Pattern ID_UTENTE_PATTERN =
            Pattern.compile("utente\\s*#?\\s*(\\d+)");

    private static final Pattern STATO_PATTERN =
            Pattern.compile("stato=([^\\s]+)");

    private static final Pattern PENALE_PATTERN =
            Pattern.compile("penale=(\\d+(?:[\\.,]\\d+)?)");

    private static final Pattern RIMBORSO_PATTERN =
            Pattern.compile("rimborso=(\\d+(?:[\\.,]\\d+)?)");

    private static final Pattern DATA_RICHIESTA_PATTERN =
            Pattern.compile("richiesta@([^\\s]+)");

    private GUIGraphicControllerRichiesteDisdetta controller;
    private SessioneUtenteBean sessione;

    private Integer selectedId;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listRichieste;
    @FXML private TextField txtIdRichiesta;
    @FXML private TextArea txtNotaGestore;

    /**
     * Metodo chiamato automaticamente da JavaFX dopo il caricamento FXML.
     */
    @FXML
    public void initialize() {
        setupRichiestaSelectionListener();
        setupRichiesteCellFactory();
    }

    /**
     * Inizializza il controller FXML con il graphic controller e la sessione.
     *
     * @param controller controller grafico delle richieste disdetta
     * @param sessione sessione del gestore corrente
     */
    public void init(GUIGraphicControllerRichiesteDisdetta controller,
                     SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Renderizza i dati ricevuti dal navigator.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearMessages();
            return;
        }

        renderMessages(params);

        if (params.containsKey(GraphicControllerUtils.KEY_RICHIESTE)) {
            renderRichieste(params.get(GraphicControllerUtils.KEY_RICHIESTE));
        }

        restoreSelection();
    }

    // =========================================================
    // EVENTI FXML
    // =========================================================

    /**
     * Carica le richieste pending.
     */
    @FXML
    public void onRicarica() {
        clearMessages();

        if (controller != null) {
            controller.caricaRichiestePending(sessione);
        }
    }

    /**
     * Approva la richiesta selezionata.
     */
    @FXML
    public void onApprova() {
        processDecision(true);
    }

    /**
     * Rifiuta la richiesta selezionata.
     */
    @FXML
    public void onRifiuta() {
        processDecision(false);
    }

    /**
     * Torna alla home.
     */
    @FXML
    public void onHome() {
        clearMessages();

        if (controller != null) {
            controller.tornaAllaHome();
        }
    }

    // =========================================================
    // LOGICA EVENTI UI
    // =========================================================

    /**
     * Esegue approvazione o rifiuto della richiesta selezionata.
     *
     * @param approved true per approvare, false per rifiutare
     */
    private void processDecision(boolean approved) {
        clearMessages();

        if (controller == null) {
            showError("Controller richieste disdetta non disponibile");
            return;
        }

        Integer idRichiesta = resolveIdRichiesta();

        if (idRichiesta == null) {
            showError("Id richiesta non valido: seleziona una richiesta dalla lista");
            return;
        }

        String nota = safeText(txtNotaGestore);

        if (approved) {
            controller.approva(idRichiesta, nota, sessione);
        } else {
            controller.rifiuta(idRichiesta, nota, sessione);
        }
    }

    // =========================================================
    // SETUP UI
    // =========================================================

    /**
     * Collega la selezione della ListView all'id richiesta corrente.
     */
    private void setupRichiestaSelectionListener() {
        if (listRichieste == null) {
            return;
        }

        listRichieste.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> handleRichiestaSelection(newValue));
    }

    /**
     * Imposta la cell factory della lista.
     *
     * Serve per mostrare ogni richiesta su più righe,
     * evitando la stringa tecnica lunga e la scrollbar orizzontale.
     */
    private void setupRichiesteCellFactory() {
        if (listRichieste == null) {
            return;
        }

        listRichieste.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item);
                setWrapText(true);
                setStyle("-fx-padding: 12 16; -fx-font-size: 14px;");
            }
        });
    }

    /**
     * Gestisce la selezione di una richiesta.
     *
     * @param selectedItem riga selezionata
     */
    private void handleRichiestaSelection(String selectedItem) {
        Integer id = parseIdFromRichiestaString(selectedItem);

        if (id == null) {
            return;
        }

        selectedId = id;

        if (txtIdRichiesta != null) {
            txtIdRichiesta.setText(String.valueOf(id));
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
     * Renderizza la lista delle richieste pending.
     *
     * Qui avviene la conversione dalla stringa grezza del bean/entity
     * a una stringa leggibile per la ListView.
     */
    private void renderRichieste(Object rawRichieste) {
        if (listRichieste == null) {
            return;
        }

        listRichieste.getItems().clear();

        if (rawRichieste instanceof List<?> richieste) {
            listRichieste.getItems().setAll(
                    richieste.stream()
                            .map(Object::toString)
                            .map(this::formatRichiestaForView)
                            .toList()
            );
        }

        if (listRichieste.getItems().isEmpty()) {
            listRichieste.getItems().add("(nessuna richiesta pending)");
        }
    }

    /**
     * Converte una richiesta grezza in testo leggibile.
     *
     * Esempio input:
     * Richiesta#23 pren#29 utente#5 stato=PENDING penale=0.00 rimborso=75.00 richiesta@2026-05-19T17:52:06
     *
     * Esempio output:
     * Richiesta #23
     * Prenotazione #29 · Utente #5
     * Stato: PENDING
     * Penale: 0.00 EUR · Rimborso: 75.00 EUR
     * Data richiesta: 2026-05-19 17:52:06
     */
    private String formatRichiestaForView(String raw) {
        if (raw == null || raw.isBlank() || raw.startsWith("(")) {
            return raw;
        }

        String idRichiesta = extract(raw, ID_RICHIESTA_PATTERN, "-");
        String idPrenotazione = extract(raw, ID_PRENOTAZIONE_PATTERN, "-");
        String idUtente = extract(raw, ID_UTENTE_PATTERN, "-");
        String stato = extract(raw, STATO_PATTERN, "-");
        String penale = formatMoney(extract(raw, PENALE_PATTERN, "0"));
        String rimborso = formatMoney(extract(raw, RIMBORSO_PATTERN, "0"));
        String dataRichiesta = formatDateTime(extract(raw, DATA_RICHIESTA_PATTERN, "-"));

        return String.format(
                "Richiesta #%s%nPrenotazione #%s · Utente #%s%nStato: %s%nPenale: %s EUR · Rimborso: %s EUR%nData richiesta: %s",
                idRichiesta,
                idPrenotazione,
                idUtente,
                stato,
                penale,
                rimborso,
                dataRichiesta
        );
    }

    /**
     * Ripristina la selezione visiva nella lista.
     */
    private void restoreSelection() {
        if (selectedId == null || selectedId <= 0 || listRichieste == null) {
            return;
        }

        int index = findIndexById(selectedId, listRichieste.getItems());

        if (index >= 0) {
            listRichieste.getSelectionModel().select(index);
        }
    }

    // =========================================================
    // INPUT HELPERS
    // =========================================================

    /**
     * Risolve l'id richiesta usando lo stato locale o il campo testo.
     */
    private Integer resolveIdRichiesta() {
        if (selectedId != null && selectedId > 0) {
            return selectedId;
        }

        return parsePositiveInt(safeText(txtIdRichiesta));
    }

    /**
     * Estrae l'id richiesta sia dal vecchio formato grezzo
     * sia dal nuovo formato visuale.
     */
    private Integer parseIdFromRichiestaString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Matcher matcher = ID_RICHIESTA_PATTERN.matcher(raw);

        if (!matcher.find()) {
            return null;
        }

        return parsePositiveInt(matcher.group(1));
    }

    /**
     * Cerca nella lista l'indice della richiesta con l'id indicato.
     */
    private int findIndexById(int id, List<String> items) {
        if (items == null) {
            return -1;
        }

        for (int i = 0; i < items.size(); i++) {
            Integer parsed = parseIdFromRichiestaString(items.get(i));

            if (parsed != null && parsed == id) {
                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // FORMAT HELPERS
    // =========================================================

    /**
     * Estrae il primo gruppo catturato da un pattern.
     */
    private String extract(String raw, Pattern pattern, String fallback) {
        Matcher matcher = pattern.matcher(raw);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return fallback;
    }

    /**
     * Formatta un importo con due decimali.
     */
    private String formatMoney(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw)) {
            return "0.00";
        }

        try {
            double value = Double.parseDouble(raw.replace(',', '.'));
            return String.format("%.2f", value);
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * Rende leggibile una data ISO sostituendo la T con uno spazio.
     */
    private String formatDateTime(String raw) {
        if (raw == null || raw.isBlank() || "-".equals(raw)) {
            return "-";
        }

        return raw.replace('T', ' ');
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
     * Restituisce testo pulito da una TextArea.
     */
    private String safeText(TextArea area) {
        return area != null && area.getText() != null
                ? area.getText().trim()
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