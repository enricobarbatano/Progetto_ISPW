// src/main/java/com/ispw/controller/factory/CLIFrontendControllerFactory.java
package com.ispw.controller.factory;


import com.ispw.controller.graphic.GraphicControllerAccount;
import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerLog;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.GraphicControllerRegistrazione;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.GraphicLoginController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerAccount;
import com.ispw.controller.graphic.cli.CLIGraphicControllerDisdetta;
import com.ispw.controller.graphic.cli.CLIGraphicControllerLog;
import com.ispw.controller.graphic.cli.CLIGraphicControllerNavigation;
import com.ispw.controller.graphic.cli.CLIGraphicControllerPenalita;
import com.ispw.controller.graphic.cli.CLIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegole;
import com.ispw.controller.graphic.cli.CLIGraphicLoginController;
import com.ispw.view.cli.CLIAccountView;
import com.ispw.view.cli.CLIDisdettaView;
import com.ispw.view.cli.CLIHomeView;
import com.ispw.view.cli.CLILogView;
import com.ispw.view.cli.CLILoginView;
import com.ispw.view.cli.CLIPenalitaView;
import com.ispw.view.cli.CLIPrenotazioneView;
import com.ispw.view.cli.CLIRegistrazioneView;
import com.ispw.view.cli.CLIRegoleView;

public final class CLIFrontendControllerFactory extends FrontendControllerFactory {
    private CLIGraphicControllerNavigation navigationController;
    private CLIGraphicLoginController loginController;
    private CLILoginView loginView;
    private CLIHomeView homeView;
    private CLIRegistrazioneView registrazioneView;
    private CLIAccountView accountView;
    private CLIPrenotazioneView prenotazioneView;
    private CLIDisdettaView disdettaView;
    private CLIRegoleView regoleView;
    private CLIPenalitaView penalitaView;
    private CLILogView logView;
    private GraphicControllerAccount accountController;
    private GraphicControllerRegistrazione registrazioneController;
    private GraphicControllerPrenotazione prenotazioneController;
    private GraphicControllerDisdetta disdettaController;
    private GraphicControllerRegole regoleController;
    private GraphicControllerPenalita penalitaController;
    private GraphicControllerLog logController;

    @Override
    public void startApplication() {
        System.out.println("Avvio CLI...");
        // Avvia il flusso con la schermata di login
        getNavigationController().goTo(GraphicControllerUtils.ROUTE_LOGIN);
    }

    @Override
    public GraphicLoginController createLoginController() {
        if (loginController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            loginController = new CLIGraphicLoginController(navigator);
        }
        return loginController;
    }

    public CLILoginView createLoginView() {
        if (loginView == null) {
            loginView = new CLILoginView(loginController == null
                ? (CLIGraphicLoginController) createLoginController()
                : loginController);
        }
        return loginView;
    }

    public CLIHomeView createHomeView() {
        if (homeView == null) {
            homeView = new CLIHomeView(getNavigationController());
        }
        return homeView;
    }

    public CLIRegistrazioneView createRegistrazioneView() {
        if (registrazioneView == null) {
            registrazioneView = new CLIRegistrazioneView(
                (CLIGraphicControllerRegistrazione) createRegistrazioneController());
        }
        return registrazioneView;
    }

    public CLIAccountView createAccountView() {
        if (accountView == null) {
            accountView = new CLIAccountView(
                (CLIGraphicControllerAccount) createAccountController());
        }
        return accountView;
    }

    public CLIPrenotazioneView createPrenotazioneView() {
        if (prenotazioneView == null) {
            prenotazioneView = new CLIPrenotazioneView(
                (CLIGraphicControllerPrenotazione) createPrenotazioneController());
        }
        return prenotazioneView;
    }

    public CLIDisdettaView createDisdettaView() {
        if (disdettaView == null) {
            disdettaView = new CLIDisdettaView(
                (CLIGraphicControllerDisdetta) createDisdettaController());
        }
        return disdettaView;
    }

    public CLIRegoleView createRegoleView() {
        if (regoleView == null) {
            regoleView = new CLIRegoleView(
                (CLIGraphicControllerRegole) createRegoleController());
        }
        return regoleView;
    }

    public CLIPenalitaView createPenalitaView() {
        if (penalitaView == null) {
            penalitaView = new CLIPenalitaView(
                (CLIGraphicControllerPenalita) createPenalitaController());
        }
        return penalitaView;
    }

    public CLILogView createLogView() {
        if (logView == null) {
            logView = new CLILogView(
                (CLIGraphicControllerLog) createLogController());
        }
        return logView;
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
    public GraphicControllerLog createLogController() {
        if (logController == null) {
            GraphicControllerNavigation navigator = getNavigationController();
            logController = new CLIGraphicControllerLog(navigator);
        }
        return logController;
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
        navigationController.registerRoute(createLoginView().getRouteName(), createLoginView());
        navigationController.registerRoute(createHomeView().getRouteName(), createHomeView());
        navigationController.registerRoute(createAccountView().getRouteName(), createAccountView());
        navigationController.registerRoute(createRegistrazioneView().getRouteName(), createRegistrazioneView());
        navigationController.registerRoute(createPrenotazioneView().getRouteName(), createPrenotazioneView());
        navigationController.registerRoute(createDisdettaView().getRouteName(), createDisdettaView());
        navigationController.registerRoute(createRegoleView().getRouteName(), createRegoleView());
        navigationController.registerRoute(createPenalitaView().getRouteName(), createPenalitaView());
        navigationController.registerRoute(createLogView().getRouteName(), createLogView());
    }
}  
