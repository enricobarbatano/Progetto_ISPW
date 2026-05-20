package com.ispw.controller.graphic.factory;

import com.ispw.controller.graphic.interfaces.GraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicLoginController;
import com.ispw.model.enums.FrontendProvider;


/**
 * Factory astratta per i controller grafici.
 *
 * Il Singleton è intenzionale:
 * il frontend provider viene scelto una sola volta a bootstrap,
 * così tutta l'applicazione usa sempre la stessa configurazione frontend
 * evitando incoerenze tra CLI e GUI.
 */

@SuppressWarnings("java:S6548")
public abstract class FrontendControllerFactory {

    // Stato: provider scelto a bootstrap e istanza singleton
    private static FrontendProvider provider;
    private static FrontendControllerFactory instance;

    public static void setFrontendProvider(FrontendProvider p) {
        if (provider != null) {
            throw new IllegalStateException("FrontendProvider gia impostato. Non puoi cambiarlo a runtime.");
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

    // Avvio UI
    public abstract void startApplication();

    // Factory controller grafici (CLI/GUI)
    public abstract GraphicLoginController createLoginController();
    public abstract GraphicControllerAccount createAccountController();
    public abstract GraphicControllerRegistrazione createRegistrazioneController();
    public abstract GraphicControllerPrenotazione createPrenotazioneController();
    public abstract GraphicControllerDisdetta createDisdettaController();
    public abstract GraphicControllerRegole createRegoleController();
    public abstract GraphicControllerPenalita createPenalitaController();
    public abstract GraphicControllerLog createLogController();

    // ✅ Nuovo: richieste disdetta (gestore)
    public abstract GraphicControllerRichiesteDisdetta createRichiesteDisdettaController();

    public abstract GraphicControllerNavigation createNavigationController();
}