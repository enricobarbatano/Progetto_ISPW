package com.ispw.view.gui;

import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegistrazione;
import com.ispw.view.interfaces.ViewRegistrazione;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIRegistrazioneView extends GenericViewGUI implements ViewRegistrazione, NavigableController {

    private final GUIGraphicControllerRegistrazione controller;

    public GUIRegistrazioneView(GUIGraphicControllerRegistrazione controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
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
        sessione = null;

        VBox root = GuiViewUtils.createRoot();

        Label title = new Label("Registrazione");
        Label error = GuiViewUtils.buildErrorLabel(getLastError());

        TextField nome = new TextField();
        nome.setPromptText("Nome");
        TextField cognome = new TextField();
        cognome.setPromptText("Cognome");
        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button submit = new Button("Registra");
        submit.setOnAction(e -> {
            Map<String, Object> form = new HashMap<>();
            form.put(GraphicControllerUtils.KEY_NOME, nome.getText());
            form.put(GraphicControllerUtils.KEY_COGNOME, cognome.getText());
            form.put(GraphicControllerUtils.KEY_EMAIL, email.getText());
            form.put(GraphicControllerUtils.KEY_PASSWORD, password.getText());
            controller.inviaDatiRegistrazione(form);
        });

        root.getChildren().addAll(title, error, nome, cognome, email, password, submit);
        GuiLauncher.setRoot(root);
    }
}
