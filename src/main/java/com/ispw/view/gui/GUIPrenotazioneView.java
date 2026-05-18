package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.PrenotazioneFXMLController;
import com.ispw.view.interfaces.ViewGestionePrenotazione;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la prenotazione.
 *
 * RESPONSABILITÀ:
 * - caricare FXML
 * - inizializzare controller FXML
 * - passare i dati ricevuti dal navigator
 *
 * IMPORTANTE:
 * ❌ nessuna logica di business
 * ❌ nessuna creazione di bean
 */
public class GUIPrenotazioneView extends GenericViewGUI
        implements ViewGestionePrenotazione, NavigableController {

    private final GUIGraphicControllerPrenotazione controller;

    // caching per evitare reload continui
    private Parent cachedRoot;
    private PrenotazioneFXMLController cachedFx;

    public GUIPrenotazioneView(GUIGraphicControllerPrenotazione controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {

            // carica FXML solo la prima volta
            if (cachedRoot == null) {
                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("/fxml/prenotazione.fxml"));

                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            // inizializza controller FXML
            cachedFx.init(controller, sessione);

            // rendering dati
            cachedFx.render(getLastParams());

            // mostra UI
            GuiLauncher.setRoot(cachedRoot);

        } catch (Exception e) {

            // fallback UI se qualcosa va storto
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento prenotazione"));
            GuiLauncher.setRoot(fallback);
        }
    }
}