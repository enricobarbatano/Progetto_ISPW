package com.ispw.view.gui;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.view.interfaces.ViewGestioneRegole;

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

        VBox root = GuiViewUtils.createRoot();

        Label title = new Label("Regole");
        Label error = GuiViewUtils.buildErrorLabel(getLastError());
        Label ok = GuiViewUtils.buildSuccessLabel(getLastSuccess());

        ListView<String> campiList = new ListView<>();
        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);
        if (rawCampi instanceof List<?> campi) {
            GuiViewUtils.fillList(campiList, campi);
        }

        TextField idCampo = new TextField();
        idCampo.setPromptText("Id campo");

        Button lista = new Button("Lista campi");
        lista.setOnAction(e -> controller.richiediListaCampi());

        Button seleziona = new Button("Seleziona campo");
        seleziona.setOnAction(e -> controller.selezionaCampo(parseInt(idCampo.getText())));

        CheckBox attivo = new CheckBox("Attivo");
        CheckBox manut = new CheckBox("Manutenzione");
        Button aggiornaStato = new Button("Aggiorna stato campo");
        aggiornaStato.setOnAction(e -> {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put(GraphicControllerUtils.KEY_ID_CAMPO, parseInt(idCampo.getText()));
            payload.put(GraphicControllerUtils.KEY_ATTIVO, attivo.isSelected());
            payload.put(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE, manut.isSelected());
            controller.aggiornaStatoCampo(payload);
        });

        TextField durata = new TextField();
        durata.setPromptText("Durata slot (min)");
        TextField apertura = new TextField();
        apertura.setPromptText("Ora apertura (HH:mm)");
        TextField chiusura = new TextField();
        chiusura.setPromptText("Ora chiusura (HH:mm)");
        TextField preavviso = new TextField();
        preavviso.setPromptText("Preavviso minimo (min)");
        Button aggiornaTemp = new Button("Aggiorna tempistiche");
        aggiornaTemp.setOnAction(e -> {
            try {
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI, parseInt(durata.getText()));
                payload.put(GraphicControllerUtils.KEY_ORA_APERTURA, LocalTime.parse(apertura.getText().trim()));
                payload.put(GraphicControllerUtils.KEY_ORA_CHIUSURA, LocalTime.parse(chiusura.getText().trim()));
                payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, parseInt(preavviso.getText()));
                controller.aggiornaTempistiche(payload);
            } catch (RuntimeException ex) {
                error.setText("Dati tempistiche non validi");
            }
        });

        TextField valorePen = new TextField();
        valorePen.setPromptText("Valore penalità");

        Button aggiornaPen = new Button("Aggiorna penalità");
        aggiornaPen.setOnAction(e -> {
            try {
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put(GraphicControllerUtils.KEY_VALORE_PENALITA, new BigDecimal(valorePen.getText().trim()));
                payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, parseIntOrDefault(preavviso.getText(), 0));
                controller.aggiornaPenalita(payload);
            } catch (RuntimeException ex) {
                error.setText("Dati penalità non validi");
            }
        });

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());

        root.getChildren().addAll(title, error, ok, campiList, idCampo, lista, seleziona, attivo, manut,
            aggiornaStato, durata, apertura, chiusura, preavviso, aggiornaTemp, valorePen, aggiornaPen, home);
        GuiLauncher.setRoot(root);
    }

    private Integer parseInt(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        int result = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            int digit = Character.digit(trimmed.charAt(i), 10);
            if (digit < 0) return null;
            result = result * 10 + digit;
        }
        return result;
    }

    private Integer parseIntOrDefault(String value, int defaultValue) {
        Integer parsed = parseInt(value);
        return parsed != null ? parsed : defaultValue;
    }
}
