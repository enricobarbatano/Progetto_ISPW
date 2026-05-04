package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class DisdettaFXMLController {

    private GUIGraphicControllerDisdetta controller;
    private SessioneUtenteBean sessione;

    // ✅ id prenotazione selezionata (fallback se textField è vuoto)
    private Integer selectedId;

    // anteprima
    private Boolean previewPossibile;
    private Float previewPenale;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listPrenotazioni;
    @FXML private TextField txtIdPrenotazione;

    @FXML private Label lblAnteprima;

    /**
     * Installiamo il listener UNA sola volta, dopo injection fx:id.
     * - click su riga -> parse id -> setText su txtIdPrenotazione -> aggiorna selectedId
     */
    @FXML
    private void initialize() {
        if (listPrenotazioni != null) {
            listPrenotazioni.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                Integer id = parseIdFromPrenotazioneString(newV);
                if (id != null) {
                    selectedId = id;
                    if (txtIdPrenotazione != null) {
                        txtIdPrenotazione.setText(String.valueOf(id));
                    }
                    clearLocalError();
                }
            });
        }
    }

    public void init(GUIGraphicControllerDisdetta controller, SessioneUtenteBean sessione) {
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

        // ===== lista prenotazioni cancellabili =====
        if (listPrenotazioni != null) {
            listPrenotazioni.getItems().clear();
            Object rawList = params != null ? params.get(GraphicControllerUtils.KEY_PRENOTAZIONI) : null;
            if (rawList instanceof List<?> l) {
                for (Object o : l) listPrenotazioni.getItems().add(String.valueOf(o));
                if (listPrenotazioni.getItems().isEmpty()) {
                    listPrenotazioni.getItems().add("(nessuna prenotazione cancellabile)");
                }
            }
        }

        // ===== anteprima =====
        previewPossibile = null;
        previewPenale = null;

        Object rawAnte = params != null ? params.get(GraphicControllerUtils.KEY_ANTEPRIMA) : null;
        if (rawAnte instanceof Map<?, ?> ante) {
            Object poss = ante.get(GraphicControllerUtils.KEY_POSSIBILE);
            Object pen = ante.get(GraphicControllerUtils.KEY_PENALE);

            previewPossibile = (poss instanceof Boolean b) ? b : null;
            previewPenale = (pen instanceof Number n) ? n.floatValue() : null;

            if (lblAnteprima != null) {
                lblAnteprima.setText(
                        "Possibile: " + (previewPossibile != null ? previewPossibile : "-")
                        + " | penale: " + (previewPenale != null ? previewPenale : 0f) + " EUR"
                );
            }
        } else {
            if (lblAnteprima != null) lblAnteprima.setText("");
        }

        // ===== se payload contiene un id prenotazione specifico (opzionale) =====
        // Se il tuo graphic controller NON invia KEY_ID_PRENOTAZIONE, puoi lasciare questa parte.
        Object rawId = params != null ? params.get(GraphicControllerUtils.KEY_ID_PRENOTAZIONE) : null;
        if (rawId instanceof Integer id && id > 0) {
            selectedId = id;
            if (txtIdPrenotazione != null) txtIdPrenotazione.setText(String.valueOf(id));
        }

        // ===== mantenere selezione (se ho selectedId) dopo refresh lista =====
        if (selectedId != null && selectedId > 0 && listPrenotazioni != null) {
            int idx = findIndexById(selectedId, listPrenotazioni.getItems());
            if (idx >= 0) listPrenotazioni.getSelectionModel().select(idx);
        }
    }

    // ===================== Handlers =====================

    @FXML
    private void onRicarica() {
        clearLocalError();
        if (controller == null) return;
        controller.richiediPrenotazioniCancellabili(sessione);
    }

    @FXML
    private void onAnteprima() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveIdFromUI();
        if (id == null) {
            lblError.setText("Id prenotazione non valido (seleziona dalla lista o inserisci un ID)");
            return;
        }

        selectedId = id;
        controller.richiediAnteprimaDisdetta(id, sessione);
    }

    @FXML
    private void onInviaRichiesta() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveIdFromUI();
        if (id == null) {
            lblError.setText("Id prenotazione non valido (seleziona dalla lista o inserisci un ID)");
            return;
        }

        // Se abbiamo anteprima e non è possibile, blocca in modo safe
        if (Boolean.FALSE.equals(previewPossibile)) {
            lblError.setText("Disdetta non consentita");
            return;
        }

        selectedId = id;
        // ✅ UC complesso: invia richiesta PENDING (non annulla immediatamente)
        controller.confermaDisdetta(id, sessione);
    }

    @FXML
    private void onHome() {
        clearLocalError();
        if (controller == null) return;
        controller.tornaAllaHome();
    }

    // ===================== Helpers =====================

    private Integer resolveIdFromUI() {
        // 1) se ho selezione “memorizzata” (listener) uso quella
        if (selectedId != null && selectedId > 0) return selectedId;

        // 2) prova a parsare il textfield
        Integer manual = parsePositiveInt(txtIdPrenotazione != null ? txtIdPrenotazione.getText() : null);
        if (manual != null) return manual;

        // 3) fallback: prova a parsare dalla riga selezionata (selezione list)
        if (listPrenotazioni != null) {
            int idx = listPrenotazioni.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < listPrenotazioni.getItems().size()) {
                return parseIdFromPrenotazioneString(listPrenotazioni.getItems().get(idx));
            }
        }

        return null;
    }

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
    }

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

    /**
     * Supporta formati tipici:
     * - "Prenotazione #1 - Importo 25.0 EUR"
     * - "#1 - ..."
     */
    private Integer parseIdFromPrenotazioneString(String s) {
        if (s == null) return null;

        // 1) prova "Prenotazione #<id>"
        int pos = s.indexOf("Prenotazione");
        int hash = (pos >= 0) ? s.indexOf('#', pos) : s.indexOf('#');
        if (hash < 0) return null;

        int i = hash + 1;
        int value = 0;
        boolean found = false;

        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            found = true;
            value = value * 10 + Character.digit(s.charAt(i), 10);
            i++;
        }
        return found && value > 0 ? value : null;
    }

    private int findIndexById(int id, List<String> items) {
        if (items == null) return -1;
        for (int i = 0; i < items.size(); i++) {
            Integer parsed = parseIdFromPrenotazioneString(items.get(i));
            if (parsed != null && parsed == id) return i;
        }
        return -1;
    }
}
