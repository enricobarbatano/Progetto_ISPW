package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.DisdettaFXMLController;
import com.ispw.view.interfaces.ViewDisdettaPrenotazione;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIDisdettaView extends GenericViewGUI implements ViewDisdettaPrenotazione, NavigableController {

    private final GUIGraphicControllerDisdetta controller;

    // ✅ cache (fondamentale per non perdere stato ad ogni goTo)
    private Parent cachedRoot;
    private DisdettaFXMLController cachedFx;

    // evita richieste ripetute a vuoto
    private boolean elencoRequested = false;

    public GUIDisdettaView(GUIGraphicControllerDisdetta controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/disdetta.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

            // Best-effort: se non ho elenco/anteprima/success e non ho error, chiedo elenco UNA volta
            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;
            boolean hasElenco = getLastParams().get(GraphicControllerUtils.KEY_PRENOTAZIONI) != null;
            boolean hasAnteprima = getLastParams().get(GraphicControllerUtils.KEY_ANTEPRIMA) != null;
            boolean hasSuccess = getLastParams().get(GraphicControllerUtils.KEY_SUCCESSO) != null
                    || getLastParams().get(GraphicControllerUtils.KEY_MESSAGE) != null;

            if (hasElenco) elencoRequested = false;

            if (!hasError && !hasElenco && !hasAnteprima && !hasSuccess && !elencoRequested) {
                elencoRequested = true;
                controller.richiediPrenotazioniCancellabili(sessione);
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Disdetta"));
            GuiLauncher.setRoot(fallback);
        }
    }
}