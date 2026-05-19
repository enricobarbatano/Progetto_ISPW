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
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per il caso d'uso Login.
 *
 * RESPONSABILITÀ:
 * - caricare il file login.fxml;
 * - recuperare il controller FXML;
 * - inizializzare il controller FXML con il graphic controller;
 * - passare al controller FXML i parametri ricevuti dal navigator;
 * - sostituire il root della Scene tramite GuiLauncher.
 *
 * NON:
 * - crea bean;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - contiene logica applicativa;
 * - gestisce direttamente il routing.
 *
 * Nota:
 * questa view è una schermata navigabile. Il navigator chiama onShow(...),
 * la view carica l'FXML e il LoginFXMLController gestisce gli eventi grafici.
 */
public class GUILoginView extends GenericViewGUI implements ViewLogin, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUILoginView.class.getName());

    private final GUIGraphicLoginController controller;

    /**
     * Costruisce la view GUI del login.
     *
     * @param controller controller grafico per il caso d'uso login
     */
    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    /**
     * Mostra la schermata di login.
     *
     * La sessione viene azzerata perché la schermata di login rappresenta
     * l'ingresso non autenticato nell'applicazione.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        sessione = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginFXMLController fx = loader.getController();
            fx.init(controller);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Login", e);
            showFallback();
        }
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento schermata Login"));
        GuiLauncher.setRoot(fallback);
    }
}