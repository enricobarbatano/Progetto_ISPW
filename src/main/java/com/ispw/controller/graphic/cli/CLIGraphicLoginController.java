package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
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
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

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
        GraphicControllerUtils.handleOnShow(log(), params, "[LOGIN]");
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
            if (navigator != null) {
                navigator.goTo("login", Map.of("error", "Credenziali mancanti"));
            }
            return;
        }
        
        LogicControllerGestioneAccesso logicController = new LogicControllerGestioneAccesso();
        SessioneUtenteBean sessione = logicController.verificaCredenziali(credenziali);
        
        if (sessione != null) {
            logicController.saveLog(sessione);
            if (navigator != null) {
                navigator.goTo("home", Map.of("sessione", sessione));
            }
        } else {
            if (navigator != null) {
                navigator.goTo("login", Map.of("error", "Credenziali non valide"));
            }
        }
    }

    /**
     * Logout.
     */
    @Override
    public void logout() {
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
