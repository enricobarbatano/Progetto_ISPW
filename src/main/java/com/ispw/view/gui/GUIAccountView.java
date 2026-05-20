package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.AccountFXMLController;
import com.ispw.view.interfaces.ViewGestioneAccount;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la gestione account.
 *
 * Responsabilità:
 * - caricare il file FXML account.fxml;
 * - inizializzare il controller FXML con le dipendenze;
 * - renderizzare i dati ricevuti dal navigator;
 * - visualizzare la scena.
 *
 * NON:
 * - contiene logica di business;
 * - crea bean;
 * - gestisce direttamente la navigazione;
 * - innesca loop di navigazione.
 *
 * Caching:
 * l'FXML viene caricato solo la prima volta per evitare reload continui.
 */
public class GUIAccountView extends GenericViewGUI
        implements ViewGestioneAccount, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIAccountView.class.getName());

    private final GUIGraphicControllerAccount controller;

    private Parent cachedRoot;
    private AccountFXMLController cachedFx;

    /**
     * Costruisce la view dell'account.
     *
     * @param controller controller grafico per la gestione account
     */
    public GUIAccountView(GUIGraphicControllerAccount controller) {
        this.controller = controller;
    }

    /**
     * Restituisce il nome della route associata alla view account.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    /**
     * Visualizza la schermata di gestione account.
     *
     * Se l'FXML non è ancora stato caricato, viene caricato e conservato in cache.
     * Poi il controller FXML viene inizializzato e vengono renderizzati i dati.
     *
     * In caso di errore di caricamento o inizializzazione,
     * viene mostrata una schermata di fallback.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            if (cachedRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account.fxml"));

                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            cachedFx.init(controller, sessione);
            cachedFx.render(getLastParams());

            GuiLauncher.setRoot(cachedRoot);

        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Account", e);

            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Account"));

            GuiLauncher.setRoot(fallback);
        }
    }
}

