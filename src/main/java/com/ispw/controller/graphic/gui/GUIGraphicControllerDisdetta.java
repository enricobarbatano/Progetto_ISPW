package com.ispw.controller.graphic.gui;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerDisdetta;

public class GUIGraphicControllerDisdetta extends AbstractGraphicControllerDisdetta {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: estende AbstractGraphicControllerDisdetta e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: routing verso home.
    // A3) Logica delegata: ereditata dalla classe astratta.
    // ========================
    
    public GUIGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }

    // ========================
    // SEZIONE LOGICA
    // Legenda metodi: nessun helper privato.
    // ========================

}
