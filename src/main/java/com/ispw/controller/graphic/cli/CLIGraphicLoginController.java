package com.ispw.controller.graphic.cli;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.abstracts.AbstractGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico CLI del caso d'uso "Login".
 *
 * Questa classe contiene solo le parti specifiche della CLI:
 * - gestione dei parametri mostrati quando la route viene aperta;
 * - navigazione verso login;
 * - navigazione verso registrazione;
 * - navigazione verso home.
 *
 * La verifica credenziali e il salvataggio del log sono ereditati dalla classe astratta,
 * che delega al controller logico tramite LogicControllerFactory.
 */
public class CLIGraphicLoginController extends AbstractGraphicLoginController {

    public CLIGraphicLoginController(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(
                log(),
                params,
                GraphicControllerUtils.PREFIX_LOGIN
        );
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Torna alla schermata di login mostrando un errore.
     */
    @Override
    protected void goToLoginWithError(String message) {
        if (navigator != null) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_LOGIN,
                    Map.of(GraphicControllerUtils.KEY_ERROR, message)
            );
        }
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
     * Naviga verso la schermata di registrazione.
     */
    @Override
    protected void goToRegistrazione() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGISTRAZIONE);
        }
    }

    /**
     * Naviga verso la home.
     *
     * Se la sessione è presente, viene passata alla schermata successiva.
     */
    @Override
    protected void goToHome(SessioneUtenteBean sessione) {
        if (navigator == null) {
            return;
        }

        if (sessione == null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
            return;
        }

        navigator.goTo(
                GraphicControllerUtils.ROUTE_HOME,
                Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione)
        );
    }
}