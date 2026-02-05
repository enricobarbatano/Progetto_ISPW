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
import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.gui.GUIGraphicControllerNavigation;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.gui.GUIGraphicLoginController;

public final class GUIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(GUIFrontendControllerFactory.class.getName());
    private GUIGraphicControllerNavigation navigationController;
    private GraphicLoginController loginController;
    private GraphicControllerAccount accountController;
    private GraphicControllerRegistrazione registrazioneController;
    private GraphicControllerPrenotazione prenotazioneController;
    private GraphicControllerDisdetta disdettaController;
    private GraphicControllerRegole regoleController;
    private GraphicControllerPenalita penalitaController;

    @Override
    public void startApplication() {
        logger.info("Avvio GUI...");
        // Avvia il flusso con la schermata di login
        GraphicLoginController loginGraphicController = createLoginController();
        loginGraphicController.onShow(null);
        // La GUI leggerà input dalla finestra e chiamerà loginController.effettuaLogin(...)
    }

    @Override
    public GraphicLoginController createLoginController() {
        if (loginController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            loginController = new GUIGraphicLoginController(navigator);
        }
        return loginController;
    }

    @Override
    public GraphicControllerAccount createAccountController() {
        if (accountController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            accountController = new GUIGraphicControllerAccount(navigator);
        }
        return accountController;
    }

    @Override
    public GraphicControllerRegistrazione createRegistrazioneController() {
        if (registrazioneController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            registrazioneController = new GUIGraphicControllerRegistrazione(navigator);
        }
        return registrazioneController;
    }

    @Override
    public GraphicControllerPrenotazione createPrenotazioneController() {
        if (prenotazioneController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            prenotazioneController = new GUIGraphicControllerPrenotazione(navigator);
        }
        return prenotazioneController;
    }

    @Override
    public GraphicControllerDisdetta createDisdettaController() {
        if (disdettaController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            disdettaController = new GUIGraphicControllerDisdetta(navigator);
        }
        return disdettaController;
    }

    @Override
    public GraphicControllerRegole createRegoleController() {
        if (regoleController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            regoleController = new GUIGraphicControllerRegole(navigator);
        }
        return regoleController;
    }

    @Override
    public GraphicControllerPenalita createPenalitaController() {
        if (penalitaController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            penalitaController = new GUIGraphicControllerPenalita(navigator);
        }
        return penalitaController;
    }

    @Override
    public GraphicControllerNavigation createNavigationController() {
        if (navigationController == null) {
            navigationController = new GUIGraphicControllerNavigation();
            registerRoutes();
        }
        return navigationController;
    }

    private GraphicControllerNavigation getNavigationController() {
        return createNavigationController();  // Delega al metodo pubblico
    }

    private void registerRoutes() {
        // Registrazione nelle factory concrete: conoscono i controller GUI reali e il navigator concreto.
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

