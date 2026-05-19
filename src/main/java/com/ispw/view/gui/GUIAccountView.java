package com.ispw.view.gui;

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
 * RESPONSABILITÀ:
 * - caricare il file FXML account.fxml
 * - inizializzare il controller FXML con le dipendenze
 * - renderizzare i dati ricevuti dal navigator
 * - visualizzare la scena
 *
 * NON:
 * - contenere logica di business
 * - creare bean
 * - gestire la navigazione
 * - innescare loop di navigazione
 *
 * CACHING:
 * L'FXML viene caricato solo la prima volta per evitare
 * inefficienze di reload continui.
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
     * @param controller il controller grafico per la gestione account
     */
    public GUIAccountView(GUIGraphicControllerAccount controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    /**
     * Visualizza la schermata di gestione account.
     *
     * Carica l'FXML una sola volta (caching), inizializza il controller FXML,
     * e renderizza i dati.
     *
     * In caso di errore IO, mostra una schermata di fallback.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            // Carica FXML solo la prima volta
            if (cachedRoot == null) {
                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("/fxml/account.fxml"));

                cachedRoot = loader.load();
                cachedFx = loader.getController();
            }

            // Inizializza il controller FXML
            cachedFx.init(controller, sessione);

            // Renderizza i dati ricevuti dal navigator
            cachedFx.render(getLastParams());

            // Mostra la scena
            GuiLauncher.setRoot(cachedRoot);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Account", e);

            // Fallback UI
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(
                    new Label("Errore caricamento schermata Account")
            );

            GuiLauncher.setRoot(fallback);
        }
    }
}