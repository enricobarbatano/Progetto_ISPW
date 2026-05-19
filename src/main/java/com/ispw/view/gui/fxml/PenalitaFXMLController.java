package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la gestione delle penalità.
 *
 * RESPONSABILITÀ:
 * - mostrare la lista degli utenti selezionabili;
 * - leggere input utente da ListView e TextField;
 * - aggiornare i campi grafici della schermata;
 * - delegare l'applicazione della penalità al graphic controller.
 *
 * NON:
 * - crea bean;
 * - crea Map per il logic layer;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - contiene logica applicativa.
 *
 * Nota:
 * il file penalita.fxml richiama solo metodi handler della GUI.
 * Per questo motivo il controller FXML resta una view passiva:
 * raccoglie input e delega al graphic controller.
 */
public class PenalitaFXMLController {

    private GUIGraphicControllerPenalita controller;

    /*
     * Ultimo id selezionato dalla lista.
     * Serve perché txtIdUtente è disabilitato nel file FXML:
     * l'utente non lo modifica manualmente, ma il controller FXML
     * lo aggiorna quando viene selezionata una riga dalla lista.
     */
    private Integer selectedId;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listUtenti;

    @FXML private TextField txtIdUtente;
    @FXML private TextField txtImporto;
    @FXML private TextField txtMotivazione;

    /**
     * Inizializza i listener JavaFX dopo il caricamento del file FXML.
     *
     * Quando l'utente seleziona una riga nella lista, viene estratto l'id
     * e viene mostrato nel campo txtIdUtente.
     */
    @FXML
    public void initialize() {
        setupListSelectionListener();
    }

    /**
     * Inizializza il controller FXML con il graphic controller.
     *
     * Il parametro sessione è mantenuto nella firma per coerenza con le altre
     * schermate e con GUIPenalitaView, ma in questo caso non viene usato:
     * il caso d'uso applica penalità lavora sui dati selezionati nella view.
     *
     * @param controller controller grafico per la gestione penalità
     * @param sessione sessione corrente, non necessaria in questa schermata
     */
    @SuppressWarnings("java:S1172")
    public void init(GUIGraphicControllerPenalita controller, SessioneUtenteBean sessione) {
        this.controller = controller;
    }

    /**
     * Renderizza messaggi e lista utenti ricevuti dal navigator.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearMessages();
            return;
        }

        renderMessages(params);
        renderUtenti(params);
        restoreSelection();
    }

    // =========================================================
    // EVENTI FXML
    // =========================================================

    /**
     * Richiede la lista degli utenti a cui applicare una penalità.
     */
    @FXML
    public void onListaUtenti() {
        clearMessages();

        if (controller != null) {
            controller.richiediListaUtenti();
        }
    }

    /**
     * Applica la penalità all'utente selezionato.
     *
     * La view passa al graphic controller solo dati semplici:
     * id utente, importo e motivazione.
     */
    @FXML
    public void onApplica() {
        clearMessages();

        if (controller == null) {
            showError("Controller penalità non disponibile");
            return;
        }

        Integer idUtente = resolveIdUtente();
        Float importo = parsePositiveFloat(safeText(txtImporto));
        String motivazione = safeText(txtMotivazione);

        if (idUtente == null) {
            showError("Seleziona un utente dalla lista");
            return;
        }

        if (importo == null) {
            showError("Importo non valido");
            return;
        }

        if (motivazione.isBlank()) {
            showError("Motivazione obbligatoria");
            return;
        }

        controller.applicaPenalita(idUtente, importo, motivazione);
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
     * Collega la selezione della ListView all'aggiornamento dell'id utente.
     */
    private void setupListSelectionListener() {
        if (listUtenti == null) {
            return;
        }

        listUtenti.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> handleUserSelection(newValue));
    }

    /**
     * Gestisce la selezione di una riga nella lista utenti.
     *
     * @param selectedItem riga selezionata dalla ListView
     */
    private void handleUserSelection(String selectedItem) {
        Integer id = parseIdFromUtenteString(selectedItem);

        if (id == null) {
            return;
        }

        selectedId = id;

        if (txtIdUtente != null) {
            txtIdUtente.setText(String.valueOf(id));
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
     * Renderizza la lista degli utenti.
     */
    private void renderUtenti(Map<String, Object> params) {
        Object raw = params.get(GraphicControllerUtils.KEY_UTENTI);

        if (listUtenti == null) {
            return;
        }

        listUtenti.getItems().clear();

        if (raw instanceof List<?> utenti) {
            listUtenti.getItems().setAll(
                    utenti.stream()
                            .map(Object::toString)
                            .toList()
            );
        }
    }

    /**
     * Ripristina la selezione visiva dopo un nuovo render.
     */
    private void restoreSelection() {
        if (selectedId == null || selectedId <= 0 || listUtenti == null) {
            return;
        }

        int index = findIndexById(selectedId, listUtenti.getItems());

        if (index >= 0) {
            listUtenti.getSelectionModel().select(index);
        }
    }

    // =========================================================
    // INPUT HELPERS
    // =========================================================

    /**
     * Risolve l'id utente usando prima il TextField e poi lo stato selezionato.
     */
    private Integer resolveIdUtente() {
        Integer idFromText = parsePositiveInt(safeText(txtIdUtente));

        if (idFromText != null) {
            return idFromText;
        }

        return selectedId;
    }

    /**
     * Estrae l'id utente da stringhe del tipo:
     * - "#1 - email (RUOLO)"
     * - "1 - email (RUOLO)"
     */
    private Integer parseIdFromUtenteString(String raw) {
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
     * Converte una stringa in float positivo.
     *
     * Accetta sia il punto sia la virgola come separatore decimale.
     */
    private Float parsePositiveFloat(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            float value = Float.parseFloat(raw.trim().replace(',', '.'));
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Cerca nella lista l'indice dell'utente con l'id indicato.
     */
    private int findIndexById(int id, List<String> items) {
        if (items == null) {
            return -1;
        }

        for (int i = 0; i < items.size(); i++) {
            Integer parsed = parseIdFromUtenteString(items.get(i));

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