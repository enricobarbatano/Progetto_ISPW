package com.ispw.controller.factory;

import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.graphic.GraphicLoginController;
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

    // Factory methods per creare i GraphicController
    public abstract GraphicLoginController createLoginController();
    public abstract GraphicControllerAccount createAccountController();
    public abstract GraphicControllerRegistrazione createRegistrazioneController();
    public abstract GraphicControllerPrenotazione createPrenotazioneController();
    public abstract GraphicControllerDisdetta createDisdettaController();
    public abstract GraphicControllerRegole createRegoleController();
    public abstract GraphicControllerPenalita createPenalitaController();
    public abstract GraphicControllerNavigation createNavigationController();
}



