package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.RegoleFXMLController;
import com.ispw.view.interfaces.ViewGestioneRegole;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIRegoleView extends GenericViewGUI implements ViewGestioneRegole, NavigableController {

    private final GUIGraphicControllerRegole controller;

    // Evita di richiedere lista campi ad ogni onShow senza payload
    private boolean campiRequested = false;

    public GUIRegoleView(GUIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/regole.fxml"));
            Parent root = loader.load();

            RegoleFXMLController fx = loader.getController();
            fx.init(controller, sessione);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

            // Best-effort: se non ho campi e non ho errori, richiedo lista campi UNA sola volta
            boolean hasCampi = getLastParams().get(GraphicControllerUtils.KEY_CAMPI) != null;
            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;

            if (!hasCampi && !hasError && !campiRequested) {
                campiRequested = true;
                controller.richiediListaCampi();
            }

            // se arrivano i campi, resetto il flag
            if (hasCampi) {
                campiRequested = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Regole"));
            GuiLauncher.setRoot(fallback);
        }
    }
}