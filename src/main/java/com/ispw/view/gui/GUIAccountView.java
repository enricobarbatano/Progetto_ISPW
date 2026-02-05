package com.ispw.view.gui;

import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerAccount;
import com.ispw.view.interfaces.ViewGestioneAccount;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIAccountView extends GenericViewGUI implements ViewGestioneAccount, NavigableController {

    private final GUIGraphicControllerAccount controller;

    public GUIAccountView(GUIGraphicControllerAccount controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow() {
        onShow(Map.of());
    }

    @Override
    public void onHide() {
        // no-op
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));

        Label title = new Label("Account");
        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");
        String err = getLastError();
        if (err != null && !err.isBlank()) {
            error.setText(err);
        }
        Label success = new Label();
        String ok = getLastSuccess();
        if (ok != null && !ok.isBlank()) {
            success.setText(ok);
        }

        TextField nome = new TextField();
        nome.setPromptText("Nome");
        TextField cognome = new TextField();
        cognome.setPromptText("Cognome");
        TextField email = new TextField();
        email.setPromptText("Email");

        Object raw = lastParams.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);
        Integer idUtente = null;
        if (raw instanceof Map<?, ?> dati) {
            Object id = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
            Object n = dati.get(GraphicControllerUtils.KEY_NOME);
            Object c = dati.get(GraphicControllerUtils.KEY_COGNOME);
            Object e = dati.get(GraphicControllerUtils.KEY_EMAIL);
            if (id instanceof Integer i) {
                idUtente = i;
            }
            if (n != null) nome.setText(String.valueOf(n));
            if (c != null) cognome.setText(String.valueOf(c));
            if (e != null) email.setText(String.valueOf(e));
        }

        final Integer idFinal = idUtente;

        Button load = new Button("Ricarica dati");
        load.setOnAction(e -> controller.loadAccount(sessione));

        Button update = new Button("Aggiorna");
        update.setOnAction(e -> {
            if (idFinal == null) {
                return;
            }
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put(GraphicControllerUtils.KEY_ID_UTENTE, idFinal);
            if (!nome.getText().isBlank()) updateMap.put(GraphicControllerUtils.KEY_NOME, nome.getText());
            if (!cognome.getText().isBlank()) updateMap.put(GraphicControllerUtils.KEY_COGNOME, cognome.getText());
            if (!email.getText().isBlank()) updateMap.put(GraphicControllerUtils.KEY_EMAIL, email.getText());
            controller.aggiornaDatiAccount(updateMap);
        });

        PasswordField oldPwd = new PasswordField();
        oldPwd.setPromptText("Vecchia password");
        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("Nuova password");
        Button changePwd = new Button("Cambia password");
        changePwd.setOnAction(e -> controller.cambiaPassword(oldPwd.getText(), newPwd.getText(), sessione));

        Button logout = new Button("Logout");
        logout.setOnAction(e -> controller.logout());

        root.getChildren().addAll(title, error, success, nome, cognome, email, load, update, oldPwd, newPwd, changePwd, logout);
        GuiLauncher.setRoot(root);
    }
}
