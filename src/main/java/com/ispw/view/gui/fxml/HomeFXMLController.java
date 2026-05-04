package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeFXMLController {

    private GraphicControllerNavigation navigator;
    private SessioneUtenteBean sessione;
    private Ruolo ruolo;

    @FXML private Label lblRuolo;

    @FXML private Button btnOp1;
    @FXML private Button btnOp2;
    @FXML private Button btnLog;
    @FXML private Button btnRichiesteDisdetta;

    public void init(GraphicControllerNavigation navigator, SessioneUtenteBean sessione) {
        this.navigator = navigator;
        this.sessione = sessione;
        this.ruolo = (sessione != null && sessione.getUtente() != null) ? sessione.getUtente().getRuolo() : null;

        if (lblRuolo != null) {
            lblRuolo.setText(ruolo != null ? "Ruolo: " + ruolo : "Ruolo: -");
        }

        // Imposta SEMPRE testi base (così non restano bottoni vuoti)
        if (btnLog != null) btnLog.setText("Log");
        if (btnRichiesteDisdetta != null) btnRichiesteDisdetta.setText("Richieste disdetta");

        if (ruolo == Ruolo.GESTORE) {
            if (btnOp1 != null) btnOp1.setText("Regole");
            if (btnOp2 != null) btnOp2.setText("Penalità");

            setVisible(btnLog, true);
            setVisible(btnRichiesteDisdetta, true);
        } else {
            if (btnOp1 != null) btnOp1.setText("Prenotazione");
            if (btnOp2 != null) btnOp2.setText("Disdetta (richiesta)");

            setVisible(btnLog, false);
            setVisible(btnRichiesteDisdetta, false);
        }
    }

    public void render(Map<String, Object> params) {
        // opzionale: qui potresti mostrare messaggi in home se vuoi
    }

    private void setVisible(Button b, boolean v) {
        if (b == null) return;
        b.setVisible(v);
        b.setManaged(v);
    }

    private void goTo(String route) {
        if (navigator == null) return;
        if (sessione != null) {
            navigator.goTo(route, Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
        } else {
            navigator.goTo(route, Map.of());
        }
    }

    @FXML private void onAccount() { goTo(GraphicControllerUtils.ROUTE_ACCOUNT); }

    @FXML private void onOp1() {
        if (ruolo == Ruolo.GESTORE) goTo(GraphicControllerUtils.ROUTE_REGOLE);
        else goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE);
    }

    @FXML private void onOp2() {
        if (ruolo == Ruolo.GESTORE) goTo(GraphicControllerUtils.ROUTE_PENALITA);
        else goTo(GraphicControllerUtils.ROUTE_DISDETTA);
    }

    @FXML private void onLog() { goTo(GraphicControllerUtils.ROUTE_LOGS); }

    @FXML private void onRichiesteDisdetta() { goTo(GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA); }

    @FXML private void onLogout() { goTo(GraphicControllerUtils.ROUTE_LOGIN); }
}