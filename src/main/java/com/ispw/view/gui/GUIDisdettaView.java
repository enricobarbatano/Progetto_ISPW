package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.DisdettaFXMLController;
import com.ispw.view.interfaces.ViewDisdettaPrenotazione;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per il caso d'uso di disdetta prenotazione.
 *
 * RESPONSABILITÀ:
 * - caricare il file disdetta.fxml;
 * - inizializzare il controller FXML;
 * - passare al controller FXML i dati ricevuti dal navigator;
 * - sostituire il root della Scene tramite GuiLauncher.
 *
 * NON:
 * - richiedere automaticamente le prenotazioni in onShow();
 * - creare bean;
 * - chiamare direttamente il logic controller;
 * - contenere logica applicativa.
 *
 * Nota:
 * il caricamento delle prenotazioni cancellabili avviene tramite il pulsante
 * "Aggiorna Lista", che richiama DisdettaFXMLController.onRicarica().
 * In questo modo si evita un loop del tipo:
 * onShow() -> controller -> goTo() -> onShow().
 */
public class GUIDisdettaView extends GenericViewGUI
        implements ViewDisdettaPrenotazione, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIDisdettaView.class.getName());

    private final GUIGraphicControllerDisdetta controller;

    private Parent cachedRoot;
    private DisdettaFXMLController cachedFx;

    /**
     * Costruisce la view GUI per la disdetta.
     *
     * @param controller controller grafico della disdetta
     */
    public GUIDisdettaView(GUIGraphicControllerDisdetta controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    /**
     * Mostra la schermata di disdetta.
     *
     * Il metodo si limita a caricare l'FXML, inizializzare il controller FXML
     * e renderizzare i dati correnti. Non avvia richieste automatiche al
     * graphic controller.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/disdetta.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Disdetta", e);
            showFallback();
        }
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento schermata Disdetta"));
        GuiLauncher.setRoot(fallback);
    }
}
