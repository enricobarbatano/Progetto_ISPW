package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.PenalitaFXMLController;
import com.ispw.view.interfaces.ViewGestionePenalita;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la gestione delle penalità.
 *
 * RESPONSABILITÀ:
 * - caricare il file penalita.fxml;
 * - inizializzare il controller FXML;
 * - renderizzare i dati ricevuti dal navigator;
 * - visualizzare la schermata.
 *
 * NON:
 * - richiedere automaticamente la lista utenti in onShow();
 * - calcolare penalità;
 * - creare bean;
 * - contenere logica applicativa.
 *
 * Nota:
 * la lista utenti viene richiesta tramite il pulsante "Carica Lista",
 * che richiama PenalitaFXMLController.onListaUtenti().
 */
public class GUIPenalitaView extends GenericViewGUI
        implements ViewGestionePenalita, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIPenalitaView.class.getName());

    private final GUIGraphicControllerPenalita controller;

    private Parent cachedRoot;
    private PenalitaFXMLController cachedFx;

    /**
     * Costruisce la view di gestione penalità.
     *
     * @param controller controller grafico per la gestione penalità
     */
    public GUIPenalitaView(GUIGraphicControllerPenalita controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    /**
     * Mostra la schermata penalità.
     *
     * Il metodo non esegue richieste automatiche al graphic controller,
     * perché tali richieste possono causare cicli di navigazione.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/penalita.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Penalità", e);
            showFallback();
        }
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento Penalità"));
        GuiLauncher.setRoot(fallback);
    }
}