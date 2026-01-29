package com.ispw.controller.factory;



public final class CLIFrontendControllerFactory extends FrontendControllerFactory {

    public CLIFrontendControllerFactory() { }

    @Override
    public void startApplication() {
        System.out.println("Avvio CLI...");
        // new CLINavigator().start();
    }
}
