package com.ispw.controller.factory;

import java.util.logging.Logger;

public final class GUIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(GUIFrontendControllerFactory.class.getName());

    public GUIFrontendControllerFactory() { }

    @Override
    public void startApplication() {
        logger.info("Avvio GUI...");
        // GuiLauncher.launchApp();
    }
}

