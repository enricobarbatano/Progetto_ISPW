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
 * Controller per la gestione delle penalità da parte del Gestore.
 * Permette di selezionare un utente dalla lista e applicare una sanzione monetaria.
 */
public class PenalitaFXMLController {

    private GUIGraphicControllerPenalita controller;

    // Ultimo ID selezionato dalla lista per facilitare l'input
    private Integer selectedId;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;
    @FXML private ListView<String> listUtenti;
    @FXML private TextField txtIdUtente;
    @FXML private TextField txtImporto;
    @FXML private TextField txtMotivazione;

    /**
     * Inizializzazione del Listener sulla ListView.
     * Collegato automaticamente da JavaFX se il metodo è public o annotato con @FXML.
     */
    @FXML
    public void initialize() {
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

    /**
     * Inizializza le dipendenze. 
     * Nota: sessione rimosso dai campi della classe se non utilizzato nei metodi onApplica/onHome.
     */
    public void init(GUIGraphicControllerPenalita controller, SessioneUtenteBean sessione) {
        this.controller = controller;
    }

    /**
     * Renderizza i dati provenienti dal controller grafico (lista utenti e messaggi).
     */
    public void render(Map<String, Object> params) {
        if (params == null) return;

        // 1. Gestione messaggi di feedback
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
        if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        // 2. Aggiornamento lista utenti
        updateUserList(params.get(GraphicControllerUtils.KEY_UTENTI));

        // 3. Ripristino selezione visiva se presente
        restoreSelection();
    }

    // --- GESTIONE EVENTI FXML ---

    @FXML
    public void onListaUtenti() {
        clearLocalError();
        if (controller != null) {
            controller.richiediListaUtenti();
        }
    }

    @FXML
    public void onApplica() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveIdUtente();
        Float importo = parsePositiveFloat(txtImporto != null ? txtImporto.getText() : null);
        String motivazione = (txtMotivazione != null) ? txtMotivazione.getText() : "";

        if (id == null || importo == null || motivazione.isBlank()) {
            lblError.setText("Dati incompleti: seleziona un utente e inserisci importo/motivazione.");
            return;
        }

        controller.applicaPenalita(id, importo, motivazione);
    }

    @FXML
    public void onHome() {
        if (controller != null) {
            controller.tornaAllaHome();
        }
    }

    // --- HELPER METODS ---

    private void updateUserList(Object raw) {
        if (listUtenti == null) return;
        listUtenti.getItems().clear();
        
        if (raw instanceof List<?> users) {
            for (Object user : users) {
                listUtenti.getItems().add(String.valueOf(user));
            }
        }
    }

    private void restoreSelection() {
        if (selectedId != null && selectedId > 0 && listUtenti != null) {
            int idx = findIndexById(selectedId, listUtenti.getItems());
            if (idx >= 0) {
                listUtenti.getSelectionModel().select(idx);
            }
        }
    }

    private Integer resolveIdUtente() {
        // Priorità al TextField (se l'utente ha scritto manualmente), fallback alla selezione
        String text = (txtIdUtente != null) ? txtIdUtente.getText() : null;
        Integer manualId = parsePositiveInt(text);
        
        return (manualId != null) ? manualId : selectedId;
    }

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
        if (lblSuccess != null) lblSuccess.setText("");
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int v = Integer.parseInt(raw.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Float parsePositiveFloat(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            float v = Float.parseFloat(raw.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIdFromUtenteString(String s) {
        if (s == null || !s.contains("#")) return null;
        try {
            // Estrae i numeri che seguono il carattere '#'
            String sub = s.split("#")[1].split(" ")[0].replaceAll("[^0-9]", "");
            return Integer.parseInt(sub);
        } catch (Exception e) {
            return null;
        }
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