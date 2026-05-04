package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginFXMLController {

    private GUIGraphicLoginController controller;

    @FXML private Label lblError;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    public void setGraphicController(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    /** Render dei messaggi in ingresso (error/success ecc.) */
    public void render(Map<String, Object> params) {
        Object err = (params != null) ? params.get(GraphicControllerUtils.KEY_ERROR) : null;
        lblError.setText(err != null ? String.valueOf(err) : "");
    }

    @FXML
    private void onLogin() {
        if (controller == null) return;
        controller.effettuaLoginRaw(txtEmail.getText(), txtPassword.getText());
    }

    @FXML
    private void onRegistrazione() {
        if (controller == null) return;
        controller.vaiARegistrazione();
    }
}