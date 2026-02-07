package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.view.interfaces.ViewLogin;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUILoginView extends GenericViewGUI implements ViewLogin, NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: view GUI login, usa controller grafico.
    // A2) IO: componenti JavaFX per credenziali.

    private final GUIGraphicLoginController controller;
    private Label errorLabel;
    private TextField emailField;
    private PasswordField passwordField;

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: costruzione UI e wiring eventi.

    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        sessione = null;

        VBox root = GuiViewUtils.createRoot();

        Label title = new Label("Login");
        errorLabel = GuiViewUtils.buildErrorLabel(getLastError());

        emailField = new TextField();
        emailField.setPromptText("Email");
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        loginBtn.setOnAction(e -> controller.effettuaLoginRaw(emailField.getText(), passwordField.getText()));

        Button regBtn = new Button("Registrazione");
        regBtn.setOnAction(e -> controller.vaiARegistrazione());

        if (errorLabel.getText() == null) {
            errorLabel.setText("");
        }

        root.getChildren().addAll(title, errorLabel, emailField, passwordField, loginBtn, regBtn);
        GuiLauncher.setRoot(root);
    }
}
