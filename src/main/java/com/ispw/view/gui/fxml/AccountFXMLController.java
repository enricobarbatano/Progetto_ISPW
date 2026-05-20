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
 * Controller FXML per la gestione account utente.
 *
 * RESPONSABILITÀ:
 * - visualizzare i dati dell'account corrente;
 * - raccogliere input per aggiornamento dati;
 * - raccogliere input per cambio password;
 * - delegare le operazioni al graphic controller.
 *
 * NON:
 * - crea bean;
 * - chiama il logic controller;
 * - gestisce direttamente il routing;
 * - contiene logica applicativa.
 *
 * Nota:
 * il metodo onLoad() è mantenuto perché è richiamato da account.fxml.
 * Non crea loop perché viene eseguito solo tramite click utente,
 * non automaticamente da GUIAccountView.onShow().
 */
public class AccountFXMLController {

    private static final String MSG_CONTROLLER_NON_DISPONIBILE =
            "Controller account non disponibile";

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
     * Inizializza il controller FXML con le dipendenze necessarie.
     */
    public void init(GUIGraphicControllerAccount controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Renderizza i dati ricevuti dal navigator.
     */
    public void render(Map<String, Object> params) {
        if (params == null) {
            clearMessages();
            return;
        }

        renderMessages(params);
        renderAccountData(params);
    }

    /**
     * Ricarica i dati account.
     *
     * Questo metodo è collegato al bottone "Ricarica Dati" nell'FXML.
     */
    @FXML
    public void onLoad() {
        clearMessages();

        if (controller == null) {
            showError(MSG_CONTROLLER_NON_DISPONIBILE);
            return;
        }

        controller.loadAccount(sessione);
    }

    /**
     * Aggiorna i dati dell'account usando valori grezzi letti dalla GUI.
     */
    @FXML
    public void onUpdate() {
        clearMessages();

        if (controller == null) {
            showError(MSG_CONTROLLER_NON_DISPONIBILE);
            return;
        }

        if (idUtente == null) {
            showError("Ricaricare i dati account");
            return;
        }

        String nome = safeText(txtNome);
        String cognome = safeText(txtCognome);
        String email = safeText(txtEmail);

        if (nome.isBlank() || cognome.isBlank() || email.isBlank()) {
            showError("Tutti i campi sono obbligatori");
            return;
        }

        controller.aggiornaDatiAccount(
                idUtente,
                nome,
                cognome,
                email,
                sessione
        );
    }

    /**
     * Cambia la password dell'utente.
     */
    @FXML
    public void onChangePwd() {
        clearMessages();

        if (controller == null) {
            showError(MSG_CONTROLLER_NON_DISPONIBILE);
            return;
        }

        String oldPassword = safePassword(oldPwd);
        String newPassword = safePassword(newPwd);

        if (oldPassword.isBlank() || newPassword.isBlank()) {
            showError("Le password sono obbligatorie");
            return;
        }

        if (newPassword.length() < 6) {
            showError("La nuova password deve avere almeno 6 caratteri");
            return;
        }

        if (oldPassword.equals(newPassword)) {
            showError("La nuova password non può essere uguale alla vecchia");
            return;
        }

        controller.cambiaPassword(oldPassword, newPassword, sessione);
    }

    /**
     * Torna alla home.
     */
    @FXML
    public void onHome() {
        if (controller != null) {
            controller.tornaAllaHome(sessione);
        }
    }

    /**
     * Effettua il logout.
     */
    @FXML
    public void onLogout() {
        if (controller != null) {
            controller.logout();
        }
    }

    private void renderMessages(Map<String, Object> params) {
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);

        if (ok == null) {
            ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
        }

        lblError.setText(err != null ? err.toString() : "");
        lblSuccess.setText(ok != null ? ok.toString() : "");
    }

    private void renderAccountData(Map<String, Object> params) {
        Object raw = params.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);

        if (!(raw instanceof Map<?, ?> dati)) {
            return;
        }

        Object idRaw = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
        if (idRaw instanceof Integer i) {
            idUtente = i;
        }

        setText(txtNome, dati.get(GraphicControllerUtils.KEY_NOME));
        setText(txtCognome, dati.get(GraphicControllerUtils.KEY_COGNOME));
        setText(txtEmail, dati.get(GraphicControllerUtils.KEY_EMAIL));
    }

    private void setText(TextField field, Object value) {
        if (field != null && value != null) {
            field.setText(value.toString());
        }
    }

    private String safeText(TextField field) {
        return field != null && field.getText() != null
                ? field.getText().trim()
                : "";
    }

    private String safePassword(PasswordField field) {
        return field != null && field.getText() != null
                ? field.getText()
                : "";
    }

    private void showError(String message) {
        lblError.setText(message);
        lblSuccess.setText("");
    }

    private void clearMessages() {
        if (lblError != null) {
            lblError.setText("");
        }

        if (lblSuccess != null) {
            lblSuccess.setText("");
        }
    }
}


