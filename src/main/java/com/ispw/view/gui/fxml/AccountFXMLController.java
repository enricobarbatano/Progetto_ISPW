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

public class AccountFXMLController {

    private GUIGraphicControllerAccount controller;
    private SessioneUtenteBean sessione;
    private Integer idUtente; // preso dal payload datiAccount

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private TextField txtNome;
    @FXML private TextField txtCognome;
    @FXML private TextField txtEmail;

    @FXML private PasswordField oldPwd;
    @FXML private PasswordField newPwd;

    public void init(GUIGraphicControllerAccount controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    public void render(Map<String, Object> params) {
        Object err = params != null ? params.get(GraphicControllerUtils.KEY_ERROR) : null;
        Object ok = null;
        if (params != null) {
            ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
            if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
        }
        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        idUtente = null;

        Object raw = params != null ? params.get(GraphicControllerUtils.KEY_DATI_ACCOUNT) : null;
        if (raw instanceof Map<?, ?> dati) {
            Object id = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
            Object n = dati.get(GraphicControllerUtils.KEY_NOME);
            Object c = dati.get(GraphicControllerUtils.KEY_COGNOME);
            Object e = dati.get(GraphicControllerUtils.KEY_EMAIL);

            if (id instanceof Integer i) idUtente = i;
            if (n != null) txtNome.setText(String.valueOf(n));
            if (c != null) txtCognome.setText(String.valueOf(c));
            if (e != null) txtEmail.setText(String.valueOf(e));
        }
    }

    @FXML private void onLoad() {
        clearLocalError();
        if (controller == null) return;
        controller.loadAccount(sessione);
    }

    @FXML private void onUpdate() {
        clearLocalError();
        if (controller == null) return;

        if (idUtente == null) {
            lblError.setText("Dati account non caricati: premi 'Ricarica dati'");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put(GraphicControllerUtils.KEY_ID_UTENTE, idUtente);
        update.put(GraphicControllerUtils.KEY_SESSIONE, sessione);

        if (txtNome.getText() != null && !txtNome.getText().isBlank())
            update.put(GraphicControllerUtils.KEY_NOME, txtNome.getText().trim());

        if (txtCognome.getText() != null && !txtCognome.getText().isBlank())
            update.put(GraphicControllerUtils.KEY_COGNOME, txtCognome.getText().trim());

        if (txtEmail.getText() != null && !txtEmail.getText().isBlank())
            update.put(GraphicControllerUtils.KEY_EMAIL, txtEmail.getText().trim());

        controller.aggiornaDatiAccount(update);
    }

    @FXML private void onChangePwd() {
        clearLocalError();
        if (controller == null) return;

        String np = newPwd.getText();
        if (np == null || np.trim().length() < 6) {
            lblError.setText("Nuova password non valida (min 6 caratteri)");
            return;
        }
        controller.cambiaPassword(oldPwd.getText(), np, sessione);
    }

    @FXML private void onHome() {
        clearLocalError();
        if (controller == null) return;
        controller.tornaAllaHome(sessione);
    }

    @FXML private void onLogout() {
        clearLocalError();
        if (controller == null) return;
        controller.logout();
    }

    private void clearLocalError() {
        lblError.setText("");
    }
}