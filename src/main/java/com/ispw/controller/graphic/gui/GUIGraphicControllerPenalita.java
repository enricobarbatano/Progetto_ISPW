package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico GUI del caso d'uso "Applica penalità".
 *
 * Questa classe contiene solo le parti specifiche della GUI:
 * - definizione del logger;
 * - navigazione verso home.
 *
 * La logica comune di selezione utente e applicazione penalità viene ereditata
 * dalla classe astratta.
 */
public class GUIGraphicControllerPenalita extends AbstractGraphicControllerPenalita {

    public GUIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Torna alla home.
     */
    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }
}