package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.RichiesteDisdettaFXMLController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la gestione delle richieste di disdetta.
 *
 * RESPONSABILITÀ:
 * - caricare il file richieste_disdetta.fxml;
 * - inizializzare il controller FXML;
 * - renderizzare le richieste ricevute dal navigator;
 * - visualizzare la schermata.
 *
 * NON:
 * - caricare automaticamente le richieste pending in onShow();
 * - approvare o rifiutare direttamente richieste;
 * - creare bean;
 * - contenere logica applicativa.
 *
 * Nota:
 * il caricamento delle richieste avviene tramite il pulsante "Ricarica",
 * che richiama RichiesteDisdettaFXMLController.onRicarica().
 */
public class GUIRichiesteDisdettaView extends GenericViewGUI implements NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIRichiesteDisdettaView.class.getName());

    private final GUIGraphicControllerRichiesteDisdetta controller;

    private Parent cachedRoot;
    private RichiesteDisdettaFXMLController cachedFx;

    /**
     * Costruisce la view di gestione richieste disdetta.
     *
     * @param controller controller grafico per la gestione richieste disdetta
     */
    public GUIRichiesteDisdettaView(GUIGraphicControllerRichiesteDisdetta controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA;
    }

    /**
     * Mostra la schermata delle richieste di disdetta.
     *
     * Il metodo non avvia caricamenti automatici per evitare cicli di navigazione.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/richieste_disdetta.fxml")
                );
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Richieste Disdetta", e);
            showFallback();
        }
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(
                new Label("Errore caricamento schermata Richieste Disdetta")
        );
        GuiLauncher.setRoot(fallback);
    }
}