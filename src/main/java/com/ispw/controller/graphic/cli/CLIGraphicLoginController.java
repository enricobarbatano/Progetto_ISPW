package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicLoginController;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso;

public class CLIGraphicLoginController extends AbstractGraphicLoginController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: estende AbstractGraphicLoginController e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve DatiLoginBean, ritorna SessioneUtenteBean.
    // A3) Logica delegata: usa LogicControllerGestioneAccesso.
    // ========================
    public CLIGraphicLoginController(GraphicControllerNavigation navigator) {
        super(navigator);
    }

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
    protected void goToHome(SessioneUtenteBean sessione) {
        if (navigator != null) {
            if (sessione == null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
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

    // ========================
    // SEZIONE LOGICA
    // Legenda metodi: nessun helper privato.
    // ========================

}
