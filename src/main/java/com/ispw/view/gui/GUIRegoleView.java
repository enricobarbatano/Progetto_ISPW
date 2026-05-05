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

    private Parent cachedRoot;
    private RegoleFXMLController cachedFx;

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
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/regole.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());
            GuiLauncher.setRoot(cachedRoot);

            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;
            boolean hasCampi = getLastParams().get(GraphicControllerUtils.KEY_CAMPI) != null;

            if (hasCampi) campiRequested = false;

            if (!hasError && !hasCampi && !campiRequested) {
                campiRequested = true;
                controller.richiediListaCampi();
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Regole"));
            GuiLauncher.setRoot(fallback);
        }
    }
}