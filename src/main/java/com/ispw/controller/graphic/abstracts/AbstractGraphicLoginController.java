package com.ispw.controller.graphic.abstracts;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.GraphicLoginController;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlAccesso;

/**
 * Controller grafico astratto del caso d'uso "Login".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - riceve input grezzi dalla view;
 * - costruisce il bean di login;
 * - controlla le credenziali ricevute;
 * - chiama il controller logico di accesso;
 * - salva il log di accesso;
 * - naviga verso home, login o registrazione.
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

    protected CtrlAccesso logicController() {
        return LogicControllerFactory.getAccessoController();
    }

    protected SessioneUtenteBean verificaCredenziali(DatiLoginBean credenziali) {
        return logicController().verificaCredenziali(credenziali);
    }

    protected void salvaLog(SessioneUtenteBean sessione) {
        logicController().saveLog(sessione);
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // In questa schermata non è necessario gestire parametri comuni.
    }

    /**
     * Effettua il login partendo da input grezzi ricevuti dalla view.
     *
     * Il bean DatiLoginBean viene costruito qui, non nella view.
     */
    @Override
    public void effettuaLogin(String email, String password) {
        if (email == null || password == null) {
            goToLoginWithError(GraphicControllerUtils.MSG_CREDENZIALI_MANCANTI);
            return;
        }

        DatiLoginBean credenziali = new DatiLoginBean(email, password);

        try {
            SessioneUtenteBean sessione = verificaCredenziali(credenziali);

            if (sessione != null) {
                salvaLog(sessione);
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