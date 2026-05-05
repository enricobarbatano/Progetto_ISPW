package com.ispw.view.gui.fxml;

import java.util.HashMap;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller per la gestione del profilo utente (Account).
 * Rifattorizzato per eliminare warning di inutilizzo e migliorare la robustezza.
 */
public class AccountFXMLController {

    private GUIGraphicControllerAccount controller;
    private SessioneUtenteBean sessione;
    private Integer idUtente; 

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private TextField txtNome;
    @FXML private TextField txtCognome;
    @FXML private TextField txtEmail;

    @FXML private PasswordField oldPwd;
    @FXML private PasswordField newPwd;

    /**
     * Inizializza il controller con le dipendenze necessarie.
     */
    public void init(GUIGraphicControllerAccount controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Esegue il rendering dei dati ricevuti dal controller grafico.
     */
    public void render(Map<String, Object> params) {
        if (params == null) return;

        // Gestione Messaggi di Errore e Successo
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object msg = params.get(GraphicControllerUtils.KEY_MESSAGE);
        if (msg == null) msg = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(msg != null ? String.valueOf(msg) : "");

        // Caricamento Dati Account nei Campi di Testo
        idUtente = null; // Reset cautelativo
        Object rawDati = params.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);

        if (rawDati instanceof Map<?, ?> dati) {
            // Estrazione sicura dell'ID (Unboxing Safety)
            Object idRaw = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
            if (idRaw instanceof Integer i) {
                this.idUtente = i;
            }

            // Popolamento campi con controllo null/stringa
            setTextIfPresent(txtNome, dati.get(GraphicControllerUtils.KEY_NOME));
            setTextIfPresent(txtCognome, dati.get(GraphicControllerUtils.KEY_COGNOME));
            setTextIfPresent(txtEmail, dati.get(GraphicControllerUtils.KEY_EMAIL));
        }
    }

    /**
     * Helper per impostare il testo solo se il valore non è nullo.
     */
    private void setTextIfPresent(TextField field, Object value) {
        if (field != null && value != null) {
            field.setText(String.valueOf(value));
        }
    }

    // --- GESTIONE EVENTI (Annotazioni @FXML aggiunte per risolvere i warning) ---

    @FXML 
    public void onLoad() {
        clearLocalError();
        if (controller != null) {
            controller.loadAccount(sessione);
        }
    }

    @FXML 
    public void onUpdate() {
        clearLocalError();
        if (controller == null) return;

        if (idUtente == null) {
            lblError.setText("Dati account non caricati: premi 'Ricarica dati'");
            return;
        }

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put(GraphicControllerUtils.KEY_ID_UTENTE, idUtente);
        updatePayload.put(GraphicControllerUtils.KEY_SESSIONE, sessione);

        addTrimmedTextToMap(updatePayload, GraphicControllerUtils.KEY_NOME, txtNome);
        addTrimmedTextToMap(updatePayload, GraphicControllerUtils.KEY_COGNOME, txtCognome);
        addTrimmedTextToMap(updatePayload, GraphicControllerUtils.KEY_EMAIL, txtEmail);

        controller.aggiornaDatiAccount(updatePayload);
    }

    @FXML 
    public void onChangePwd() {
        clearLocalError();
        if (controller == null) return;

        String rawNewPwd = newPwd.getText();
        if (rawNewPwd == null || rawNewPwd.trim().length() < 6) {
            lblError.setText("Nuova password non valida (min 6 caratteri)");
            return;
        }
        controller.cambiaPassword(oldPwd.getText(), rawNewPwd, sessione);
    }

    @FXML 
    public void onHome() {
        clearLocalError();
        if (controller != null) {
            controller.tornaAllaHome(sessione);
        }
    }

    @FXML 
    public void onLogout() {
        clearLocalError();
        if (controller != null) {
            controller.logout();
        }
    }

    /**
     * Helper per aggiungere testo alla mappa solo se non vuoto.
     */
    private void addTrimmedTextToMap(Map<String, Object> map, String key, TextField field) {
        if (field != null && field.getText() != null && !field.getText().isBlank()) {
            map.put(key, field.getText().trim());
        }
    }

    private void clearLocalError() {
        lblError.setText("");
        lblSuccess.setText("");
    }
}