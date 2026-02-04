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

    @Override
    public void startApplication() {
        logger.info("Avvio CLI...");
        // Avvia il flusso con la schermata di login
        GraphicLoginController loginController = createLoginController();
        loginController.onShow(null);
        // La CLI leggerà input da console e chiamerà loginController.effettuaLogin(...)
    }

    @Override
    public GraphicLoginController createLoginController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicLoginController((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerAccount createAccountController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicControllerAccount((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerRegistrazione createRegistrazioneController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicControllerRegistrazione((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerPrenotazione createPrenotazioneController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicControllerPrenotazione((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerDisdetta createDisdettaController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicControllerDisdetta((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerRegole createRegoleController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicControllerRegole((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerPenalita createPenalitaController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new CLIGraphicControllerPenalita((CLIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerNavigation createNavigationController() {
        if (navigationController == null) {
            navigationController = new CLIGraphicControllerNavigation();
        }
        return navigationController;
    }

    private GraphicControllerNavigation getNavigationController() {
        if (navigationController == null) {
            navigationController = new CLIGraphicControllerNavigation();
        }
        return navigationController;
    }
}  
