package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.cli.console.ConsoleRichiesteDisdettaDecisionView;
import com.ispw.view.cli.console.ConsoleRichiesteDisdettaElencoView;

/**
 * View CLI per la gestione delle richieste di disdetta lato gestore.
 *
 * RESPONSABILITÀ:
 * - mostrare le richieste pending;
 * - leggere id richiesta e nota gestore;
 * - delegare approvazione e rifiuto al graphic controller.
 *
 * NON:
 * - crea bean;
 * - chiama logic controller;
 * - accede a DAO o persistenza.
 */
public class CLIRichiesteDisdettaView extends GenericViewCLI implements NavigableController {

    private final CLIGraphicControllerRichiesteDisdetta controller;

    private final Scanner in = new Scanner(System.in);
    private final ConsoleRichiesteDisdettaElencoView elencoView =
            new ConsoleRichiesteDisdettaElencoView();
    private final ConsoleRichiesteDisdettaDecisionView decisionView =
            new ConsoleRichiesteDisdettaDecisionView(in);

    private Integer selectedId;

    public CLIRichiesteDisdettaView(CLIGraphicControllerRichiesteDisdetta controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        renderHeader();

        /*
         * Se il payload non contiene ancora la lista richieste,
         * chiedo al graphic controller di caricarla.
         */
        if (!hasRichiestePayload()) {
            controller.caricaRichiestePending(sessione);
            System.out.println("(caricamento richieste...)");
            return;
        }

        List<String> richieste = readRichiesteFromPayload();
        elencoView.show(richieste);

        if (!richieste.isEmpty()) {
            selectedId = decisionView.askSelectedId();
        } else {
            selectedId = null;
        }

        dispatchMenu();
    }

    /**
     * Stampa intestazione e messaggi.
     */
    private void renderHeader() {
        System.out.println("\n=== HOME GESTORE > RICHIESTE DISDETTA ===");
        CliViewUtils.printMessages(getLastError(), getLastSuccess());
    }

    /**
     * Verifica se il payload contiene la chiave delle richieste.
     *
     * Questo mantiene la logica precedente:
     * - chiave assente: bisogna caricare;
     * - chiave presente ma non lista: viene trattata come lista vuota.
     */
    private boolean hasRichiestePayload() {
        return lastParams.containsKey(GraphicControllerUtils.KEY_RICHIESTE);
    }

    /**
     * Legge le richieste dal payload in modo sicuro.
     *
     * Se il valore non è una lista, restituisce lista vuota.
     * Questo risolve S1168: i metodi che restituiscono collezioni
     * non devono restituire null.
     */
    private List<String> readRichiesteFromPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_RICHIESTE);

        if (!(raw instanceof List<?> richiesteObj)) {
            return List.of();
        }

        return richiesteObj.stream()
                .map(Object::toString)
                .toList();
    }

    /**
     * Gestisce il menu delle decisioni.
     */
    private void dispatchMenu() {
        String scelta = decisionView.readMenuChoice();

        switch (scelta) {
            case "1" -> controller.caricaRichiestePending(sessione);
            case "2" -> handleDecision(true);
            case "3" -> handleDecision(false);
            case "0" -> controller.tornaAllaHome();
            default -> System.out.println("Scelta non valida");
        }
    }

    /**
     * Approva o rifiuta una richiesta.
     *
     * @param approva true per approvare, false per rifiutare
     */
    private void handleDecision(boolean approva) {
        Integer id = decisionView.readIdRichiesta(selectedId);

        if (id == null) {
            System.out.println("[ERRORE] Id richiesta non valido");
            return;
        }

        String nota = decisionView.readNota(
                approva ? "Nota gestore" : "Motivazione/nota gestore"
        );

        if (approva) {
            controller.approva(id, nota, sessione);
        } else {
            controller.rifiuta(id, nota, sessione);
        }
    }
}