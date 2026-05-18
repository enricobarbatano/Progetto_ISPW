package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.cli.console.ConsolePagamentoView;
import com.ispw.view.cli.console.ConsolePrenotazioneConfirmView;
import com.ispw.view.cli.console.ConsolePrenotazioneSearchView;
import com.ispw.view.interfaces.ViewGestionePrenotazione;
import com.ispw.view.shared.PrenotazioneViewUtils;

/**
 * View CLI per prenotazione campo.
 *
 * RESPONSABILITÀ:
 * - mostra campi disponibili
 * - raccoglie input utente
 * - chiama il GraphicController con parametri semplici
 *
 * IMPORTANTE:
 * ❌ NON crea Bean
 * ❌ NON usa Map per input
 * ✅ usa solo parametri primitivi
 */
public class CLIPrenotazioneView extends GenericViewCLI
        implements ViewGestionePrenotazione, NavigableController {

    private final CLIGraphicControllerPrenotazione controller;

    private final ConsolePrenotazioneSearchView searchView =
            new ConsolePrenotazioneSearchView();

    private final ConsolePrenotazioneConfirmView confirmView =
            new ConsolePrenotazioneConfirmView();

    private final ConsolePagamentoView pagamentoView =
            new ConsolePagamentoView();

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
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        // STEP pagamento
        if (handlePagamentoPayload()) return;

        // STEP riepilogo
        if (handleRiepilogoPayload()) return;

        // STEP slot disponibili
        if (handleSlotDisponibiliPayload()) return;

        // STEP iniziale → lista campi
        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);

        if (!(rawCampi instanceof List<?> campiObj)) {
            controller.richiediListaCampi();
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> campi = (List<String>) campiObj;

        searchView.showCampi(campi);

        // richiesta ricerca
        searchView.renderSearchForm();

        lastCampoId = searchView.readCampoId();
        String data = searchView.readData();
        String ora = searchView.readOraInizio();
        int durata = searchView.readDurataMin();

        // ✅ FIX: metodo corretto
        controller.cercaDisponibilita(
                lastCampoId,
                data,
                ora,
                durata
        );
    }

    // =========================================================
    // SLOT DISPONIBILI
    // =========================================================

    private boolean handleSlotDisponibiliPayload() {

        Object raw = lastParams.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI);

        if (!(raw instanceof List<?> slotsObj)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        List<String> slots = (List<String>) slotsObj;

        searchView.showResults(slots);

        if (slots.isEmpty()) {
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        int sel = searchView.askSlotSelectionIndex(slots.size());

        if (sel < 0) return true;

        String slot = slots.get(sel);

        PrenotazioneViewUtils.SlotInfo info =
                PrenotazioneViewUtils.parseSlot(slot);

        if (info == null) {
            searchView.showError("Formato slot non valido");
            return true;
        }

        // ✅ FIX: metodo corretto
        controller.creaPrenotazione(
                lastCampoId,
                info.data(),
                info.oraInizio(),
                info.oraFine(),
                sessione
        );

        return true;
    }

    // =========================================================
    // RIEPILOGO
    // =========================================================

    private boolean handleRiepilogoPayload() {

        Object raw = lastParams.get(GraphicControllerUtils.KEY_RIEPILOGO);

        if (!(raw instanceof Map<?, ?> riepilogo)) {
            return false;
        }

        Object riepilogoStr =
                riepilogo.get(GraphicControllerUtils.KEY_RIEPILOGO);

        Object importo =
                riepilogo.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);

        String riepilogoText =
                (riepilogoStr == null) ? "" : riepilogoStr.toString();

        if (riepilogoText.isBlank()) {
            confirmView.showInfo("Nessuno slot disponibile.");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        confirmView.renderSummary(riepilogoText);

        boolean conferma = confirmView.askConfirmation();

        if (!conferma) {
            confirmView.showInfo("Operazione annullata");
            return true;
        }

        float importoVal =
                (importo instanceof Number n) ? n.floatValue() : 0f;

        pagamentoView.renderPaymentForm(importoVal, "PAYPAL");

        String metodo = pagamentoView.readMetodoPagamento();
        String cred = pagamentoView.readCredenzialiPagamento();

        // ✅ FIX: metodo corretto
        controller.procediAlPagamento(
                metodo,
                cred,
                importoVal,
                sessione
        );

        return true;
    }

    // =========================================================
    // PAGAMENTO
    // =========================================================

    private boolean handlePagamentoPayload() {

        Object raw = lastParams.get(GraphicControllerUtils.KEY_PAGAMENTO);

        if (!(raw instanceof Map<?, ?> pagamento)) {
            return false;
        }

        Object success = pagamento.get(GraphicControllerUtils.KEY_SUCCESSO);
        Object stato = pagamento.get(GraphicControllerUtils.KEY_STATO);
        Object msg = pagamento.get(GraphicControllerUtils.KEY_MESSAGGIO);

        String esito =
                PrenotazioneViewUtils.formatEsitoPagamento(success, stato, msg);

        pagamentoView.showPaymentOutcome(esito);

        CliViewUtils.askReturnHome(in, controller::tornaAllaHome);

        return true;
    }
}