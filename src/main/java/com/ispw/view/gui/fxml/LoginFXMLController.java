package com.ispw.view.gui.fxml;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller FXML Login
 *
 * RESPONSABILITÀ:
 * - leggere input utente
 * - chiamare controller grafico
 *
 * NON:
 * - creare bean
 */
public class LoginFXMLController {

    private GUIGraphicLoginController controller;

    @FXML private Label lblError;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    public void init(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    public void render(Map<String, Object> params) {

        Object err = params.get(GraphicControllerUtils.KEY_ERROR);

        lblError.setText(err != null ? err.toString() : "");
    }

    @FXML
    public void onLogin() {

        controller.effettuaLogin(
                txtEmail.getText(),
                txtPassword.getText()
        );
    }

    @FXML
    public void onRegistrazione() {
        controller.vaiARegistrazione();
    }
}