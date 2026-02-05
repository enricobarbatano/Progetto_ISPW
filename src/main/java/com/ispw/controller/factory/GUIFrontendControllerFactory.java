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
import com.ispw.view.gui.GUIAccountView;
import com.ispw.view.gui.GUIDisdettaView;
import com.ispw.view.gui.GUIHomeView;
import com.ispw.view.gui.GUILoginView;
import com.ispw.view.gui.GUIPenalitaView;
import com.ispw.view.gui.GUIPrenotazioneView;
import com.ispw.view.gui.GUIRegistrazioneView;
import com.ispw.view.gui.GUIRegoleView;
import com.ispw.view.gui.GuiLauncher;

public final class GUIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(GUIFrontendControllerFactory.class.getName());
    private GUIGraphicControllerNavigation navigationController;
    private GUIGraphicLoginController loginController;
    private GUILoginView loginView;
    private GUIHomeView homeView;
    private GUIRegistrazioneView registrazioneView;
    private GUIAccountView accountView;
    private GUIPrenotazioneView prenotazioneView;
    private GUIDisdettaView disdettaView;
    private GUIRegoleView regoleView;
    private GUIPenalitaView penalitaView;
    private GraphicControllerAccount accountController;
    private GraphicControllerRegistrazione registrazioneController;
    private GraphicControllerPrenotazione prenotazioneController;
    private GraphicControllerDisdetta disdettaController;
    private GraphicControllerRegole regoleController;
    private GraphicControllerPenalita penalitaController;

    @Override
    public void startApplication() {
        logger.info("Avvio GUI...");
        GuiLauncher.launchApp(() -> getNavigationController().goTo(com.ispw.controller.graphic.GraphicControllerUtils.ROUTE_LOGIN));
    }

    @Override
    public GraphicLoginController createLoginController() {
        if (loginController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            loginController = new GUIGraphicLoginController(navigator);
        }
        return loginController;
    }

    public GUILoginView createLoginView() {
        if (loginView == null) {
            loginView = new GUILoginView(loginController == null
                ? (GUIGraphicLoginController) createLoginController()
                : loginController);
        }
        return loginView;
    }

    public GUIHomeView createHomeView() {
        if (homeView == null) {
            homeView = new GUIHomeView(getNavigationController());
        }
        return homeView;
    }

    public GUIRegistrazioneView createRegistrazioneView() {
        if (registrazioneView == null) {
            registrazioneView = new GUIRegistrazioneView(
                (GUIGraphicControllerRegistrazione) createRegistrazioneController());
        }
        return registrazioneView;
    }

    public GUIAccountView createAccountView() {
        if (accountView == null) {
            accountView = new GUIAccountView(
                (GUIGraphicControllerAccount) createAccountController());
        }
        return accountView;
    }

    public GUIPrenotazioneView createPrenotazioneView() {
        if (prenotazioneView == null) {
            prenotazioneView = new GUIPrenotazioneView(
                (GUIGraphicControllerPrenotazione) createPrenotazioneController());
        }
        return prenotazioneView;
    }

    public GUIDisdettaView createDisdettaView() {
        if (disdettaView == null) {
            disdettaView = new GUIDisdettaView(
                (GUIGraphicControllerDisdetta) createDisdettaController());
        }
        return disdettaView;
    }

    public GUIRegoleView createRegoleView() {
        if (regoleView == null) {
            regoleView = new GUIRegoleView(
                (GUIGraphicControllerRegole) createRegoleController());
        }
        return regoleView;
    }

    public GUIPenalitaView createPenalitaView() {
        if (penalitaView == null) {
            penalitaView = new GUIPenalitaView(
                (GUIGraphicControllerPenalita) createPenalitaController());
        }
        return penalitaView;
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
        navigationController.registerRoute(createLoginView().getRouteName(), createLoginView());
        navigationController.registerRoute(createHomeView().getRouteName(), createHomeView());
        navigationController.registerRoute(createAccountView().getRouteName(), createAccountView());
        navigationController.registerRoute(createRegistrazioneView().getRouteName(), createRegistrazioneView());
        navigationController.registerRoute(createPrenotazioneView().getRouteName(), createPrenotazioneView());
        navigationController.registerRoute(createDisdettaView().getRouteName(), createDisdettaView());
        navigationController.registerRoute(createRegoleView().getRouteName(), createRegoleView());
        navigationController.registerRoute(createPenalitaView().getRouteName(), createPenalitaView());
    }
} 

