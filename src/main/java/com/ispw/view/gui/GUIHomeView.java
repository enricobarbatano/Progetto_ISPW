package com.ispw.view.gui;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.HomeFXMLController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la home.
 *
 * RESPONSABILITÀ:
 * - caricare il file FXML home.fxml
 * - inizializzare il controller FXML con la sessione
 * - visualizzare il menu principale
 *
 * NON:
 * - contenere logica di navigazione
 * - creare bean
 *
 * ARCHITETTURA:
 * La home è un hub di navigazione verso tutti i casi d'uso.
 * Il controller FXML ha accesso al navigator per permettere
 * all'utente di navigare verso le diverse funzionalità.
 */
public class GUIHomeView extends GenericViewGUI implements NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIHomeView.class.getName());

    private final GraphicControllerNavigation navigator;

    /**
     * Costruisce la view home.
     *
     * @param navigator il navigator per la navigazione tra schermate
     */
    public GUIHomeView(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    /**
     * Restituisce il nome della route.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_HOME;
    }

    /**
     * Visualizza la schermata home.
     *
     * Carica l'FXML, inizializza il controller con la sessione,
     * e mostra il menu principale.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            HomeFXMLController fx = loader.getController();
            fx.init(navigator, sessione);

            GuiLauncher.setRoot(root);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Home", e);

            // Fallback UI
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento Home"));
            GuiLauncher.setRoot(fallback);
        }
    }
}