package com.ispw.view.gui;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewHomeProfilo;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GUIHomeView extends GenericViewGUI implements ViewHomeProfilo, NavigableController {

    private final GraphicControllerNavigation navigator;

    public GUIHomeView(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_HOME;
    }

    @Override
    public void onShow() {
        onShow(Map.of());
    }

    @Override
    public void onHide() {
        // no-op
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));

        Label title = new Label("Home");
        SessioneUtenteBean s = sessione;
        Ruolo ruolo = (s != null && s.getUtente() != null) ? s.getUtente().getRuolo() : null;
        Label ruoloLabel = new Label(ruolo != null ? "Ruolo: " + ruolo : "Ruolo: -");

        Button account = new Button("Account");
        account.setOnAction(e -> goTo(GraphicControllerUtils.ROUTE_ACCOUNT));

        Button op1 = new Button(ruolo == Ruolo.GESTORE ? "Regole" : "Prenotazione");
        op1.setOnAction(e -> goTo(ruolo == Ruolo.GESTORE ? GraphicControllerUtils.ROUTE_REGOLE
                                                         : GraphicControllerUtils.ROUTE_PRENOTAZIONE));

        Button op2 = new Button(ruolo == Ruolo.GESTORE ? "Penalità" : "Disdetta");
        op2.setOnAction(e -> goTo(ruolo == Ruolo.GESTORE ? GraphicControllerUtils.ROUTE_PENALITA
                                                         : GraphicControllerUtils.ROUTE_DISDETTA));

        Button logout = new Button("Logout");
        logout.setOnAction(e -> goTo(GraphicControllerUtils.ROUTE_LOGIN));

        root.getChildren().addAll(title, ruoloLabel, account, op1, op2, logout);
        GuiLauncher.setRoot(root);
    }

    private void goTo(String route) {
        if (navigator == null) {
            return;
        }
        if (sessione != null) {
            navigator.goTo(route, Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
        } else {
            navigator.goTo(route, Map.of());
        }
    }
}
