package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.view.shared.RegistrazioneViewUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrazioneFXMLController {

    private GUIGraphicControllerRegistrazione controller;

    @FXML private Label lblError;
    @FXML private TextField txtNome;
    @FXML private TextField txtCognome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    public void init(GUIGraphicControllerRegistrazione controller) {
        this.controller = controller;
    }

    public void render(Map<String, Object> params) {
        Object err = params != null ? params.get(GraphicControllerUtils.KEY_ERROR) : null;
        lblError.setText(err != null ? String.valueOf(err) : "");
    }

    @FXML private void onRegistra() {
        if (controller == null) return;
        controller.inviaDatiRegistrazione(
                RegistrazioneViewUtils.buildForm(
                        txtNome.getText(),
                        txtCognome.getText(),
                        txtEmail.getText(),
                        txtPassword.getText()
                )
        );
    }

    @FXML private void onLogin() {
        if (controller == null) return;
        controller.vaiAlLogin();
    }
}