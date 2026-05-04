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

public class PenalitaFXMLController {

    private GUIGraphicControllerPenalita controller;
    private SessioneUtenteBean sessione;

    // ✅ ultimo id selezionato (fallback se il textfield è vuoto)
    private Integer selectedId;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listUtenti;
    @FXML private TextField txtIdUtente;
    @FXML private TextField txtImporto;
    @FXML private TextField txtMotivazione;

    /**
     * Listener “smart”:
     * click su utente -> parse id -> auto-compila txtIdUtente -> aggiorna selectedId.
     */
    @FXML
    private void initialize() {
        if (listUtenti != null) {
            listUtenti.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                Integer id = parseIdFromUtenteString(newV);
                if (id != null) {
                    selectedId = id;
                    if (txtIdUtente != null) {
                        txtIdUtente.setText(String.valueOf(id));
                    }
                    clearLocalError();
                }
            });
        }
    }

    public void init(GUIGraphicControllerPenalita controller, SessioneUtenteBean sessione) {
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

        // lista utenti
        if (listUtenti != null) {
            listUtenti.getItems().clear();
            Object raw = params != null ? params.get(GraphicControllerUtils.KEY_UTENTI) : null;
            if (raw instanceof List<?> l) {
                for (Object o : l) listUtenti.getItems().add(String.valueOf(o));
            }
        }

        // opzionale: mantieni selezione dopo refresh lista
        if (selectedId != null && selectedId > 0 && listUtenti != null) {
            int idx = findIndexById(selectedId, listUtenti.getItems());
            if (idx >= 0) listUtenti.getSelectionModel().select(idx);
        }
    }

    @FXML
    private void onListaUtenti() {
        clearLocalError();
        if (controller == null) return;
        controller.richiediListaUtenti();
    }

    @FXML
    private void onApplica() {
        clearLocalError();
        if (controller == null) return;

        // ✅ usa selezione (selectedId) come fallback se il campo è vuoto
        Integer id = resolveIdUtente();
        Float importo = parsePositiveFloat(txtImporto != null ? txtImporto.getText() : null);
        String motivazione = txtMotivazione != null ? txtMotivazione.getText() : null;

        if (id == null || importo == null || motivazione == null || motivazione.isBlank()) {
            lblError.setText("Dati penalità non validi (seleziona utente, inserisci importo e motivazione)");
            return;
        }

        controller.applicaPenalita(id, importo, motivazione);
    }

    @FXML
    private void onHome() {
        clearLocalError();
        if (controller == null) return;
        controller.tornaAllaHome();
    }

    // ===================== Helpers =====================

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
    }

    /**
     * Risolve l'id utente in modo robusto:
     * 1) se l'utente ha selezionato una riga -> selectedId
     * 2) fallback -> parse del textField
     */
    private Integer resolveIdUtente() {
        if (selectedId != null && selectedId > 0) return selectedId;
        return parsePositiveInt(txtIdUtente != null ? txtIdUtente.getText() : null);
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

    private Float parsePositiveFloat(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;
        try {
            float v = Float.parseFloat(t);
            return v > 0 ? v : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Estrae un id da stringhe tipiche della tua lista utenti.
     * Esempi supportati:
     * - "#12 - Mario Rossi (email=...)"
     * - "Utente #12 ..."
     * - "... id=12 ..."
     */
    private Integer parseIdFromUtenteString(String s) {
        if (s == null) return null;

        // 1) pattern "#<id>"
        int hash = s.indexOf('#');
        if (hash >= 0) {
            int i = hash + 1;
            int value = 0;
            boolean found = false;
            while (i < s.length() && Character.isDigit(s.charAt(i))) {
                found = true;
                value = value * 10 + Character.digit(s.charAt(i), 10);
                i++;
            }
            if (found && value > 0) return value;
        }

        // 2) pattern "id=<id>"
        int idPos = s.indexOf("id=");
        if (idPos >= 0) {
            int i = idPos + 3;
            int value = 0;
            boolean found = false;
            while (i < s.length() && Character.isDigit(s.charAt(i))) {
                found = true;
                value = value * 10 + Character.digit(s.charAt(i), 10);
                i++;
            }
            if (found && value > 0) return value;
        }

        return null;
    }

    private int findIndexById(int id, List<String> items) {
        if (items == null) return -1;
        for (int i = 0; i < items.size(); i++) {
            Integer parsed = parseIdFromUtenteString(items.get(i));
            if (parsed != null && parsed == id) return i;
        }
        return -1;
    }
}