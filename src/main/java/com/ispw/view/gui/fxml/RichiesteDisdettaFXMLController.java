package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RichiesteDisdettaFXMLController {

    private GUIGraphicControllerRichiesteDisdetta controller;
    private SessioneUtenteBean sessione;

    // ✅ ultimo id selezionato (fallback se il textfield è vuoto)
    private Integer selectedId;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listRichieste;
    @FXML private TextField txtIdRichiesta;
    @FXML private TextArea txtNotaGestore;

    /**
     * Viene chiamato automaticamente dopo l'injection degli fx:id.
     * Qui installiamo il listener che auto-compila txtIdRichiesta e valorizza selectedId.
     */
    @FXML
    private void initialize() {
        if (listRichieste != null) {
            listRichieste.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                Integer id = parseIdFromRichiestaString(newV);
                if (id != null) {
                    selectedId = id;
                    if (txtIdRichiesta != null) {
                        txtIdRichiesta.setText(String.valueOf(id));
                    }
                    clearLocalError();
                }
            });
        }
    }

    public void init(GUIGraphicControllerRichiesteDisdetta controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    public void render(Map<String, Object> params) {
        // messaggi
        Object err = params != null ? params.get(GraphicControllerUtils.KEY_ERROR) : null;
        Object ok = null;
        if (params != null) {
            ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
            if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
        }

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        // lista richieste
        Object raw = params != null ? params.get(GraphicControllerUtils.KEY_RICHIESTE) : null;
        listRichieste.getItems().clear();

        if (raw instanceof List<?> l) {
            for (Object o : l) listRichieste.getItems().add(String.valueOf(o));
            if (listRichieste.getItems().isEmpty()) {
                listRichieste.getItems().add("(nessuna richiesta pending)");
            }
        } else {
            listRichieste.getItems().add("(premi Ricarica per caricare le richieste)");
        }

        // opzionale: se ho un selectedId già scelto, provo a mantenerlo selezionato dopo un render
        if (selectedId != null && selectedId > 0) {
            int idx = findIndexById(selectedId, listRichieste.getItems());
            if (idx >= 0) listRichieste.getSelectionModel().select(idx);
        }
    }

    @FXML
    private void onRicarica() {
        clearLocalError();
        if (controller == null) return;
        controller.caricaRichiestePending(sessione);
    }

    @FXML
    private void onApprova() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveId();
        if (id == null) {
            lblError.setText("Id richiesta non valido (seleziona una richiesta o inserisci un ID)");
            return;
        }
        controller.approva(id, txtNotaGestore != null ? txtNotaGestore.getText() : "", sessione);
    }

    @FXML
    private void onRifiuta() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveId();
        if (id == null) {
            lblError.setText("Id richiesta non valido (seleziona una richiesta o inserisci un ID)");
            return;
        }
        controller.rifiuta(id, txtNotaGestore != null ? txtNotaGestore.getText() : "", sessione);
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
     * Risolve l'id richiesta in modo robusto:
     * 1) se l'utente ha selezionato una richiesta, selectedId è già valorizzato
     * 2) altrimenti prova a parsare il TextField
     */
    private Integer resolveId() {
        if (selectedId != null && selectedId > 0) return selectedId;
        return parsePositiveInt(txtIdRichiesta != null ? txtIdRichiesta.getText() : null);
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
     * Esempio stringa: "Richiesta#1 pren#1 utente#2 stato=PENDING ..."
     * Estrae il numero dopo "Richiesta#".
     */
    private Integer parseIdFromRichiestaString(String s) {
        if (s == null) return null;
        String key = "Richiesta#";
        int pos = s.indexOf(key);
        if (pos < 0) return null;
        int i = pos + key.length();
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
            Integer parsed = parseIdFromRichiestaString(items.get(i));
            if (parsed != null && parsed == id) return i;
        }
        return -1;
    }
}