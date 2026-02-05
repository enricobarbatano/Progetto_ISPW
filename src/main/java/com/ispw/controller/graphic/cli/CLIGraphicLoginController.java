package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicLoginController;


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
public class CLIGraphicLoginController extends AbstractGraphicLoginController {
    
    // ==================== Constructors ====================
    public CLIGraphicLoginController(GraphicControllerNavigation navigator) {
        super(navigator);
    }
    
    // ==================== NavigableController ====================
    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, GraphicControllerUtils.PREFIX_LOGIN);
    }

    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToLoginWithError(String message) {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN,
                Map.of(GraphicControllerUtils.KEY_ERROR, message));
        }
    }

    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
        }
    }

    @Override
    protected void goToRegistrazione() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGISTRAZIONE);
        }
    }

    @Override
    protected void goToHome(com.ispw.bean.SessioneUtenteBean sessione) {
        if (navigator != null) {
            if (sessione == null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
            } else {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME,
                    Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
            }
        }
    }

    /**
     * Login con dati grezzi: l’adattamento in bean resta nel controller grafico.
     */
    public void effettuaLoginRaw(String email, String password) {
        if (email == null && password == null) {
            effettuaLogin(null);
            return;
        }
        effettuaLogin(new DatiLoginBean(email, password));
    }
}
