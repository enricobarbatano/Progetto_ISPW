package com.ispw.controller.graphic.cli;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPenalita;

public class CLIGraphicControllerPenalita extends AbstractGraphicControllerPenalita {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: estende AbstractGraphicControllerPenalita e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: routing verso home.
    // A3) Logica delegata: ereditata dalla classe astratta.
    
    public CLIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

    // SEZIONE LOGICA
    // Legenda metodi: nessun helper privato.

}
