package com.ispw.view.gui;

import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerDisdetta;
import com.ispw.view.interfaces.ViewDisdettaPrenotazione;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIDisdettaView extends GenericViewGUI implements ViewDisdettaPrenotazione, NavigableController {

    private final GUIGraphicControllerDisdetta controller;

    public GUIDisdettaView(GUIGraphicControllerDisdetta controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        String err = getLastError();
        if (err != null && !err.isBlank()) {
            renderMessage("Errore: " + err);
            return;
        }

        if (handleSuccess()) return;
        if (handleAnteprima()) return;
        if (handleElenco()) return;

        controller.richiediPrenotazioniCancellabili(sessione);
    }

    private void renderMessage(String msg) {
        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label(msg));
        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());
        root.getChildren().add(home);
        GuiLauncher.setRoot(root);
    }

    private boolean handleElenco() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PRENOTAZIONI);
        if (!(raw instanceof List<?> elenco)) return false;
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) elenco;

        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label("Prenotazioni cancellabili"));

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(list);

        TextField idField = new TextField();
        idField.setPromptText("Id prenotazione");

        Button anteprima = new Button("Anteprima disdetta");
        anteprima.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText().trim());
            controller.richiediAnteprimaDisdetta(id, sessione);
        });

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());

        root.getChildren().addAll(listView, idField, anteprima, home);
        GuiLauncher.setRoot(root);
        return true;
    }

    private boolean handleAnteprima() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_ANTEPRIMA);
        if (!(raw instanceof Map<?, ?> anteprima)) return false;

        Object possibile = anteprima.get(GraphicControllerUtils.KEY_POSSIBILE);
        Object penale = anteprima.get(GraphicControllerUtils.KEY_PENALE);
        boolean poss = possibile instanceof Boolean b && b;
        float pen = penale instanceof Number n ? n.floatValue() : 0f;

        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label("Anteprima disdetta"));
        root.getChildren().add(new Label("Possibile: " + poss + " - penale: " + pen + "€"));

        TextField idField = new TextField();
        idField.setPromptText("Id prenotazione");

        Button conferma = new Button("Conferma disdetta");
        conferma.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText().trim());
            controller.confermaDisdetta(id, sessione);
        });

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());

        root.getChildren().addAll(idField, conferma, home);
        GuiLauncher.setRoot(root);
        return true;
    }

    private boolean handleSuccess() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_SUCCESSO);
        if (raw == null) return false;
        renderMessage(String.valueOf(raw));
        return true;
    }
}
