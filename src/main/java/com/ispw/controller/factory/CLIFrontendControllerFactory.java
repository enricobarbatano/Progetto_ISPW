// src/main/java/com/ispw/controller/factory/CLIFrontendControllerFactory.java
package com.ispw.controller.factory;

import java.util.logging.Logger;

public final class CLIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(CLIFrontendControllerFactory.class.getName());

    public CLIFrontendControllerFactory() { }

    @Override
    public void startApplication() {
        logger.info("Avvio CLI...");

    
    }
} 
