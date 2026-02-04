package com.ispw.controller.graphic.gui;

import java.util.Map;
import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicLoginController;

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
    protected void goToHome(com.ispw.bean.SessioneUtenteBean sessione) {
        if (navigator != null) {
            if (sessione == null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
            } else {
                navigator.goTo(GraphicControllerUtils.ROUTE_HOME,
                    Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
            }
        }
    }
}
