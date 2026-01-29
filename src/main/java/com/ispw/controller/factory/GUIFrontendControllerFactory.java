package com.ispw.controller.factory;


public final class GUIFrontendControllerFactory extends FrontendControllerFactory {

    public GUIFrontendControllerFactory() { }

    @Override
    public void startApplication() {
        System.out.println("Avvio GUI...");
        // GuiLauncher.launchApp();
    }
}

