package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller per la Dashboard principale.
 * Gestisce la visualizzazione dinamica in base al ruolo dell'utente (Gestore/Cliente).
 */
public class HomeFXMLController {

    private GraphicControllerNavigation navigator;
    private SessioneUtenteBean sessione;
    private Ruolo ruolo;

    @FXML private Label lblRuolo;
    @FXML private Button btnOp1;
    @FXML private Button btnOp2;
    @FXML private Button btnLog;
    @FXML private Button btnRichiesteDisdetta;
    //@FXML private Button btnAccount; // Aggiunto se presente nell'FXML per onAccount

    /**
     * Inizializza la dashboard con i dati della sessione.
     */
    public void init(GraphicControllerNavigation navigator, SessioneUtenteBean sessione) {
        this.navigator = navigator;
        this.sessione = sessione;
        
        // Fix potenziale null su sessione o utente
        this.ruolo = (sessione != null && sessione.getUtente() != null) 
                     ? sessione.getUtente().getRuolo() 
                     : null;

        updateUI();
    }

    /**
     * Aggiorna gli elementi grafici in base al ruolo.
     */
    private void updateUI() {
        if (lblRuolo != null) {
            lblRuolo.setText(ruolo != null ? "Ruolo: " + ruolo : "Ruolo: -");
        }

        // Testi di default per evitare bottoni vuoti
        if (btnLog != null) btnLog.setText("Log Attività");
        if (btnRichiesteDisdetta != null) btnRichiesteDisdetta.setText("Richieste Disdetta");

        boolean isGestore = (ruolo == Ruolo.GESTORE);

        if (isGestore) {
            configureButton(btnOp1, "Configura Regole", true);
            configureButton(btnOp2, "Gestione Penalità", true);
            setVisible(btnLog, true);
            setVisible(btnRichiesteDisdetta, true);
        } else {
            configureButton(btnOp1, "Nuova Prenotazione", true);
            configureButton(btnOp2, "Invia Disdetta", true);
            setVisible(btnLog, false);
            setVisible(btnRichiesteDisdetta, false);
        }
    }

    /**
     * Helper per configurare testo e visibilità in un colpo solo.
     */
    private void configureButton(Button b, String text, boolean visible) {
        if (b != null) {
            b.setText(text);
            setVisible(b, visible);
        }
    }

    private void setVisible(Button b, boolean v) {
        if (b == null) return;
        b.setVisible(v);
        b.setManaged(v);
    }

    private void goTo(String route) {
        if (navigator == null) return;
        
        // Passaggio sicuro della sessione
        Map<String, Object> params = (sessione != null) 
            ? Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione) 
            : Map.of();
            
        navigator.goTo(route, params);
    }

    // --- GESTIONE EVENTI (Rifattorizzati per eliminare i Warning) ---

    @FXML 
    public void onAccount() { 
        goTo(GraphicControllerUtils.ROUTE_ACCOUNT); 
    }

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

    @FXML 
    public void onLog() { 
        goTo(GraphicControllerUtils.ROUTE_LOGS); 
    }

    @FXML 
    public void onRichiesteDisdetta() { 
        goTo(GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA); 
    }

    @FXML 
    public void onLogout() { 
        goTo(GraphicControllerUtils.ROUTE_LOGIN); 
    }
    
    public void render(Map<String, Object> params) {
        // Implementazione opzionale per messaggi dinamici
    }
}