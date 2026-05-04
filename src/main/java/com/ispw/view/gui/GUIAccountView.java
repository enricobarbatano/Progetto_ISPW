package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.AccountFXMLController;
import com.ispw.view.interfaces.ViewGestioneAccount;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIAccountView extends GenericViewGUI implements ViewGestioneAccount, NavigableController {

    private final GUIGraphicControllerAccount controller;

    public GUIAccountView(GUIGraphicControllerAccount controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account.fxml"));
            Parent root = loader.load();

            AccountFXMLController fx = loader.getController();
            fx.init(controller, sessione);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

            // Best-effort: se entro su Account senza dati e senza errori, carico i dati
            boolean hasDati = getLastParams().get(GraphicControllerUtils.KEY_DATI_ACCOUNT) != null;
            boolean hasError = getLastParams().get(GraphicControllerUtils.KEY_ERROR) != null;

            if (!hasDati && !hasError) {
                controller.loadAccount(sessione);
            }

        } catch (Exception e) {
            e.printStackTrace();
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Account"));
            GuiLauncher.setRoot(fallback);
        }
    }
}