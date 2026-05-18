package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico GUI del caso d'uso "Prenota campo".
 *
 * Questa classe contiene solo le parti specifiche della GUI:
 * - definizione del logger;
 * - notifica degli errori;
 * - navigazione verso home.
 *
 * La logica comune della prenotazione viene ereditata dalla classe astratta.
 */
public class GUIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione {

    public GUIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Notifica un errore relativo alla prenotazione.
     *
     * Nella versione GUI, al momento, l'errore viene scritto nel logger.
     */
    @Override
    protected void notifyPrenotazioneError(String message) {
        log().warning(message);
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