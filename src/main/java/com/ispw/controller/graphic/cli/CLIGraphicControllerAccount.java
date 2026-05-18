package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico CLI del caso d'uso "Gestione account".
 *
 * Questa classe contiene solo le parti specifiche della CLI:
 * - definizione del logger;
 * - navigazione verso login;
 * - navigazione verso home.
 *
 * La logica comune del caso d'uso viene ereditata dalla classe astratta.
 * La logica applicativa viene delegata al logic controller tramite factory
 * nella classe astratta.
 */
public class CLIGraphicControllerAccount extends AbstractGraphicControllerAccount {

    public CLIGraphicControllerAccount(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Torna alla schermata di login.
     */
    @Override
    protected void goToLogin() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN);
        }
    }

    /**
     * Torna alla home.
     *
     * Se la sessione è presente, viene passata alla schermata successiva.
     */
    @Override
    protected void goToHome(SessioneUtenteBean sessione) {
        if (navigator == null) {
            return;
        }

        if (sessione != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME,
                    Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
            return;
        }

        navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
    }
}
