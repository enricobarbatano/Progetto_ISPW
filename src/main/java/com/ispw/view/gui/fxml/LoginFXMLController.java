package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la schermata di login.
 *
 * RESPONSABILITÀ:
 * - leggere email e password dai campi grafici;
 * - mostrare eventuali messaggi di errore;
 * - delegare il login al graphic controller;
 * - delegare il passaggio alla registrazione.
 *
 * NON:
 * - crea bean;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce direttamente la navigazione.
 *
 * Nota:
 * il graphic controller riceve valori semplici e costruisce il bean
 * necessario al caso d'uso di login.
 */
public class LoginFXMLController {

    private GUIGraphicLoginController controller;

    @FXML private Label lblError;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    /**
     * Inizializza il controller FXML con il graphic controller.
     *
     * @param controller controller grafico del login
     */
    public void init(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    /**
     * Renderizza eventuali messaggi ricevuti dal navigator.
     *
     * @param params parametri della route corrente
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearError();
            return;
        }

        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        setError(err);
    }

    /**
     * Gestisce il click sul pulsante "Accedi".
     */
    @FXML
    public void onLogin() {
        clearError();

        if (controller == null) {
            setError("Controller login non disponibile");
            return;
        }

        String email = safeText(txtEmail);
        String password = safePassword(txtPassword);

        if (email.isBlank() || password.isBlank()) {
            setError("Email e password sono obbligatori");
            return;
        }

        controller.effettuaLogin(email, password);
    }

    /**
     * Gestisce il click sul pulsante di registrazione.
     */
    @FXML
    public void onRegistrazione() {
        clearError();

        if (controller != null) {
            controller.vaiARegistrazione();
        }
    }

    /**
     * Restituisce testo pulito da un TextField.
     */
    private String safeText(TextField field) {
        return field != null && field.getText() != null
                ? field.getText().trim()
                : "";
    }

    /**
     * Restituisce testo da un PasswordField.
     */
    private String safePassword(PasswordField field) {
        return field != null && field.getText() != null
                ? field.getText()
                : "";
    }

    /**
     * Mostra un messaggio di errore.
     */
    private void setError(Object value) {
        if (lblError != null) {
            lblError.setText(value != null ? value.toString() : "");
        }
    }

    /**
     * Pulisce il messaggio di errore.
     */
    private void clearError() {
        setError("");
    }
}
