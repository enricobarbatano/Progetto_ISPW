package com.ispw.view.gui;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.RichiesteDisdettaFXMLController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIRichiesteDisdettaView extends GenericViewGUI implements NavigableController {

    private final GUIGraphicControllerRichiesteDisdetta controller;

    // ✅ cache: evita di perdere selezione/nota ad ogni round-trip
    private Parent cachedRoot;
    private RichiesteDisdettaFXMLController cachedFx;

    // evita richieste ripetute a vuoto
    private boolean richiesteRequested = false;

    public GUIRichiesteDisdettaView(GUIGraphicControllerRichiesteDisdetta controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/richieste_disdetta.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            // riallinea (utile in caso di logout/login)
            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;
            boolean hasRichieste = getLastParams().get(GraphicControllerUtils.KEY_RICHIESTE) != null;

            if (hasRichieste) richiesteRequested = false;

            if (!hasError && !hasRichieste && !richiesteRequested) {
                richiesteRequested = true;
                SessioneUtenteBean s = sessione;
                controller.caricaRichiestePending(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Richieste Disdetta"));
            GuiLauncher.setRoot(fallback);
        }
    }
}
