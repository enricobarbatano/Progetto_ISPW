package com.ispw.view.gui;

import java.util.List;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.view.interfaces.ViewGestionePrenotazione;
import com.ispw.view.shared.PrenotazioneViewUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GUIPrenotazioneView extends GenericViewGUI implements ViewGestionePrenotazione, NavigableController {

    private final GUIGraphicControllerPrenotazione controller;
    private int lastCampoId;

    public GUIPrenotazioneView(GUIGraphicControllerPrenotazione controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
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

        String err = getLastError();
        if (err != null && !err.isBlank()) {
            renderMessage("Errore: " + err);
            return;
        }

        if (handlePagamento()) return;
        if (handleRiepilogo()) return;
        if (handleSlots()) return;

        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);
        if (!(rawCampi instanceof List<?> campiObj)) {
            controller.richiediListaCampi(sessione);
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> campi = (List<String>) campiObj;
        renderSearch(campi);
    }

    private void renderMessage(String msg) {
        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label(msg));
        GuiLauncher.setRoot(root);
    }

    private boolean handleSlots() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI);
        if (!(raw instanceof List<?> slotsObj)) return false;
        @SuppressWarnings("unchecked")
        List<String> slots = (List<String>) slotsObj;

        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label("Slot disponibili"));

        ListView<String> list = new ListView<>();
        GuiViewUtils.fillList(list, slots);

        Button select = new Button("Seleziona slot");
        select.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            String slot = slots.get(idx);
            PrenotazioneViewUtils.SlotInfo info = PrenotazioneViewUtils.parseSlot(slot);
            if (info == null) return;
            controller.creaPrenotazioneRaw(lastCampoId, info.data(), info.oraInizio(), info.oraFine(), sessione);
        });

        root.getChildren().addAll(list, select);
        GuiLauncher.setRoot(root);
        return true;
    }

    private boolean handleRiepilogo() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_RIEPILOGO);
        if (!(raw instanceof Map<?, ?> riepilogo)) return false;

        Object riepilogoStr = riepilogo.get(GraphicControllerUtils.KEY_RIEPILOGO);
        Object importo = riepilogo.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);
        float importoVal = (importo instanceof Number n) ? n.floatValue() : 0f;

        VBox root = GuiViewUtils.createRoot();
        root.getChildren().addAll(new Label("Riepilogo prenotazione"), new Label(String.valueOf(riepilogoStr)));

        TextField metodo = new TextField("PAYPAL");
        metodo.setPromptText("Metodo");
        TextField cred = new TextField();
        cred.setPromptText("Inserisci il codice fiscale per la fatturazione");

        Button paga = new Button("Paga");
        paga.setOnAction(e -> controller.procediAlPagamentoRaw(metodo.getText(), cred.getText(), importoVal, sessione));

        root.getChildren().addAll(metodo, cred, paga);
        GuiLauncher.setRoot(root);
        return true;
    }

    private boolean handlePagamento() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PAGAMENTO);
        if (!(raw instanceof Map<?, ?> pagamento)) return false;

        Object success = pagamento.get(GraphicControllerUtils.KEY_SUCCESSO);
        Object stato = pagamento.get(GraphicControllerUtils.KEY_STATO);
        Object msg = pagamento.get(GraphicControllerUtils.KEY_MESSAGGIO);

        VBox root = GuiViewUtils.createRoot();
        root.getChildren().add(new Label("Esito pagamento"));
        root.getChildren().add(new Label(PrenotazioneViewUtils.formatEsitoPagamento(success, stato, msg)));

        Button home = GuiViewUtils.buildHomeButton(() -> controller.tornaAllaHome());
        root.getChildren().add(home);

        GuiLauncher.setRoot(root);
        return true;
    }

    private void renderSearch(List<String> campi) {
        VBox root = GuiViewUtils.createRoot();

        Label title = new Label("Prenotazione");
        ListView<String> campiList = new ListView<>();
        GuiViewUtils.fillList(campiList, campi);

        TextField idCampo = new TextField();
        idCampo.setPromptText("Id campo");
        TextField data = new TextField();
        data.setPromptText("Data (yyyy-MM-dd)");
        TextField ora = new TextField();
        ora.setPromptText("Ora inizio (HH:mm)");
        TextField durata = new TextField();
        durata.setPromptText("Durata (min)");

        Button cerca = new Button("Cerca disponibilità");
        cerca.setOnAction(e -> {
            int id = Integer.parseInt(idCampo.getText().trim());
            int dur = Integer.parseInt(durata.getText().trim());
            lastCampoId = id;
            controller.cercaDisponibilitaRaw(id, data.getText(), ora.getText(), dur);
        });

        root.getChildren().addAll(title, campiList, idCampo, data, ora, durata, cerca);
        GuiLauncher.setRoot(root);
    }

    // The parseSlot method and SlotInfo record are now handled by PrenotazioneViewUtils
}
