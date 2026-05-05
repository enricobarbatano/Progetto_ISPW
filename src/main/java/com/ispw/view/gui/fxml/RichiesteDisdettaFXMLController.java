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

/**
 * Controller per la gestione delle richieste di disdetta da parte del Gestore.
 */
public class RichiesteDisdettaFXMLController {

    private GUIGraphicControllerRichiesteDisdetta controller;
    private SessioneUtenteBean sessione;

    private Integer selectedId;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listRichieste;
    @FXML private TextField txtIdRichiesta;
    @FXML private TextArea txtNotaGestore;

    /**
     * Inizializzazione: imposta il listener sulla lista per la selezione automatica dell'ID.
     * Metodo reso public per eliminare i warning dell'IDE.
     */
    @FXML
    public void initialize() {
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

    /**
     * Aggiorna la vista con la lista delle richieste pending e i messaggi di stato.
     */
    public void render(Map<String, Object> params) {
        if (params == null) return;

        // Gestione Messaggi
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
        if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        // Aggiornamento Lista
        if (listRichieste != null) {
            listRichieste.getItems().clear();
            Object raw = params.get(GraphicControllerUtils.KEY_RICHIESTE);

            if (raw instanceof List<?> l) {
                for (Object o : l) listRichieste.getItems().add(String.valueOf(o));
                if (listRichieste.getItems().isEmpty()) {
                    listRichieste.getItems().add("(nessuna richiesta pending)");
                }
            } else {
                listRichieste.getItems().add("(premi Ricarica per caricare le richieste)");
            }

            // Sincronizzazione selezione
            if (selectedId != null && selectedId > 0) {
                int idx = findIndexById(selectedId, listRichieste.getItems());
                if (idx >= 0) listRichieste.getSelectionModel().select(idx);
            }
        }
    }

    // --- GESTIONE EVENTI FXML (Resi public per eliminare i warning) ---

    @FXML
    public void onRicarica() {
        clearLocalError();
        if (controller != null) {
            controller.caricaRichiestePending(sessione);
        }
    }

    @FXML
    public void onApprova() {
        processDecision(true);
    }

    @FXML
    public void onRifiuta() {
        processDecision(false);
    }

    @FXML
    public void onHome() {
        clearLocalError();
        if (controller != null) {
            controller.tornaAllaHome();
        }
    }

    // --- HELPERS ---

    /**
     * Logica comune per approvazione e rifiuto per evitare duplicazione di codice.
     */
    private void processDecision(boolean isApproved) {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveId();
        if (id == null) {
            lblError.setText("Id richiesta non valido (seleziona dalla lista o inserisci un ID)");
            return;
        }

        String nota = (txtNotaGestore != null) ? txtNotaGestore.getText() : "";
        
        if (isApproved) {
            controller.approva(id, nota, sessione);
        } else {
            controller.rifiuta(id, nota, sessione);
        }
    }

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
        if (lblSuccess != null) lblSuccess.setText("");
    }

    private Integer resolveId() {
        // Se c'è una selezione attiva nella ListView, usiamo quella
        if (selectedId != null && selectedId > 0) return selectedId;
        
        // Altrimenti proviamo a parsare il testo inserito manualmente
        String manualId = (txtIdRichiesta != null) ? txtIdRichiesta.getText() : null;
        return parsePositiveInt(manualId);
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int v = Integer.parseInt(raw.trim());
            return (v > 0) ? v : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseIdFromRichiestaString(String s) {
        if (s == null || !s.contains("Richiesta#")) return null;
        try {
            // Estrazione robusta del numero dopo "Richiesta#"
            String sub = s.split("Richiesta#")[1].split(" ")[0].replaceAll("[^0-9]", "");
            return Integer.parseInt(sub);
        } catch (Exception e) {
            return null;
        }
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