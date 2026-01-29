package com.ispw.controller.factory;



import com.ispw.model.enums.FrontendProvider;

public abstract class FrontendControllerFactory {

    private static FrontendProvider provider;
    private static FrontendControllerFactory instance;

    // Imposta il provider UNA SOLA VOLTA nel bootstrap
    public static void setFrontendProvider(FrontendProvider p) {
        if (provider != null) {
            throw new IllegalStateException("FrontendProvider già impostato. Non puoi cambiarlo a runtime.");
        }
        provider = p;
    }

    // Restituisce sempre la stessa istanza (Singleton)
    public static FrontendControllerFactory getInstance() {
        if (provider == null) {
            throw new IllegalStateException("FrontendProvider non configurato. Chiama setFrontendProvider() prima.");
        }

        if (instance == null) {
            instance = switch (provider) {
                case CLI -> new CLIFrontendControllerFactory();
                case GUI -> new GUIFrontendControllerFactory();
            };
        }
        return instance;
    }

    public abstract void startApplication();
}



