package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.RegistrazioneFXMLController;
import com.ispw.view.interfaces.ViewRegistrazione;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIRegistrazioneView extends GenericViewGUI implements ViewRegistrazione, NavigableController {

    private final GUIGraphicControllerRegistrazione controller;

    public GUIRegistrazioneView(GUIGraphicControllerRegistrazione controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        // Registrazione non deve portarsi dietro sessione
        sessione = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registrazione.fxml"));
            Parent root = loader.load();

            RegistrazioneFXMLController fx = loader.getController();
            fx.init(controller);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Registrazione"));
            GuiLauncher.setRoot(fallback);
        }
    }
}