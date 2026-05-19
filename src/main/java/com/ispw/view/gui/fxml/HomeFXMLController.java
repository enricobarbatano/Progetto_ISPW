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

/**
 * Controller FXML per la home page.
 *
 * RESPONSABILITÀ:
 * - mostrare il ruolo dell'utente corrente;
 * - configurare il menu in base al ruolo;
 * - leggere i click sui pulsanti;
 * - delegare la navigazione al navigator.
 *
 * NON:
 * - crea bean;
 * - chiama logic controller;
 * - accede a DAO o persistenza;
 * - contiene logica applicativa dei casi d'uso.
 *
 * Nota:
 * la home è un hub di navigazione. Per questo motivo usa direttamente
 * il navigator ricevuto dalla GUIHomeView.
 */
public class HomeFXMLController {

    private GraphicControllerNavigation navigator;
    private SessioneUtenteBean sessione;
    private Ruolo ruolo;

    @FXML private Label lblRuolo;
    @FXML private Label lblTitoloOp1;
    @FXML private Label lblTitoloOp2;

    @FXML private Button btnOp1;
    @FXML private Button btnOp2;
    @FXML private Button btnLog;
    @FXML private Button btnRichiesteDisdetta;

    @FXML private VBox boxGestore;

    /**
     * Inizializza il controller con navigator e sessione.
     *
     * @param navigator navigator centrale dell'applicazione
     * @param sessione sessione utente corrente
     */
    public void init(GraphicControllerNavigation navigator, SessioneUtenteBean sessione) {
        this.navigator = navigator;
        this.sessione = sessione;
        this.ruolo = extractRuolo(sessione);

        updateUI();
    }

    /**
     * Renderizza eventuali dati.
     *
     * La home non usa parametri aggiuntivi oltre alla sessione passata in init().
     *
     * @param params parametri della route corrente
     */
    @SuppressWarnings("java:S1172")
    public void render(Map<String, Object> params) {
        // Metodo mantenuto per uniformità con gli altri controller FXML.
    }

    /**
     * Naviga alla gestione account.
     */
    @FXML
    public void onAccount() {
        goTo(GraphicControllerUtils.ROUTE_ACCOUNT);
    }

    /**
     * Naviga alla prima operazione principale, diversa in base al ruolo.
     */
    @FXML
    public void onOp1() {
        if (isGestore()) {
            goTo(GraphicControllerUtils.ROUTE_REGOLE);
        } else {
            goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE);
        }
    }

    /**
     * Naviga alla seconda operazione principale, diversa in base al ruolo.
     */
    @FXML
    public void onOp2() {
        if (isGestore()) {
            goTo(GraphicControllerUtils.ROUTE_PENALITA);
        } else {
            goTo(GraphicControllerUtils.ROUTE_DISDETTA);
        }
    }

    /**
     * Naviga alla schermata dei log.
     */
    @FXML
    public void onLog() {
        goTo(GraphicControllerUtils.ROUTE_LOGS);
    }

    /**
     * Naviga alla schermata delle richieste di disdetta.
     */
    @FXML
    public void onRichiesteDisdetta() {
        goTo(GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA);
    }

    /**
     * Effettua il logout tornando al login.
     */
    @FXML
    public void onLogout() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, Map.of());
        }
    }

    /**
     * Aggiorna testi e visibilità del menu in base al ruolo.
     */
    private void updateUI() {
        updateRuoloLabel();
        updateGestoreBox();
        updateMainActions();
    }

    /**
     * Aggiorna la label del ruolo.
     */
    private void updateRuoloLabel() {
        if (lblRuolo != null) {
            lblRuolo.setText("Sessione attiva come: " + (ruolo != null ? ruolo : "Ospite"));
        }
    }

    /**
     * Mostra o nasconde il pannello riservato al gestore.
     */
    private void updateGestoreBox() {
        boolean visible = isGestore();

        if (boxGestore != null) {
            boxGestore.setVisible(visible);
            boxGestore.setManaged(visible);
        }
    }

    /**
     * Aggiorna le due azioni principali della home.
     */
    private void updateMainActions() {
        if (isGestore()) {
            setText(lblTitoloOp1, "Configurazione Campi");
            setText(btnOp1, "Gestisci Regole");

            setText(lblTitoloOp2, "Parametri Economici");
            setText(btnOp2, "Gestione Penalità");
            return;
        }

        setText(lblTitoloOp1, "Prenotazione Campi");
        setText(btnOp1, "Nuova Prenotazione");

        setText(lblTitoloOp2, "Gestione Account");
        setText(btnOp2, "Invia Disdetta");
    }

    /**
     * Naviga a una route passando la sessione, se presente.
     */
    private void goTo(String route) {
        if (navigator == null || route == null) {
            return;
        }

        Map<String, Object> params = sessione != null
                ? Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione)
                : Map.of();

        navigator.goTo(route, params);
    }

    /**
     * Estrae il ruolo dalla sessione.
     */
    private Ruolo extractRuolo(SessioneUtenteBean sessione) {
        return sessione != null && sessione.getUtente() != null
                ? sessione.getUtente().getRuolo()
                : null;
    }

    /**
     * Controlla se l'utente corrente è gestore.
     */
    private boolean isGestore() {
        return ruolo == Ruolo.GESTORE;
    }

    /**
     * Imposta testo su una Label.
     */
    private void setText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    /**
     * Imposta testo su un Button.
     */
    private void setText(Button button, String value) {
        if (button != null) {
            button.setText(value);
        }
    }
}