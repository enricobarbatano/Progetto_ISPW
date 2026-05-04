package com.ispw.view.gui.fxml;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class LogFXMLController {

    private GUIGraphicControllerLog controller;
    private SessioneUtenteBean sessione;

    @FXML private Label lblError;
    @FXML private ListView<String> listLogs;

    public void init(GUIGraphicControllerLog controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    @SuppressWarnings("unchecked")
    public void render(Map<String, Object> params) {
        Object err = params != null ? params.get(GraphicControllerUtils.KEY_ERROR) : null;
        lblError.setText(err != null ? String.valueOf(err) : "");

        listLogs.getItems().clear();
        Object raw = params != null ? params.get(GraphicControllerUtils.KEY_LOGS) : null;
        if (raw instanceof List<?> l) {
            for (Object o : l) listLogs.getItems().add(String.valueOf(o));
            if (listLogs.getItems().isEmpty()) listLogs.getItems().add("(nessun log disponibile)");
        }
    }

    @FXML private void onRefresh() {
        if (controller == null) return;
        controller.richiediLog(20);
    }

    @FXML private void onHome() {
        if (controller == null) return;
        controller.tornaAllaHome();
    }
}