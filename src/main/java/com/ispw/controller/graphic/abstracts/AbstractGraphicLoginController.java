package com.ispw.controller.graphic.abstracts;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.GraphicLoginController;

public abstract class AbstractGraphicLoginController implements GraphicLoginController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa GraphicLoginController (interfaccia) e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve DatiLoginBean, ritorna SessioneUtenteBean.
    // A3) Logica delegata: demandata ai controller concreti.
    // ========================

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicLoginController(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToLoginWithError(String message);

    protected abstract void goToLogin();

    protected abstract void goToRegistrazione();

    protected abstract void goToHome(SessioneUtenteBean sessione);

    protected abstract SessioneUtenteBean verificaCredenziali(DatiLoginBean credenziali);

    protected abstract void salvaLog(SessioneUtenteBean sessione);

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void effettuaLogin(DatiLoginBean credenziali) {
        if (credenziali == null) {
            goToLoginWithError(GraphicControllerUtils.MSG_CREDENZIALI_MANCANTI);
            return;
        }

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

    // ========================
    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) effettuaLoginRaw(...) - adapter per input grezzi.
    // ========================
    public void effettuaLoginRaw(String email, String password) {
        if (email == null && password == null) {
            effettuaLogin(null);
            return;
        }
        effettuaLogin(new DatiLoginBean(email, password));
    }
}
