package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.RegoleFXMLController;
import com.ispw.view.interfaces.ViewGestioneRegole;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la gestione delle regole.
 *
 * RESPONSABILITÀ:
 * - caricare il file regole.fxml;
 * - inizializzare il controller FXML;
 * - renderizzare la lista campi e i messaggi ricevuti dal navigator;
 * - sostituire il root della Scene.
 *
 * NON:
 * - richiedere automaticamente la lista campi in onShow();
 * - creare bean;
 * - contenere logica di business;
 * - accedere direttamente alla persistenza.
 *
 * Nota:
 * la lista campi viene caricata tramite il pulsante "Carica",
 * che richiama RegoleFXMLController.onListaCampi().
 */
public class GUIRegoleView extends GenericViewGUI
        implements ViewGestioneRegole, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIRegoleView.class.getName());

    private final GUIGraphicControllerRegole controller;

    private Parent cachedRoot;
    private RegoleFXMLController cachedFx;

    /**
     * Costruisce la view di gestione regole.
     *
     * @param controller controller grafico per la gestione regole
     */
    public GUIRegoleView(GUIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    /**
     * Mostra la schermata di gestione regole.
     *
     * Il metodo si limita al ciclo di vita grafico:
     * caricamento FXML, init, render e setRoot.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/regole.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Regole", e);
            showFallback();
        }
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento schermata Regole"));
        GuiLauncher.setRoot(fallback);
    }
}