package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la vista di Login.
 * Segue il pattern "Pure View": riceve messaggi tramite render() e delega le azioni al controller grafico.
 */
public class LoginFXMLController {

    private GUIGraphicLoginController controller;

    @FXML private Label lblError;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    /**
     * Inizializza il controller con il riferimento al controller grafico.
     * Uniformato a 'init' per coerenza con gli altri controller della view.
     */
    public void init(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    /**
     * Esegue il rendering dei messaggi di errore o stato provenienti dal controller grafico.
     */
    public void render(Map<String, Object> params) {
        if (params == null) return;

        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        if (lblError != null) {
            lblError.setText(err != null ? String.valueOf(err) : "");
        }
    }

    // --- GESTIONE EVENTI (Resi public per evitare warning di inutilizzo) ---

    @FXML
    public void onLogin() {
        clearLocalError();
        if (controller != null) {
            // Delega la validazione e l'esecuzione al controller grafico
            controller.effettuaLoginRaw(txtEmail.getText(), txtPassword.getText());
        }
    }

    @FXML
    public void onRegistrazione() {
        clearLocalError();
        if (controller != null) {
            controller.vaiARegistrazione();
        }
    }

    /**
     * Pulisce i messaggi di errore prima di una nuova operazione.
     */
    private void clearLocalError() {
        if (lblError != null) {
            lblError.setText("");
        }
    }
}