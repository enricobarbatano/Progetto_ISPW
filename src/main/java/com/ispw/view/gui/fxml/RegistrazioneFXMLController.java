package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la registrazione di un nuovo utente.
 *
 * RESPONSABILITÀ:
 * - leggere nome, cognome, email e password dal form;
 * - mostrare eventuali messaggi di errore;
 * - delegare la registrazione al graphic controller;
 * - delegare il ritorno alla schermata di login.
 *
 * NON:
 * - crea bean;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce direttamente la navigazione.
 *
 * Nota:
 * il ruolo viene scelto qui solo come valore grezzo di input.
 * Il bean di registrazione viene creato dal graphic controller.
 */
public class RegistrazioneFXMLController {

    private GUIGraphicControllerRegistrazione controller;

    @FXML
    private Label lblError;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtCognome;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    /**
     * Inizializza il controller FXML con il graphic controller.
     *
     * @param controller controller grafico della registrazione
     */
    public void init(GUIGraphicControllerRegistrazione controller) {
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
     * Gestisce il click sul pulsante "Registrati Ora".
     */
    @FXML
    public void onRegistra() {
        clearError();

        if (controller == null) {
            setError("Controller registrazione non disponibile");
            return;
        }

        String nome = safeText(txtNome);
        String cognome = safeText(txtCognome);
        String email = safeText(txtEmail);
        String password = safePassword(txtPassword);

        if (nome.isBlank() || cognome.isBlank() || email.isBlank() || password.isBlank()) {
            setError("Tutti i campi sono obbligatori");
            return;
        }

        controller.inviaDatiRegistrazione(
                nome,
                cognome,
                email,
                password,
                Ruolo.UTENTE
        );
    }

    /**
     * Torna alla schermata di login.
     */
    @FXML
    public void onLogin() {
        clearError();

        if (controller != null) {
            controller.vaiAlLogin();
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
     * Mostra un messaggio di errore ricevuto dalla GUIView.
     */
    public void showError(String message) {
        setError(message);
    }

    /**
     * Mostra un messaggio di errore nella label dedicata.
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