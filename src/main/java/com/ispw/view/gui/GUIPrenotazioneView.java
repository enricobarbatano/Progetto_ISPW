package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.PrenotazioneFXMLController;
import com.ispw.view.interfaces.ViewGestionePrenotazione;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la prenotazione.
 *
 * RESPONSABILITÀ:
 * - caricare il file prenotazione.fxml;
 * - inizializzare il controller FXML;
 * - renderizzare i dati ricevuti dal navigator;
 * - sostituire il root della Scene.
 *
 * NON:
 * - creare bean;
 * - contenere logica di business;
 * - richiedere automaticamente dati in onShow();
 * - accedere al logic controller.
 *
 * Nota:
 * il caricamento dei campi avviene tramite il pulsante "Aggiorna Campi",
 * che richiama PrenotazioneFXMLController.onListaCampi().
 */
public class GUIPrenotazioneView extends GenericViewGUI
        implements ViewGestionePrenotazione, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIPrenotazioneView.class.getName());

    private final GUIGraphicControllerPrenotazione controller;

    private Parent cachedRoot;
    private PrenotazioneFXMLController cachedFx;

    /**
     * Costruisce la view di prenotazione.
     *
     * @param controller controller grafico per la prenotazione
     */
    public GUIPrenotazioneView(GUIGraphicControllerPrenotazione controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
    }

    /**
     * Mostra la schermata di prenotazione.
     *
     * Il metodo si limita al caricamento FXML, inizializzazione,
     * renderizzazione e sostituzione del root.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prenotazione.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Prenotazione", e);
            showFallback();
        }
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento prenotazione"));
        GuiLauncher.setRoot(fallback);
    }
}
