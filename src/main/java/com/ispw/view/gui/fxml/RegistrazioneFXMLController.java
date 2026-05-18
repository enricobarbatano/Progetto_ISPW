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
 * Controller FXML registrazione
 */
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
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        lblError.setText(err != null ? err.toString() : "");
    }

    @FXML
    public void onRegistra() {

        controller.inviaDatiRegistrazione(
                txtNome.getText(),
                txtCognome.getText(),
                txtEmail.getText(),
                txtPassword.getText(),
                Ruolo.UTENTE
        );
    }

    @FXML
    public void onLogin() {
        controller.vaiAlLogin();
    }
}