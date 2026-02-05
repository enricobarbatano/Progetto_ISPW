package com.ispw.view.gui;

import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.view.interfaces.ViewGestionePenalita;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIPenalitaView extends GenericViewGUI implements ViewGestionePenalita, NavigableController {

    private final GUIGraphicControllerPenalita controller;

    public GUIPenalitaView(GUIGraphicControllerPenalita controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
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

        Label title = new Label("Penalità");
        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");
        String err = getLastError();
        if (err != null && !err.isBlank()) {
            error.setText(err);
        }
        Label ok = new Label();
        String success = getLastSuccess();
        if (success != null && !success.isBlank()) {
            ok.setText(success);
        }

        ListView<String> utentiList = new ListView<>();
        Object rawUtenti = lastParams.get(GraphicControllerUtils.KEY_UTENTI);
        if (rawUtenti instanceof List<?> utenti) {
            for (Object u : utenti) {
                utentiList.getItems().add(String.valueOf(u));
            }
        }

        TextField idUtente = new TextField();
        idUtente.setPromptText("Id utente");
        TextField importo = new TextField();
        importo.setPromptText("Importo");
        TextField motivazione = new TextField();
        motivazione.setPromptText("Motivazione");

        Button lista = new Button("Lista utenti");
        lista.setOnAction(e -> controller.richiediListaUtenti());

        Button applica = new Button("Applica penalità");
        applica.setOnAction(e -> controller.applicaPenalita(
            Integer.parseInt(idUtente.getText().trim()),
            Float.parseFloat(importo.getText().trim()),
            motivazione.getText()));

        Button home = new Button("Home");
        home.setOnAction(e -> controller.tornaAllaHome());

        root.getChildren().addAll(title, error, ok, utentiList, idUtente, importo, motivazione, lista, applica, home);
        GuiLauncher.setRoot(root);
    }
}
