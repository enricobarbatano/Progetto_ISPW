package com.ispw.controller.graphic.cli;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPrenotazione;

/**
 * Adapter CLI per la prenotazione campo.
 */
public class CLIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione {
    
    public CLIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }
    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void notifyPrenotazioneError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
            GraphicControllerUtils.PREFIX_PRENOT, message);
    }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

}
