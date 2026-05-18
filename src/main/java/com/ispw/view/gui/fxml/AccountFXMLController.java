package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller FXML per la gestione account.
 *
 * RESPONSABILITÀ:
 * - mostra dati utente
 * - raccoglie input
 * - delega al controller grafico
 *
 * NON:
 * - crea bean
 * - contiene logica di business
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
     * Inizializzazione dipendenze
     */
    public void init(GUIGraphicControllerAccount controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Rendering dati provenienti dal controller grafico
     */
    public void render(Map<String, Object> params) {

        if (params == null) return;

        // Messaggi
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok  = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? err.toString() : "");
        lblSuccess.setText(ok != null ? ok.toString() : "");

        // Dati account
        Object raw = params.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);

        if (raw instanceof Map<?, ?> dati) {

            Object idRaw = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
            if (idRaw instanceof Integer i) {
                idUtente = i;
            }

            setText(txtNome, dati.get(GraphicControllerUtils.KEY_NOME));
            setText(txtCognome, dati.get(GraphicControllerUtils.KEY_COGNOME));
            setText(txtEmail, dati.get(GraphicControllerUtils.KEY_EMAIL));
        }
    }

    // =========================================================
    // EVENTI UI
    // =========================================================

    @FXML
    public void onLoad() {
        controller.loadAccount(sessione);
    }

    @FXML
    public void onUpdate() {

        if (idUtente == null) {
            lblError.setText("Ricaricare dati account");
            return;
        }

        controller.aggiornaDatiAccount(
                idUtente,
                txtNome.getText(),
                txtCognome.getText(),
                txtEmail.getText(),
                sessione
        );
    }

    @FXML
    public void onChangePwd() {

        controller.cambiaPassword(
                oldPwd.getText(),
                newPwd.getText(),
                sessione
        );
    }

    @FXML
    public void onHome() {
        controller.tornaAllaHome(sessione);
    }

    @FXML
    public void onLogout() {
        controller.logout();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void setText(TextField f, Object v) {
        if (f != null && v != null) {
            f.setText(v.toString());
        }
    }
}