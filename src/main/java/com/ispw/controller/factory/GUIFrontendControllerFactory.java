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

    @Override
    public void startApplication() {
        logger.info("Avvio GUI...");
        // Avvia il flusso con la schermata di login
        GraphicLoginController loginController = createLoginController();
        loginController.onShow(null);
        // La GUI leggerà input dalla finestra e chiamerà loginController.effettuaLogin(...)
    }

    @Override
    public GraphicLoginController createLoginController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicLoginController((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerAccount createAccountController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicControllerAccount((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerRegistrazione createRegistrazioneController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicControllerRegistrazione((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerPrenotazione createPrenotazioneController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicControllerPrenotazione((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerDisdetta createDisdettaController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicControllerDisdetta((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerRegole createRegoleController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicControllerRegole((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerPenalita createPenalitaController() {
        GraphicControllerNavigation navigator = getNavigationController();
        return new GUIGraphicControllerPenalita((GUIGraphicControllerNavigation) navigator);
    }

    @Override
    public GraphicControllerNavigation createNavigationController() {
        if (navigationController == null) {
            navigationController = new GUIGraphicControllerNavigation();
        }
        return navigationController;
    }

    private GraphicControllerNavigation getNavigationController() {
        if (navigationController == null) {
            navigationController = new GUIGraphicControllerNavigation();
        }
        return navigationController;
    }
} 

