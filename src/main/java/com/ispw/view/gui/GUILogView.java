package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.LogFXMLController;
import com.ispw.view.interfaces.ViewLog;
import com.ispw.view.shared.LogViewUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la visualizzazione dei log di sistema.
 *
 * RESPONSABILITÀ:
 * - controllare se l'utente corrente può accedere alla schermata;
 * - caricare il file log.fxml;
 * - inizializzare il controller FXML;
 * - renderizzare i log ricevuti dal navigator;
 * - sostituire il root della Scene.
 *
 * NON:
 * - richiedere automaticamente i log in onShow();
 * - generare o modificare log;
 * - contenere logica applicativa;
 * - accedere direttamente al logic controller.
 *
 * Nota:
 * il caricamento dei log avviene tramite il pulsante "Aggiorna Registro",
 * che richiama LogFXMLController.onRefresh().
 */
public class GUILogView extends GenericViewGUI implements ViewLog, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUILogView.class.getName());

    private final GUIGraphicControllerLog controller;

    private Parent cachedRoot;
    private LogFXMLController cachedFx;

    /**
     * Costruisce la view dei log.
     *
     * @param controller controller grafico per la consultazione dei log
     */
    public GUILogView(GUIGraphicControllerLog controller) {
        this.controller = controller;
    }

    /**
     * Restituisce la route associata alla schermata.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    /**
     * Mostra la schermata dei log.
     *
     * Se l'utente non è un gestore, viene mostrato un messaggio di accesso negato.
     * Se l'utente è autorizzato, viene caricato l'FXML e vengono renderizzati
     * gli eventuali log già presenti nei parametri.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        if (!LogViewUtils.isGestore(sessione)) {
            showAccessDenied();
            return;
        }

        try {
            if (cachedRoot == null || cachedFx == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/log.fxml"));
                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Log", e);
            showFallback();
        }
    }

    /**
     * Mostra il messaggio di accesso negato.
     */
    private void showAccessDenied() {
        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label("Accesso ai log riservato al gestore"));
        root.getChildren().add(GuiViewUtils.buildHomeButton(controller::tornaAllaHome));
        GuiLauncher.setRoot(root);
    }

    /**
     * Mostra una schermata semplice in caso di errore di caricamento.
     */
    private void showFallback() {
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento schermata Log"));
        fallback.getChildren().add(GuiViewUtils.buildHomeButton(controller::tornaAllaHome));
        GuiLauncher.setRoot(fallback);
    }
}