package com.ispw.view.gui;

import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerLog;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewLog;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class GUILogView extends GenericViewGUI implements ViewLog, NavigableController {

    private final GUIGraphicControllerLog controller;

    public GUILogView(GUIGraphicControllerLog controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    @Override
    public void onShow() {
        onShow(Map.of());
    }

    @Override
    public void onHide() {
        // no-op
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        Ruolo ruolo = (sessione != null && sessione.getUtente() != null) ? sessione.getUtente().getRuolo() : null;
        if (ruolo != Ruolo.GESTORE) {
            VBox root = GuiViewUtils.createRoot();
            root.getChildren().add(new Label("Accesso ai log riservato al gestore"));
            Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());
            root.getChildren().add(home);
            GuiLauncher.setRoot(root);
            return;
        }

        Object raw = lastParams.get(GraphicControllerUtils.KEY_LOGS);
        if (!(raw instanceof List<?>)) {
            controller.richiediLog(20);
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> logs = (List<String>) raw;

        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label("Log di sistema"));

        ListView<String> list = new ListView<>();
        GuiViewUtils.fillList(list, logs);

        Button refresh = new Button("Aggiorna");
        refresh.setOnAction(e -> controller.richiediLog(20));

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());

        root.getChildren().addAll(list, refresh, home);
        GuiLauncher.setRoot(root);
    }
}
