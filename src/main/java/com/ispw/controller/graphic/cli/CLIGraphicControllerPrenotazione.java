package com.ispw.controller.graphic.cli;

import java.util.logging.Logger;

import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

/**
 * Controller grafico CLI del caso d'uso "Prenota campo".
 *
 * Questa classe contiene solo le parti specifiche della CLI:
 * - definizione del logger;
 * - notifica degli errori;
 * - navigazione verso home.
 *
 * La logica comune della prenotazione viene ereditata dalla classe astratta.
 */
public class CLIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione {

    public CLIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Notifica un errore relativo alla prenotazione.
     */
    @Override
    protected void notifyPrenotazioneError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                GraphicControllerUtils.PREFIX_PRENOT,
                message
        );
    }

    /**
     * Torna alla home.
     */
    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }
}