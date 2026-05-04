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

public class GUIPrenotazioneView extends GenericViewGUI implements ViewGestionePrenotazione, NavigableController {

    private final GUIGraphicControllerPrenotazione controller;

    // ✅ cache UI/Controller per non perdere stato tra i round-trip del navigator
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
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prenotazione.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            // riallinea controller grafico e sessione (utile in caso di logout/login)
            cachedFx.init(controller, sessione);

            // render payload
            cachedFx.render(getLastParams());
            GuiLauncher.setRoot(cachedRoot);

            // best-effort: al primo ingresso senza payload carica campi
            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;
            boolean hasCampi = getLastParams().get(GraphicControllerUtils.KEY_CAMPI) != null;
            boolean hasSlots = getLastParams().get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) != null;
            boolean hasRiep = getLastParams().get(GraphicControllerUtils.KEY_RIEPILOGO) != null;
            boolean hasPay = getLastParams().get(GraphicControllerUtils.KEY_PAGAMENTO) != null;

            if (!hasError && !hasCampi && !hasSlots && !hasRiep && !hasPay) {
                controller.richiediListaCampi(sessione);
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Prenotazione"));
            GuiLauncher.setRoot(fallback);
        }
    }
}