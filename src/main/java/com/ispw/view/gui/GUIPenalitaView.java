package com.ispw.view.gui;

import java.util.Map;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.PenalitaFXMLController;
import com.ispw.view.interfaces.ViewGestionePenalita;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIPenalitaView extends GenericViewGUI implements ViewGestionePenalita, NavigableController {

    private final GUIGraphicControllerPenalita controller;

    // ✅ cache per non perdere selezione / campi su round-trip
    private Parent cachedRoot;
    private PenalitaFXMLController cachedFx;

    // evita richieste ripetute senza payload
    private boolean utentiRequested = false;

    public GUIPenalitaView(GUIGraphicControllerPenalita controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/penalita.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

            // Best-effort: se non ho utenti e non ho error, richiedo lista UNA volta
            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;
            boolean hasUtenti = getLastParams().get(GraphicControllerUtils.KEY_UTENTI) != null;

            if (hasUtenti) utentiRequested = false;

            if (!hasError && !hasUtenti && !utentiRequested) {
                utentiRequested = true;
                controller.richiediListaUtenti();
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento Penalità"));
            GuiLauncher.setRoot(fallback);
        }
    }
}

