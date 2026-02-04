package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicLoginController;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso;


/**
 * Adapter CLI per il Login.
 * 
 * Pattern: Adapter
 * - Adatta form login (View) → Bean → LogicController
 * - Riceve SessioneUtenteBean dal LogicController
 * - Gestisce navigazione post-login
 * 
 * Responsabilità:
 * - Riceve DatiLoginBean dalla View (form compilato)
 * - Delega verifica credenziali al LogicControllerGestioneAccesso
 * - Riceve SessioneUtenteBean dal LogicController
 * - Gestisce navigazione (home, registrazione)
 */
public class CLIGraphicLoginController implements GraphicLoginController {
    
    // ==================== Dependencies ====================
    private final GraphicControllerNavigation navigator;
    
    // ==================== Constructors ====================
    public CLIGraphicLoginController(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    // ==================== NavigableController ====================
    @Override
    public String getRouteName() {
        return "login";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Lifecycle: quando view login è mostrata
        // Es. pulire campi, mostrare messaggio benvenuto
    }

    // ==================== Business Methods ====================
    
    /**
     * Effettua login.
     * 
     * Flusso:
     * 1. View compila form → crea DatiLoginBean
     * 2. View chiama questo metodo
     * 3. Deleghiamo a LogicController.verificaCredenziali()
     * 4. LogicController restituisce SessioneUtenteBean (se valido) o null
     * 5. Se SessioneUtenteBean != null → login OK → salva log e naviga a home
     * 6. Se null → login fallito → notifica View
     * 
     * @param credenziali DatiLoginBean con email e password
     */
    @Override
    public void effettuaLogin(DatiLoginBean credenziali) {
        if (credenziali == null) {
            // Notifica View: credenziali mancanti
            return;
        }
        
        // DELEGAZIONE: verifica credenziali tramite LogicController (creato on-demand)
        LogicControllerGestioneAccesso logicController = new LogicControllerGestioneAccesso();
        SessioneUtenteBean sessione = logicController.verificaCredenziali(credenziali);
        
        if (sessione != null) {
            // Login riuscito
            // Salva log di accesso
            logicController.saveLog(sessione);
            
            // Passa SessioneUtenteBean alla View per memorizzarla
            // La View memorizzerà sessioneCorrente = sessione
            
            // Navigazione a home
            vaiAHome();
        } else {
            // Login fallito
            // Notifica View: credenziali errate
        }
    }

    /**
     * Logout.
     * 
     * Flusso:
     * 1. View chiama logout
     * 2. Invalida sessioneUtenteBean nella View
     * 3. Naviga a login
     */
    @Override
    public void logout() {
        // La View dovrà:
        // - Invalidare sessioneCorrente (= null)
        // - Pulire UI
        
        // Navigazione a login
        if (navigator != null) {
            navigator.goTo("login");
        }
    }

    /**
     * Naviga alla schermata di registrazione.
     */
    @Override
    public void vaiARegistrazione() {
        if (navigator != null) {
            navigator.goTo("registrazione");
        }
    }

    /**
     * Naviga alla home.
     */
    @Override
    public void vaiAHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
