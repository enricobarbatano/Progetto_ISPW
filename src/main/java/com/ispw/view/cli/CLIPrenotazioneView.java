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
 * View CLI per la prenotazione campo.
 *
 * RESPONSABILITÀ:
 * - mostrare campi disponibili;
 * - raccogliere input utente;
 * - mostrare slot, riepilogo e pagamento;
 * - chiamare il graphic controller con parametri semplici.
 *
 * NON:
 * - crea bean;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - costruisce Map applicative per il layer logico.
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
            controller.richiediListaCampi();
            return;
        }

        List<String> campi = campiObj.stream()
                .map(Object::toString)
                .toList();

        searchView.showCampi(campi);
        searchView.renderSearchForm();

        lastCampoId = searchView.readCampoId();
        String data = searchView.readData();
        String ora = searchView.readOraInizio();
        int durata = searchView.readDurataMin();

        controller.cercaDisponibilita(
                lastCampoId,
                data,
                ora,
                durata
        );
    }

    private boolean handleSlotDisponibiliPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI);

        if (!(raw instanceof List<?> slotsObj)) {
            return false;
        }

        List<String> slots = slotsObj.stream()
                .map(Object::toString)
                .toList();

        searchView.showResults(slots);

        if (slots.isEmpty()) {
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        int selectedIndex = searchView.askSlotSelectionIndex(slots.size());

        if (selectedIndex < 0) {
            System.out.println("Selezione annullata.");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        String slot = slots.get(selectedIndex);
        PrenotazioneViewUtils.SlotInfo info = PrenotazioneViewUtils.parseSlot(slot);

        if (info == null) {
            searchView.showError("Formato slot non valido");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        if (lastCampoId <= 0) {
            searchView.showError("Campo non selezionato correttamente");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        controller.creaPrenotazione(
                lastCampoId,
                info.data(),
                info.oraInizio(),
                info.oraFine(),
                sessione
        );

        return true;
    }

    private boolean handleRiepilogoPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_RIEPILOGO);

        if (!(raw instanceof Map<?, ?> riepilogo)) {
            return false;
        }

        Object riepilogoRaw = riepilogo.get(GraphicControllerUtils.KEY_RIEPILOGO);
        Object importoRaw = riepilogo.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);

        String riepilogoText = riepilogoRaw != null ? riepilogoRaw.toString() : "";

        if (riepilogoText.isBlank()) {
            confirmView.showInfo("Riepilogo non disponibile.");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        confirmView.renderSummary(riepilogoText);

        boolean conferma = confirmView.askConfirmation();

        if (!conferma) {
            confirmView.showInfo("Operazione annullata.");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        float importo = importoRaw instanceof Number n ? n.floatValue() : 0f;

        if (importo <= 0f) {
            pagamentoView.showError("Importo non valido");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        pagamentoView.renderPaymentForm(importo, "PAYPAL");

        String metodo = pagamentoView.readMetodoPagamento();
        String credenziale = pagamentoView.readCredenzialiPagamento();

        controller.procediAlPagamento(
                metodo,
                credenziale,
                importo,
                sessione
        );

        return true;
    }

    private boolean handlePagamentoPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PAGAMENTO);

        if (!(raw instanceof Map<?, ?> pagamento)) {
            return false;
        }

        Object success = pagamento.get(GraphicControllerUtils.KEY_SUCCESSO);
        Object stato = pagamento.get(GraphicControllerUtils.KEY_STATO);
        Object messaggio = pagamento.get(GraphicControllerUtils.KEY_MESSAGGIO);

        String esito = PrenotazioneViewUtils.formatEsitoPagamento(
                success,
                stato,
                messaggio
        );

        pagamentoView.showPaymentOutcome(esito);
        CliViewUtils.askReturnHome(in, controller::tornaAllaHome);

        return true;
    }
}