package com.ispw.controller.graphic.gui;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicLoginController;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso;

/**
 * Adapter GUI per il login (JavaFX/Swing).
 */
public class GUIGraphicLoginController extends AbstractGraphicLoginController {
    
    public GUIGraphicLoginController(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
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
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);
        }
    }

    @Override
    protected void goToRegistrazione() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGISTRAZIONE, null);
        }
    }

    @Override
    protected void goToHome(SessioneUtenteBean sessione) {
        if (navigator != null) {
            if (sessione == null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
            } else {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME,
                    Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
            }
        }
    }

    @Override
    protected SessioneUtenteBean verificaCredenziali(DatiLoginBean credenziali) {
        return new LogicControllerGestioneAccesso().verificaCredenziali(credenziali);
    }

    @Override
    protected void salvaLog(SessioneUtenteBean sessione) {
        new LogicControllerGestioneAccesso().saveLog(sessione);
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
