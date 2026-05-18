package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico GUI del flusso gestore del caso d'uso "Disdici prenotazione".
 *
 * Questa classe contiene solo le parti specifiche della GUI:
 * - definizione del logger;
 * - navigazione verso home.
 *
 * La logica comune di caricamento, approvazione e rifiuto delle richieste
 * viene ereditata dalla classe astratta.
 */
public class GUIGraphicControllerRichiesteDisdetta extends AbstractGraphicControllerRichiesteDisdetta {

    public GUIGraphicControllerRichiesteDisdetta(GraphicControllerNavigation navigator) {
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