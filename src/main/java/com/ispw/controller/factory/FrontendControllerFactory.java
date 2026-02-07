package com.ispw.controller.factory;

import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerLog;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.graphic.GraphicLoginController;
import com.ispw.model.enums.FrontendProvider;

public abstract class FrontendControllerFactory {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: factory astratta per controller grafici (CLI/GUI).
    // A2) Stato: provider scelto a bootstrap e istanza singleton.

    private static FrontendProvider provider;
    private static FrontendControllerFactory instance;

    public static void setFrontendProvider(FrontendProvider p) {
        if (provider != null) {
            throw new IllegalStateException("FrontendProvider giÃ  impostato. Non puoi cambiarlo a runtime.");
        }
        provider = p;
    }

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

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) startApplication: avvio UI.
    // L2) create*Controller: factory dei controller grafici.

    public abstract void startApplication();

    public abstract GraphicLoginController createLoginController();
    public abstract GraphicControllerAccount createAccountController();
    public abstract GraphicControllerRegistrazione createRegistrazioneController();
    public abstract GraphicControllerPrenotazione createPrenotazioneController();
    public abstract GraphicControllerDisdetta createDisdettaController();
    public abstract GraphicControllerRegole createRegoleController();
    public abstract GraphicControllerPenalita createPenalitaController();
    public abstract GraphicControllerLog createLogController();
    public abstract GraphicControllerNavigation createNavigationController();
}



