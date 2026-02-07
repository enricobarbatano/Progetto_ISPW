package com.ispw.view.gui;

import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.view.interfaces.ViewGestioneAccount;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIAccountView extends GenericViewGUI implements ViewGestioneAccount, NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: view GUI account, usa controller grafico.
    // A2) IO: componenti JavaFX e params.

    private final GUIGraphicControllerAccount controller;

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: costruzione UI e wiring eventi.
    // L2) setup/load/build*: supporto campi e payload.

    public GUIAccountView(GUIGraphicControllerAccount controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        VBox root = GuiViewUtils.createRoot();
        Label title = new Label("Account");
        Label error = GuiViewUtils.buildErrorLabel(getLastError());
        Label success = GuiViewUtils.buildSuccessLabel(getLastSuccess());

        TextField nome = new TextField();
        TextField cognome = new TextField();
        TextField email = new TextField();
        setupAccountFields(nome, cognome, email);

        Integer idUtente = loadAccountData(nome, cognome, email);

        PasswordField oldPwd = new PasswordField();
        oldPwd.setPromptText("Vecchia password");
        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("Nuova password (min 6 caratteri)");

        Button load = new Button("Ricarica dati");
        load.setOnAction(e -> controller.loadAccount(sessione));

        Button update = new Button("Aggiorna");
        update.setOnAction(e -> {
            if (idUtente == null) {
                controller.loadAccount(sessione);
                return;
            }
            controller.aggiornaDatiAccount(buildUpdateMap(idUtente, nome, cognome, email));
        });

        Button changePwd = new Button("Cambia password");
        changePwd.setOnAction(e -> {
            if (newPwd.getText() == null || newPwd.getText().trim().length() < 6) {
                return;
            }
            controller.cambiaPassword(oldPwd.getText(), newPwd.getText(), sessione);
        });

        Button logout = new Button("Logout");
        logout.setOnAction(e -> controller.logout());

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome(sessione));

        root.getChildren().addAll(title, error, success, nome, cognome, email, load, update, oldPwd, newPwd,
            changePwd, logout, home);
        GuiLauncher.setRoot(root);
    }

    private void setupAccountFields(TextField nome, TextField cognome, TextField email) {
        nome.setPromptText("Nome");
        cognome.setPromptText("Cognome");
        email.setPromptText("Email");
    }

    private Integer loadAccountData(TextField nome, TextField cognome, TextField email) {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);
        if (!(raw instanceof Map<?, ?> dati)) {
            return null;
        }
        Object id = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
        Object n = dati.get(GraphicControllerUtils.KEY_NOME);
        Object c = dati.get(GraphicControllerUtils.KEY_COGNOME);
        Object e = dati.get(GraphicControllerUtils.KEY_EMAIL);
        if (n != null) nome.setText(String.valueOf(n));
        if (c != null) cognome.setText(String.valueOf(c));
        if (e != null) email.setText(String.valueOf(e));
        return (id instanceof Integer i) ? i : null;
    }

    private Map<String, Object> buildUpdateMap(Integer idUtente,
                                               TextField nome,
                                               TextField cognome,
                                               TextField email) {
        Map<String, Object> updateMap = new HashMap<>();
        if (idUtente == null) {
            return updateMap;
        }
        updateMap.put(GraphicControllerUtils.KEY_ID_UTENTE, idUtente);
        updateMap.put(GraphicControllerUtils.KEY_SESSIONE, sessione);
        if (!nome.getText().isBlank()) updateMap.put(GraphicControllerUtils.KEY_NOME, nome.getText());
        if (!cognome.getText().isBlank()) updateMap.put(GraphicControllerUtils.KEY_COGNOME, cognome.getText());
        if (!email.getText().isBlank()) updateMap.put(GraphicControllerUtils.KEY_EMAIL, email.getText());
        return updateMap;
    }
}
