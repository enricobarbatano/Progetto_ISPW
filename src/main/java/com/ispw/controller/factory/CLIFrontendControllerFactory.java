// src/main/java/com/ispw/controller/factory/CLIFrontendControllerFactory.java
package com.ispw.controller.factory;

import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.graphic.GraphicLoginController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerAccount;
import com.ispw.controller.graphic.cli.CLIGraphicControllerDisdetta;
import com.ispw.controller.graphic.cli.CLIGraphicControllerNavigation;
import com.ispw.controller.graphic.cli.CLIGraphicControllerPenalita;
import com.ispw.controller.graphic.cli.CLIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegole;
import com.ispw.controller.graphic.cli.CLIGraphicLoginController;

public final class CLIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(CLIFrontendControllerFactory.class.getName());
    private CLIGraphicControllerNavigation navigationController;
    private GraphicLoginController loginController;
    private GraphicControllerAccount accountController;
    private GraphicControllerRegistrazione registrazioneController;
    private GraphicControllerPrenotazione prenotazioneController;
    private GraphicControllerDisdetta disdettaController;
    private GraphicControllerRegole regoleController;
    private GraphicControllerPenalita penalitaController;

    @Override
    public void startApplication() {
        logger.info("Avvio CLI...");
        // Avvia il flusso con la schermata di login
        GraphicLoginController loginGraphicController = createLoginController();
        loginGraphicController.onShow(null);
        // La CLI leggerà input da console e chiamerà loginController.effettuaLogin(...)
    }

    @Override
    public GraphicLoginController createLoginController() {
        if (loginController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            loginController = new CLIGraphicLoginController(navigator);
        }
        return loginController;
    }

    @Override
    public GraphicControllerAccount createAccountController() {
        if (accountController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            accountController = new CLIGraphicControllerAccount(navigator);
        }
        return accountController;
    }

    @Override
    public GraphicControllerRegistrazione createRegistrazioneController() {
        if (registrazioneController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            registrazioneController = new CLIGraphicControllerRegistrazione(navigator);
        }
        return registrazioneController;
    }

    @Override
    public GraphicControllerPrenotazione createPrenotazioneController() {
        if (prenotazioneController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            prenotazioneController = new CLIGraphicControllerPrenotazione(navigator);
        }
        return prenotazioneController;
    }

    @Override
    public GraphicControllerDisdetta createDisdettaController() {
        if (disdettaController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            disdettaController = new CLIGraphicControllerDisdetta(navigator);
        }
        return disdettaController;
    }

    @Override
    public GraphicControllerRegole createRegoleController() {
        if (regoleController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            regoleController = new CLIGraphicControllerRegole(navigator);
        }
        return regoleController;
    }

    @Override
    public GraphicControllerPenalita createPenalitaController() {
        if (penalitaController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            penalitaController = new CLIGraphicControllerPenalita(navigator);
        }
        return penalitaController;
    }

    @Override
    public GraphicControllerNavigation createNavigationController() {
        if (navigationController == null) {
            navigationController = new CLIGraphicControllerNavigation();
            registerRoutes();
        }
        return navigationController;
    }

    private GraphicControllerNavigation getNavigationController() {
        return createNavigationController();  // Delega al metodo pubblico
    }

    private void registerRoutes() {
        // Registrazione nelle factory concrete: conoscono i controller CLI reali e il navigator concreto.
        // Nell'astratta introdurrebbe dipendenze verso classi concrete, violando il disaccoppiamento.
        navigationController.registerRoute(createLoginController().getRouteName(), createLoginController());
        navigationController.registerRoute(createAccountController().getRouteName(), createAccountController());
        navigationController.registerRoute(createRegistrazioneController().getRouteName(), createRegistrazioneController());
        navigationController.registerRoute(createPrenotazioneController().getRouteName(), createPrenotazioneController());
        navigationController.registerRoute(createDisdettaController().getRouteName(), createDisdettaController());
        navigationController.registerRoute(createRegoleController().getRouteName(), createRegoleController());
        navigationController.registerRoute(createPenalitaController().getRouteName(), createPenalitaController());
    }
}  
