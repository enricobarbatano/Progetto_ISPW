package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPrenotazione;

public class GUIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: estende AbstractGraphicControllerPrenotazione e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: notifica errori e routing verso home.
    // A3) Logica delegata: ereditata dalla classe astratta.
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

    // SEZIONE LOGICA
    // Legenda metodi: nessun helper privato.

}
