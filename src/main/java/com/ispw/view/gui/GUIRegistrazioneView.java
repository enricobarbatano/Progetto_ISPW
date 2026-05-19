package com.ispw.view.gui;

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
 * RESPONSABILITÀ:
 * - caricare il file FXML registrazione.fxml
 * - inizializzare il controller FXML
 * - renderizzare i dati (messaggi di errore/successo)
 * - visualizzare il form di registrazione
 *
 * NON:
 * - creare bean di registrazione (responsabilità del controller grafico)
 * - validare i dati (responsabilità del controller grafico)
 * - gestire la navigazione
 *
 * NOTA:
 * Durante la registrazione, la sessione è null.
 * Solo dopo il login, la sessione viene creata.
 */
public class GUIRegistrazioneView extends GenericViewGUI implements ViewRegistrazione, NavigableController {

    private static final Logger LOGGER = Logger.getLogger(GUIRegistrazioneView.class.getName());

    private final GUIGraphicControllerRegistrazione controller;

    /**
     * Costruisce la view di registrazione.
     *
     * @param controller il controller grafico per la registrazione
     */
    public GUIRegistrazioneView(GUIGraphicControllerRegistrazione controller) {
        this.controller = controller;
    }

    /**
     * Restituisce il nome della route.
     */
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
    }

    /**
     * Visualizza la schermata di registrazione.
     *
     * Carica l'FXML, inizializza il controller, e renderizza i messaggi.
     * La sessione è null durante la registrazione.
     */
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        // La registrazione non richiede sessione
        sessione = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registrazione.fxml"));
            Parent root = loader.load();

            RegistrazioneFXMLController fx = loader.getController();
            fx.init(controller);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore caricamento schermata Registrazione", e);

            // Fallback UI
            VBox fallback = GuiViewUtils.createRoot();
            fallback.getChildren().add(new Label("Errore caricamento schermata Registrazione"));
            GuiLauncher.setRoot(fallback);
        }
    }
}