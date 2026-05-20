package com.ispw.view.gui;
import java.io.IOException;
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
 * Responsabilità:
 * - caricare il file FXML home.fxml;
 * - inizializzare il controller FXML con navigator e sessione;
 * - visualizzare il menu principale.
 *
 * NON:
 * - contiene logica di business;
 * - crea bean;
 * - implementa logica applicativa di navigazione.
 *
 * Architettura:
 * la home è un hub grafico di navigazione verso i casi d'uso.
 */
public class GUIHomeView extends GenericViewGUI implements NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIHomeView.class.getName());

    private final GraphicControllerNavigation navigator;

    /**
     * Costruisce la view home.
     *
     * @param navigator controller di navigazione grafica
     */
    public GUIHomeView(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    /**
     * Restituisce il nome della route associata alla home.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_HOME;
    }

    /**
     * Visualizza la schermata home.
     *
     * Carica l'FXML, inizializza il controller FXML
     * e mostra la root grafica.
     *
     * In caso di errore di caricamento o inizializzazione,
     * viene mostrata una schermata di fallback.
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

        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Home", e);

            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento Home"));

            GuiLauncher.setRoot(fallback);
        }
    }
}

