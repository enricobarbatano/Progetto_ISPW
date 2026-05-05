package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.LoginFXMLController;
import com.ispw.view.interfaces.ViewLogin;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class GUILoginView extends GenericViewGUI implements ViewLogin, NavigableController {

    private final GUIGraphicLoginController controller;
    
    // Dichiarazione del logger per la classe
    private static final Logger LOGGER = Logger.getLogger(GUILoginView.class.getName());

    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        this.sessione = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginFXMLController fx = loader.getController();
            
            // Metodo init uniformato correttamente
            fx.init(controller);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

        } catch (IOException e) {
            // Utilizzo del logger dichiarato sopra
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del file FXML per il Login", e);
            
            // Fallback per non interrompere l'esecuzione dell'app
            GuiLauncher.setRoot(GuiViewUtils.createRoot());
        }
    }
}