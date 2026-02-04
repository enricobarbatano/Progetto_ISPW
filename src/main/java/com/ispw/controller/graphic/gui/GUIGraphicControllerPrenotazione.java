package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPrenotazione;

/**
 * Adapter GUI per la prenotazione campo.
 * Trasforma dati grezzi (String, int) in bean per il LogicController.
 */
public class GUIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione {
    
    public GUIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }
    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void notifyPrenotazioneError(String message) {
        if (log() != null) {
            log().warning(message);
        }
    }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }
    
}
