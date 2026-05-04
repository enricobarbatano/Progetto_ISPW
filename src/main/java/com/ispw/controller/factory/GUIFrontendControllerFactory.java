package com.ispw.controller.factory;

import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.gui.GUIGraphicControllerLog;
import com.ispw.controller.graphic.gui.GUIGraphicControllerNavigation;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.gui.GUIGraphicLoginController;

import com.ispw.controller.graphic.interfaces.GraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.GraphicLoginController;

import com.ispw.view.gui.GUIAccountView;
import com.ispw.view.gui.GUIDisdettaView;
import com.ispw.view.gui.GUIHomeView;
import com.ispw.view.gui.GUILogView;
import com.ispw.view.gui.GUILoginView;
import com.ispw.view.gui.GUIPenalitaView;
import com.ispw.view.gui.GUIPrenotazioneView;
import com.ispw.view.gui.GUIRegistrazioneView;
import com.ispw.view.gui.GUIRegoleView;
import com.ispw.view.gui.GUIRichiesteDisdettaView;
import com.ispw.view.gui.GuiLauncher;

public final class GUIFrontendControllerFactory extends FrontendControllerFactory {

    private static final Logger logger = Logger.getLogger(GUIFrontendControllerFactory.class.getName());

    // Navigation (router)
    private GUIGraphicControllerNavigation navigationController;

    // Login
    private GUIGraphicLoginController loginController;
    private GUILoginView loginView;

    // Home
    private GUIHomeView homeView;

    // Views
    private GUIRegistrazioneView registrazioneView;
    private GUIAccountView accountView;
    private GUIPrenotazioneView prenotazioneView;
    private GUIDisdettaView disdettaView;
    private GUIRegoleView regoleView;
    private GUIPenalitaView penalitaView;
    private GUILogView logView;

    // ✅ Nuova view (gestore: richieste disdetta)
    private GUIRichiesteDisdettaView richiesteDisdettaView;

    // Controller (interfacce, DIP)
    private GraphicControllerAccount accountController;
    private GraphicControllerRegistrazione registrazioneController;
    private GraphicControllerPrenotazione prenotazioneController;
    private GraphicControllerDisdetta disdettaController;
    private GraphicControllerRegole regoleController;
    private GraphicControllerPenalita penalitaController;
    private GraphicControllerLog logController;

    // ✅ Nuovo controller
    private GraphicControllerRichiesteDisdetta richiesteDisdettaController;

    @Override
    public void startApplication() {
        logger.info("Avvio GUI...");
        GuiLauncher.launchApp(() -> getNavigationController().goTo(GraphicControllerUtils.ROUTE_LOGIN));
    }

    // ===================== Controllers =====================

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
    public GraphicControllerLog createLogController() {
        if (logController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            logController = new GUIGraphicControllerLog(navigator);
        }
        return logController;
    }

    // ✅ Nuovo: richieste disdetta (gestore)
    @Override
    public GraphicControllerRichiesteDisdetta createRichiesteDisdettaController() {
        if (richiesteDisdettaController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            richiesteDisdettaController = new GUIGraphicControllerRichiesteDisdetta(navigator);
        }
        return richiesteDisdettaController;
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
        return createNavigationController();
    }

    // ===================== Views =====================

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

    public GUILogView createLogView() {
        if (logView == null) {
            logView = new GUILogView(
                (GUIGraphicControllerLog) createLogController());
        }
        return logView;
    }

    // ✅ Nuova view (gestore)
    public GUIRichiesteDisdettaView createRichiesteDisdettaView() {
        if (richiesteDisdettaView == null) {
            richiesteDisdettaView = new GUIRichiesteDisdettaView(
                (GUIGraphicControllerRichiesteDisdetta) createRichiesteDisdettaController()
            );
        }
        return richiesteDisdettaView;
    }

    // ===================== Routes registration =====================

    private void registerRoutes() {
        navigationController.registerRoute(createLoginView().getRouteName(), createLoginView());
        navigationController.registerRoute(createHomeView().getRouteName(), createHomeView());
        navigationController.registerRoute(createAccountView().getRouteName(), createAccountView());
        navigationController.registerRoute(createRegistrazioneView().getRouteName(), createRegistrazioneView());
        navigationController.registerRoute(createPrenotazioneView().getRouteName(), createPrenotazioneView());
        navigationController.registerRoute(createDisdettaView().getRouteName(), createDisdettaView());
        navigationController.registerRoute(createRegoleView().getRouteName(), createRegoleView());
        navigationController.registerRoute(createPenalitaView().getRouteName(), createPenalitaView());
        navigationController.registerRoute(createLogView().getRouteName(), createLogView());

        // ✅ Nuova route gestore
        navigationController.registerRoute(createRichiesteDisdettaView().getRouteName(), createRichiesteDisdettaView());
    }
}