package com.ispw.view.gui;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.RegistrazioneFXMLController;
import com.ispw.view.interfaces.ViewRegistrazione;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * View GUI per la registrazione di un nuovo utente.
 *
 * Responsabilità:
 * - caricare il file FXML registrazione.fxml;
 * - inizializzare il controller FXML;
 * - renderizzare messaggi di errore o successo;
 * - visualizzare il form di registrazione.
 *
 * NON:
 * - crea bean di registrazione;
 * - valida direttamente i dati;
 * - gestisce direttamente la navigazione.
 *
 * Nota:
 * durante la registrazione la sessione è null.
 * La sessione viene creata solo dopo il login.
 */
public class GUIRegistrazioneView extends GenericViewGUI
        implements ViewRegistrazione, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIRegistrazioneView.class.getName());

    private final GUIGraphicControllerRegistrazione controller;

    /**
     * Costruisce la view di registrazione.
     *
     * @param controller controller grafico per la registrazione
     */
    public GUIRegistrazioneView(GUIGraphicControllerRegistrazione controller) {
        this.controller = controller;
    }

    /**
     * Restituisce il nome della route associata alla registrazione.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
    }

    /**
     * Visualizza la schermata di registrazione.
     *
     * La sessione viene forzata a null perché in questa fase
     * l'utente non è ancora autenticato.
     *
     * In caso di errore di caricamento o inizializzazione,
     * viene mostrata una schermata di fallback.
     */
    
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        sessione = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registrazione.fxml"));
            Parent root = loader.load();

            RegistrazioneFXMLController fx = loader.getController();

            fx.init(controller);

            // IMPORTANTE: passa params
            fx.render(params);

            //NUOVO: gestisci errore
            if (params != null && params.containsKey("error")) {
                fx.showError((String) params.get("error"));
            }

            GuiLauncher.setRoot(root);

        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Registrazione", e);

            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Registrazione"));

            GuiLauncher.setRoot(fallback);
        }
    }

}

