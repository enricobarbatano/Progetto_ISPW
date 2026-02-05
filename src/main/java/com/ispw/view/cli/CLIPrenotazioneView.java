package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerPrenotazione;
import com.ispw.view.cli.console.ConsolePagamentoView;
import com.ispw.view.cli.console.ConsolePrenotazioneConfirmView;
import com.ispw.view.cli.console.ConsolePrenotazioneSearchView;
import com.ispw.view.interfaces.ViewGestionePrenotazione;

/**
 * View CLI per prenotazione campo.
 */
public class CLIPrenotazioneView extends GenericViewCLI implements ViewGestionePrenotazione, NavigableController {

    private final CLIGraphicControllerPrenotazione controller;
    private final ConsolePrenotazioneSearchView searchView = new ConsolePrenotazioneSearchView();
    private final ConsolePrenotazioneConfirmView confirmView = new ConsolePrenotazioneConfirmView();
    private final ConsolePagamentoView pagamentoView = new ConsolePagamentoView();
    private final Scanner in = new Scanner(System.in);

    private int lastCampoId;

    public CLIPrenotazioneView(CLIGraphicControllerPrenotazione controller) {
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
            System.err.println("[ERRORE] " + err);
        }

        if (handlePagamentoPayload()) {
            return;
        }
        if (handleRiepilogoPayload()) {
            return;
        }
        if (handleSlotDisponibiliPayload()) {
            return;
        }

        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);
        if (!(rawCampi instanceof List<?> campiObj)) {
            controller.richiediListaCampi(sessione);
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> campi = (List<String>) campiObj;
        searchView.showCampi(campi);

        // default: avvia ricerca
        searchView.renderSearchForm();
        lastCampoId = searchView.readCampoId();
        String data = searchView.readData();
        String ora = searchView.readOraInizio();
        int durata = searchView.readDurataMin();

        controller.cercaDisponibilitaRaw(lastCampoId, data, ora, durata);
    }

    private boolean handleSlotDisponibiliPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI);
        if (!(raw instanceof List<?> slotsObj)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<String> slots = (List<String>) slotsObj;
        searchView.showResults(slots);
        int sel = searchView.askSlotSelectionIndex(slots.size());
        if (sel < 0) {
            return true;
        }
        String slot = slots.get(sel);
        SlotInfo info = parseSlot(slot);
        if (info == null) {
            searchView.showError("Formato slot non valido");
            return true;
        }
        controller.creaPrenotazioneRaw(lastCampoId, info.data, info.oraInizio, info.oraFine, sessione);
        return true;
    }

    private boolean handleRiepilogoPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_RIEPILOGO);
        if (!(raw instanceof Map<?, ?> riepilogo)) {
            return false;
        }
        Object riepilogoStr = riepilogo.get(GraphicControllerUtils.KEY_RIEPILOGO);
        Object importo = riepilogo.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);

        confirmView.renderSummary(String.valueOf(riepilogoStr));
        boolean conferma = confirmView.askConfirmation();
        if (!conferma) {
            confirmView.showInfo("Operazione annullata");
            return true;
        }

        float importoVal = (importo instanceof Number n) ? n.floatValue() : 0f;
        pagamentoView.renderPaymentForm(importoVal, "PAYPAL");
        String metodo = pagamentoView.readMetodoPagamento();
        String cred = pagamentoView.readCredenzialiPagamento();

        controller.procediAlPagamentoRaw(metodo, cred, importoVal, sessione);
        return true;
    }

    private boolean handlePagamentoPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PAGAMENTO);
        if (!(raw instanceof Map<?, ?> pagamento)) {
            return false;
        }
        Object success = pagamento.get(GraphicControllerUtils.KEY_SUCCESSO);
        Object stato = pagamento.get(GraphicControllerUtils.KEY_STATO);
        Object msg = pagamento.get(GraphicControllerUtils.KEY_MESSAGGIO);

        String esito = String.format("Esito: %s - stato: %s - %s", success, stato, msg);
        pagamentoView.showPaymentOutcome(esito);
        System.out.print("Torna alla home? [s/N]: ");
        String ans = in.nextLine().trim();
        if ("s".equalsIgnoreCase(ans)) {
            controller.tornaAllaHome();
        }
        return true;
    }

    private SlotInfo parseSlot(String slot) {
        if (slot == null) return null;
        String[] parts = slot.split(" ");
        if (parts.length < 2) return null;
        String data = parts[0];
        String times = parts[1];
        int dash = times.indexOf('-');
        if (dash <= 0) return null;
        String oraInizio = times.substring(0, dash);
        String oraFine = times.substring(dash + 1);
        return new SlotInfo(data, oraInizio, oraFine);
    }

    private record SlotInfo(String data, String oraInizio, String oraFine) { }
}
