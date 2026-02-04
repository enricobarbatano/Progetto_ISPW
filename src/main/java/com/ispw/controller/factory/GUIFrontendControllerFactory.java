package com.ispw.controller.factory;

import java.util.logging.Logger;

public final class GUIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(GUIFrontendControllerFactory.class.getName());

    @Override
    public void startApplication() {
        logger.info("Avvio GUI...");
        // Implementazione specifica per l'avvio dell'applicazione GUI
    }
} 

