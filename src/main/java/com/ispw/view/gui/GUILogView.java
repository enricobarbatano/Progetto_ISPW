package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.LogFXMLController;
import com.ispw.view.interfaces.ViewLog;
import com.ispw.view.shared.LogViewUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
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
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        // ✅ Mantieni vincolo: accesso ai log solo per gestore
        if (!LogViewUtils.isGestore(sessione)) {
            VBox root = GuiViewUtils.createRoot();
            root.getChildren().add(new Label("Accesso ai log riservato al gestore"));
            root.getChildren().add(GuiViewUtils.buildHomeButton(controller::tornaAllaHome));
            GuiLauncher.setRoot(root);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/log.fxml"));
            Parent root = loader.load();

            LogFXMLController fx = loader.getController();
            fx.init(controller, sessione);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

            // Best-effort: se non ho logs e non ho errori, li richiedo
            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;
            boolean hasLogs  = getLastParams().get(GraphicControllerUtils.KEY_LOGS) != null;

            if (!hasError && !hasLogs) {
                controller.richiediLog(20);
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Log"));
            fallback.getChildren().add(GuiViewUtils.buildHomeButton(controller::tornaAllaHome));
            GuiLauncher.setRoot(fallback);
        }
    }
}