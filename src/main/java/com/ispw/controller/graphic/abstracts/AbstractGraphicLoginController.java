package com.ispw.controller.graphic.abstracts;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.GraphicLoginController;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso;

/**
 * Classe astratta che centralizza la logica comune dei controller grafici Login
 * (CLI/GUI) per ridurre duplicazione. Non introduce nuove responsabilità né
 * modifica il disaccoppiamento: delega invariata ai LogicController e mantiene
 * la stessa navigazione verso la View tramite GraphicControllerNavigation.
 */
public abstract class AbstractGraphicLoginController implements GraphicLoginController {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicLoginController(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToLoginWithError(String message);

    protected abstract void goToLogin();

    protected abstract void goToRegistrazione();

    protected abstract void goToHome(SessioneUtenteBean sessione);

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // lifecycle hook (override if needed)
    }

    @Override
    public void effettuaLogin(DatiLoginBean credenziali) {
        if (credenziali == null) {
            goToLoginWithError(GraphicControllerUtils.MSG_CREDENZIALI_MANCANTI);
            return;
        }

        LogicControllerGestioneAccesso logicController = new LogicControllerGestioneAccesso();
        try {
            SessioneUtenteBean sessione = logicController.verificaCredenziali(credenziali);
            if (sessione != null) {
                logicController.saveLog(sessione);
                goToHome(sessione);
            } else {
                goToLoginWithError(GraphicControllerUtils.MSG_CREDENZIALI_NON_VALIDE);
            }
        } catch (IllegalStateException ex) {
            goToLoginWithError(ex.getMessage());
        }
    }

    @Override
    public void logout() {
        goToLogin();
    }

    @Override
    public void vaiARegistrazione() {
        goToRegistrazione();
    }

    @Override
    public void vaiAHome() {
        goToHome(null);
    }
}
