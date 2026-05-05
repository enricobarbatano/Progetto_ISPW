package com.ispw.view.gui.fxml;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller per la gestione delle disdette.
 * Implementa Initializable per gestire correttamente il ciclo di vita JavaFX.
 */
public class DisdettaFXMLController implements Initializable {

    private GUIGraphicControllerDisdetta controller;
    private SessioneUtenteBean sessione;

    // Stato interno per la gestione dell'ID e dell'anteprima
    private Integer selectedId;
    private Boolean previewPossibile;
    private Float previewPenale;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;
    @FXML private ListView<String> listPrenotazioni;
    @FXML private TextField txtIdPrenotazione;
    @FXML private Label lblAnteprima;

    /**
     * Inizializzazione: impostiamo il listener sulla lista.
     * Cambiato in public per evitare i warning "never used" di alcuni IDE.
     */
    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
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
        if (params == null) return;

        // 1. Gestione messaggi (Error/Success)
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
        if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        // 2. Popolamento Lista Prenotazioni
        updateListView(params.get(GraphicControllerUtils.KEY_PRENOTAZIONI));

        // 3. Gestione Anteprima (Safe Unboxing)
        handlePreviewData(params.get(GraphicControllerUtils.KEY_ANTEPRIMA));

        // 4. Sincronizzazione ID selezionato dal payload
        handleSelectedIdFromParams(params.get(GraphicControllerUtils.KEY_ID_PRENOTAZIONE));
    }

    // --- Handlers FXML (Resi public per eliminare i warning) ---

    @FXML
    public void onRicarica() {
        clearLocalError();
        if (controller != null) {
            controller.richiediPrenotazioniCancellabili(sessione);
        }
    }

    @FXML
    public void onAnteprima() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveIdFromUI();
        if (id == null) {
            lblError.setText("ID non valido: seleziona dalla lista o inseriscilo manualmente.");
            return;
        }

        selectedId = id;
        controller.richiediAnteprimaDisdetta(id, sessione);
    }

    @FXML
    public void onInviaRichiesta() {
        clearLocalError();
        if (controller == null) return;

        Integer id = resolveIdFromUI();
        if (id == null) {
            lblError.setText("Seleziona una prenotazione valida.");
            return;
        }

        // Controllo logico sull'anteprima caricata
        if (Boolean.FALSE.equals(previewPossibile)) {
            lblError.setText("Disdetta non consentita per questa prenotazione.");
            return;
        }

        selectedId = id;
        controller.confermaDisdetta(id, sessione);
    }

    @FXML
    public void onHome() {
        if (controller != null) {
            controller.tornaAllaHome();
        }
    }

    // --- Helper di Supporto e Pulizia Logica ---

    private void updateListView(Object rawList) {
        if (listPrenotazioni == null) return;
        listPrenotazioni.getItems().clear();
        
        if (rawList instanceof List<?> list) {
            for (Object item : list) {
                listPrenotazioni.getItems().add(String.valueOf(item));
            }
            if (listPrenotazioni.getItems().isEmpty()) {
                listPrenotazioni.getItems().add("(nessuna prenotazione cancellabile)");
            }
        }
    }

    private void handlePreviewData(Object rawAnte) {
        previewPossibile = null;
        previewPenale = null;

        if (rawAnte instanceof Map<?, ?> ante) {
            Object poss = ante.get(GraphicControllerUtils.KEY_POSSIBILE);
            Object pen = ante.get(GraphicControllerUtils.KEY_PENALE);

            // Evitiamo unboxing impliciti pericolosi
            previewPossibile = (poss instanceof Boolean b) ? b : null;
            previewPenale = (pen instanceof Number n) ? n.floatValue() : 0.0f;

            if (lblAnteprima != null) {
                String possStr = (previewPossibile != null) ? (previewPossibile ? "Sì" : "No") : "-";
                lblAnteprima.setText(String.format("Disdetta possibile: %s | Penale: %.2f EUR", possStr, previewPenale));
            }
        } else if (lblAnteprima != null) {
            lblAnteprima.setText("");
        }
    }

    private void handleSelectedIdFromParams(Object rawId) {
        if (rawId instanceof Integer id && id > 0) {
            selectedId = id;
            if (txtIdPrenotazione != null) txtIdPrenotazione.setText(String.valueOf(id));
        }

        // Mantiene la selezione visuale nella lista
        if (selectedId != null && listPrenotazioni != null) {
            int idx = findIndexById(selectedId, listPrenotazioni.getItems());
            if (idx >= 0) listPrenotazioni.getSelectionModel().select(idx);
        }
    }

    private Integer resolveIdFromUI() {
        if (selectedId != null && selectedId > 0) return selectedId;

        // Parsing manuale dal TextField
        String text = (txtIdPrenotazione != null) ? txtIdPrenotazione.getText() : null;
        Integer manual = parsePositiveInt(text);
        if (manual != null) return manual;

        // Fallback dalla selezione attuale della lista
        if (listPrenotazioni != null) {
            String selected = listPrenotazioni.getSelectionModel().getSelectedItem();
            return parseIdFromPrenotazioneString(selected);
        }
        return null;
    }

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
        if (lblSuccess != null) lblSuccess.setText("");
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            int v = Integer.parseInt(raw.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIdFromPrenotazioneString(String s) {
        if (s == null || !s.contains("#")) return null;
        try {
            // Estrae il numero dopo il carattere '#'
            String sub = s.substring(s.indexOf('#') + 1).split(" ")[0];
            int id = Integer.parseInt(sub.replaceAll("[^0-9]", ""));
            return id > 0 ? id : null;
        } catch (Exception e) {
            return null;
        }
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