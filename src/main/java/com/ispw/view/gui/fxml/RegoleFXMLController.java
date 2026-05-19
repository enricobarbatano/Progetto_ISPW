package com.ispw.view.gui.fxml;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
 * Controller FXML per la gestione delle regole dei campi.
 *
 * RESPONSABILITÀ:
 * - mostrare la lista dei campi;
 * - leggere input utente da ListView, TextField e CheckBox;
 * - aggiornare i componenti grafici della schermata;
 * - delegare le operazioni al graphic controller.
 *
 * NON:
 * - crea bean;
 * - crea Map per il logic layer;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce direttamente la navigazione.
 *
 * Nota:
 * il metodo onSelezionaCampo() deve restare presente perché è richiamato
 * dal file regole.fxml tramite onMouseClicked="#onSelezionaCampo".
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

    /**
     * Inizializza il controller FXML con il graphic controller.
     *
     * @param controller controller grafico per la gestione regole
     */
    public void init(GUIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    /**
     * Renderizza messaggi e dati ricevuti dal navigator.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearMessages();
            return;
        }

        renderMessages(params);
        renderCampi(params);
        renderCampoSelezionato(params);
    }

    /**
     * Richiede la lista dei campi.
     */
    @FXML
    public void onListaCampi() {
        clearMessages();

        if (controller != null) {
            controller.richiediListaCampi();
        }
    }

    /**
     * Gestisce la selezione di un campo dalla ListView.
     *
     * Questo metodo è collegato al file FXML tramite onMouseClicked.
     */
    @FXML
    public void onSelezionaCampo() {
        clearMessages();

        if (listCampi == null) {
            return;
        }

        String selectedItem = listCampi.getSelectionModel().getSelectedItem();

        if (selectedItem == null || selectedItem.isBlank()) {
            return;
        }

        Integer idCampo = parseCampoId(selectedItem);

        if (idCampo == null) {
            showError("Errore nell'estrazione dell'ID campo");
            return;
        }

        if (txtIdCampo != null) {
            txtIdCampo.setText(String.valueOf(idCampo));
        }
    }

    /**
     * Aggiorna lo stato operativo del campo selezionato.
     */
    @FXML
    public void onAggiornaStato() {
        clearMessages();

        if (controller == null) {
            showError("Controller regole non disponibile");
            return;
        }

        Integer idCampo = parsePositiveInt(safeText(txtIdCampo));

        if (idCampo == null) {
            showError("ID campo non valido");
            return;
        }

        controller.aggiornaStatoCampo(
                idCampo,
                chkAttivo != null && chkAttivo.isSelected(),
                chkManutenzione != null && chkManutenzione.isSelected()
        );
    }

    /**
     * Aggiorna le tempistiche di prenotazione dei campi.
     */
    @FXML
    public void onAggiornaTempistiche() {
        clearMessages();

        if (controller == null) {
            showError("Controller regole non disponibile");
            return;
        }

        Integer durataSlot = parsePositiveInt(safeText(txtDurata));
        Integer preavviso = parseNonNegativeInt(safeText(txtPreavviso));

        if (durataSlot == null || preavviso == null) {
            showError("Durata o preavviso non validi");
            return;
        }

        try {
            LocalTime apertura = LocalTime.parse(safeText(txtApertura));
            LocalTime chiusura = LocalTime.parse(safeText(txtChiusura));

            controller.aggiornaTempistiche(
                    preavviso,
                    durataSlot,
                    apertura,
                    chiusura
            );

        } catch (DateTimeParseException e) {
            showError("Formato ora non valido. Usa HH:mm");
        }
    }

    /**
     * Aggiorna il valore della penalità e il relativo preavviso minimo.
     */
    @FXML
    public void onAggiornaPenalita() {
        clearMessages();

        if (controller == null) {
            showError("Controller regole non disponibile");
            return;
        }

        Integer preavviso = parseNonNegativeInt(safeText(txtPreavvisoPenalita));

        if (preavviso == null) {
            showError("Preavviso penalità non valido");
            return;
        }

        try {
            BigDecimal valore = new BigDecimal(safeText(txtValorePenalita));

            if (valore.compareTo(BigDecimal.ZERO) < 0) {
                showError("Valore penalità non valido");
                return;
            }

            controller.aggiornaPenalita(preavviso, valore);

        } catch (NumberFormatException e) {
            showError("Valore penalità non valido");
        }
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
     * Renderizza la lista dei campi.
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
     * Renderizza un eventuale campo selezionato tramite payload.
     */
    private void renderCampoSelezionato(Map<String, Object> params) {
        Object rawId = params.get(GraphicControllerUtils.KEY_ID_CAMPO);

        if (rawId instanceof Number number && txtIdCampo != null) {
            txtIdCampo.setText(String.valueOf(number.intValue()));
        }
    }

    // =========================================================
    // PARSING E UTILITY
    // =========================================================

    /**
     * Estrae l'id campo da stringhe del tipo:
     * - "1 - Campo Calcetto"
     * - "#1 - Campo Calcetto"
     */
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
     * Converte una stringa in intero non negativo.
     */
    private Integer parseNonNegativeInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            int value = Integer.parseInt(raw.trim());
            return value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

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