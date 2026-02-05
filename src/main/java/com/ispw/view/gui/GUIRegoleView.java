package com.ispw.view.gui;

import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.view.interfaces.ViewGestioneRegole;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIRegoleView extends GenericViewGUI implements ViewGestioneRegole, NavigableController {

    private final GUIGraphicControllerRegole controller;

    public GUIRegoleView(GUIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
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

        Label title = new Label("Regole");
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

        ListView<String> campiList = new ListView<>();
        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);
        if (rawCampi instanceof List<?> campi) {
            for (Object c : campi) {
                campiList.getItems().add(String.valueOf(c));
            }
        }

        TextField idCampo = new TextField();
        idCampo.setPromptText("Id campo");

        Button lista = new Button("Lista campi");
        lista.setOnAction(e -> controller.richiediListaCampi());

        Button seleziona = new Button("Seleziona campo");
        seleziona.setOnAction(e -> controller.selezionaCampo(Integer.parseInt(idCampo.getText().trim())));

        CheckBox attivo = new CheckBox("Attivo");
        CheckBox manut = new CheckBox("Manutenzione");
        Button aggiornaStato = new Button("Aggiorna stato campo");
        aggiornaStato.setOnAction(e -> {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put(GraphicControllerUtils.KEY_ID_CAMPO, Integer.parseInt(idCampo.getText().trim()));
            payload.put(GraphicControllerUtils.KEY_ATTIVO, attivo.isSelected());
            payload.put(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE, manut.isSelected());
            controller.aggiornaStatoCampo(payload);
        });

        TextField durata = new TextField();
        durata.setPromptText("Durata slot (min)");
        TextField preavviso = new TextField();
        preavviso.setPromptText("Preavviso minimo (min)");
        Button aggiornaTemp = new Button("Aggiorna tempistiche");
        aggiornaTemp.setOnAction(e -> {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI, Integer.parseInt(durata.getText().trim()));
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, Integer.parseInt(preavviso.getText().trim()));
            controller.aggiornaTempistiche(payload);
        });

        Button aggiornaPen = new Button("Aggiorna penalità");
        aggiornaPen.setOnAction(e -> {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, Integer.parseInt(preavviso.getText().trim()));
            controller.aggiornaPenalita(payload);
        });

        Button home = new Button("Home");
        home.setOnAction(e -> controller.tornaAllaHome());

        root.getChildren().addAll(title, error, ok, campiList, idCampo, lista, seleziona, attivo, manut,
            aggiornaStato, durata, preavviso, aggiornaTemp, aggiornaPen, home);
        GuiLauncher.setRoot(root);
    }
}
