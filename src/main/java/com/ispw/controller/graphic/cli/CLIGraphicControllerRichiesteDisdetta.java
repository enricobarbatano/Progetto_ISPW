package com.ispw.controller.graphic.cli;

import java.util.logging.Logger;

import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

public class CLIGraphicControllerRichiesteDisdetta extends AbstractGraphicControllerRichiesteDisdetta {

    // SEZIONE ARCHITETTURALE
    // A1) Concreto CLI: estende AbstractGraphicControllerRichiesteDisdetta
    // A2) Differenze CLI/GUI: solo log() e goToHome()
    // A3) Nessuna logica applicativa: demandata al logic controller

    public CLIGraphicControllerRichiesteDisdetta(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }
}