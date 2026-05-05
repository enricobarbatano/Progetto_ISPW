package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeFXMLController {

    private GraphicControllerNavigation navigator;
    private SessioneUtenteBean sessione;
    private Ruolo ruolo;

    @FXML private Label lblRuolo;
    @FXML private Label lblTitoloOp1;
    @FXML private Label lblTitoloOp2;
    @FXML private Button btnOp1;
    @FXML private Button btnOp2;
    @FXML private VBox boxGestore; // Il contenitore dell'area admin

    public void init(GraphicControllerNavigation navigator, SessioneUtenteBean sessione) {
        this.navigator = navigator;
        this.sessione = sessione;
        
        this.ruolo = (sessione != null && sessione.getUtente() != null) 
                     ? sessione.getUtente().getRuolo() 
                     : null;

        updateUI();
    }

    private void updateUI() {
        if (lblRuolo != null) {
            lblRuolo.setText("Sessione attiva come: " + (ruolo != null ? ruolo : "Ospite"));
        }

        boolean isGestore = (ruolo == Ruolo.GESTORE);

        // Mostra/Nascondi intero blocco gestore
        if (boxGestore != null) {
            boxGestore.setVisible(isGestore);
            boxGestore.setManaged(isGestore);
        }

        if (isGestore) {
            lblTitoloOp1.setText("Configurazione Campi");
            btnOp1.setText("Gestisci Regole");
            
            lblTitoloOp2.setText("Parametri Economici");
            btnOp2.setText("Gestione Penalità");
        } else {
            lblTitoloOp1.setText("Prenotazione Campi");
            btnOp1.setText("Nuova Prenotazione");
            
            lblTitoloOp2.setText("Gestione Account");
            btnOp2.setText("Invia Disdetta");
        }
    }

    private void goTo(String route) {
        if (navigator == null) return;
        Map<String, Object> params = (sessione != null) 
            ? Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione) 
            : Map.of();
        navigator.goTo(route, params);
    }

    @FXML public void onAccount() { goTo(GraphicControllerUtils.ROUTE_ACCOUNT); }

    @FXML 
    public void onOp1() {
        if (ruolo == Ruolo.GESTORE) {
            goTo(GraphicControllerUtils.ROUTE_REGOLE);
        } else {
            goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE);
        }
    }

    @FXML 
    public void onOp2() {
        if (ruolo == Ruolo.GESTORE) {
            goTo(GraphicControllerUtils.ROUTE_PENALITA);
        } else {
            goTo(GraphicControllerUtils.ROUTE_DISDETTA);
        }
    }

    @FXML public void onLog() { goTo(GraphicControllerUtils.ROUTE_LOGS); }
    @FXML public void onRichiesteDisdetta() { goTo(GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA); }
    @FXML public void onLogout() { goTo(GraphicControllerUtils.ROUTE_LOGIN); }
    
    public void render(Map<String, Object> params) { }
}