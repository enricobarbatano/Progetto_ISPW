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
 * Controller per la visualizzazione dei log di sistema.
 * Rimosso il campo 'sessione' poiché non utilizzato nelle chiamate al controller.
 */
public class LogFXMLController {

    private GUIGraphicControllerLog controller;

    @FXML private Label lblError;
    @FXML private ListView<String> listLogs;

    /**
     * Inizializzazione del controller.
     * Rimosso l'assegnamento della sessione per eliminare i warning dell'IDE.
     */
    public void init(GUIGraphicControllerLog controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        // La sessione qui viene ignorata perché non serve ai metodi onRefresh o onHome
    }

    public void render(Map<String, Object> params) {
        if (params == null) return;

        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        lblError.setText(err != null ? String.valueOf(err) : "");

        updateLogList(params.get(GraphicControllerUtils.KEY_LOGS));
    }

    private void updateLogList(Object rawLogs) {
        if (listLogs == null) return;
        listLogs.getItems().clear();
        
        if (rawLogs instanceof List<?> logs) {
            for (Object log : logs) {
                listLogs.getItems().add(String.valueOf(log));
            }
            if (listLogs.getItems().isEmpty()) {
                listLogs.getItems().add("(nessun log disponibile)");
            }
        }
    }

    @FXML 
    public void onRefresh() {
        if (lblError != null) lblError.setText("");
        if (controller != null) {
            controller.richiediLog(20);
        }
    }

    @FXML 
    public void onHome() {
        if (controller != null) {
            controller.tornaAllaHome();
        }
    }
}