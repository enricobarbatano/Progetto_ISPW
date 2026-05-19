package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controller FXML per la visualizzazione dei log di sistema.
 *
 * RESPONSABILITÀ:
 * - mostrare eventuali messaggi di errore;
 * - mostrare la lista dei log ricevuti dal navigator;
 * - delegare al graphic controller la richiesta di aggiornamento log;
 * - delegare al graphic controller il ritorno alla home.
 *
 * NON:
 * - legge file di log;
 * - accede a DAO o persistenza;
 * - chiama direttamente il logic controller;
 * - crea bean;
 * - gestisce direttamente la navigazione.
 *
 * Nota:
 * il caricamento dei log avviene tramite il pulsante "Aggiorna Registro",
 * che richiama onRefresh(). La GUIView non carica automaticamente i log
 * in onShow(), così si evitano cicli di navigazione.
 */
public class LogFXMLController {

    private GUIGraphicControllerLog controller;

    @FXML private Label lblError;
    @FXML private ListView<String> listLogs;

    /**
     * Inizializza il controller FXML con il graphic controller.
     *
     * La sessione viene ricevuta per coerenza con le altre schermate,
     * ma non è necessaria agli handler di questa view.
     *
     * @param controller controller grafico dei log
     * @param sessione sessione corrente, non usata direttamente in questa view
     */
    @SuppressWarnings("java:S1172")
    public void init(GUIGraphicControllerLog controller, SessioneUtenteBean sessione) {
        this.controller = controller;
    }

    /**
     * Renderizza i dati ricevuti dal navigator.
     *
     * La lista viene aggiornata solo se nel payload è presente KEY_LOGS.
     * In questo modo un eventuale messaggio di errore non cancella
     * automaticamente i log già mostrati.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearError();
            return;
        }

        renderError(params);

        if (params.containsKey(GraphicControllerUtils.KEY_LOGS)) {
            renderLogs(params.get(GraphicControllerUtils.KEY_LOGS));
        }
    }

    /**
     * Richiede l'aggiornamento del registro log.
     */
    @FXML
    public void onRefresh() {
        clearError();

        if (controller != null) {
            controller.richiediLog(20);
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
     * Renderizza eventuali messaggi di errore.
     */
    private void renderError(Map<String, Object> params) {
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);

        if (lblError != null) {
            lblError.setText(err != null ? String.valueOf(err) : "");
        }
    }

    /**
     * Renderizza la lista dei log.
     */
    private void renderLogs(Object rawLogs) {
        if (listLogs == null) {
            return;
        }

        listLogs.getItems().clear();

        if (rawLogs instanceof List<?> logs) {
            listLogs.getItems().setAll(
                    logs.stream()
                            .map(Object::toString)
                            .toList()
            );
        }

        if (listLogs.getItems().isEmpty()) {
            listLogs.getItems().add("(nessun log disponibile)");
        }
    }

    /**
     * Pulisce il messaggio di errore locale.
     */
    private void clearError() {
        if (lblError != null) {
            lblError.setText("");
        }
    }
}