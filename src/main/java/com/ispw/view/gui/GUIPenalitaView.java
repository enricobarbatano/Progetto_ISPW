package com.ispw.view.gui;

import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPenalita;
import com.ispw.view.interfaces.ViewGestionePenalita;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIPenalitaView extends GenericViewGUI implements ViewGestionePenalita, NavigableController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: view GUI penalita, usa controller grafico.
    // A2) IO: componenti JavaFX e lista utenti.
    // ========================

    private final GUIGraphicControllerPenalita controller;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: costruzione UI e wiring eventi.
    // ========================

    public GUIPenalitaView(GUIGraphicControllerPenalita controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        VBox root = GuiViewUtils.createRoot();

        Label title = new Label("Penalità");
        Label error = GuiViewUtils.buildErrorLabel(getLastError());
        Label ok = GuiViewUtils.buildSuccessLabel(getLastSuccess());

        ListView<String> utentiList = new ListView<>();
        Object rawUtenti = lastParams.get(GraphicControllerUtils.KEY_UTENTI);
        if (rawUtenti instanceof List<?> utenti) {
            GuiViewUtils.fillList(utentiList, utenti);
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

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());

        root.getChildren().addAll(title, error, ok, utentiList, idUtente, importo, motivazione, lista, applica, home);
        GuiLauncher.setRoot(root);
    }
}
