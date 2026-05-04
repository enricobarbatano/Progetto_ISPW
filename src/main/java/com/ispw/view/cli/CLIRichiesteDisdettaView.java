package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.cli.console.ConsoleRichiesteDisdettaDecisionView;
import com.ispw.view.cli.console.ConsoleRichiesteDisdettaElencoView;

public class CLIRichiesteDisdettaView extends GenericViewCLI implements NavigableController {

    private final CLIGraphicControllerRichiesteDisdetta controller;

    private final Scanner in = new Scanner(System.in);
    private final ConsoleRichiesteDisdettaElencoView elencoView = new ConsoleRichiesteDisdettaElencoView();
    private final ConsoleRichiesteDisdettaDecisionView decisionView = new ConsoleRichiesteDisdettaDecisionView(in);

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

        // se non ho la lista, chiedo al controller e termino (round-trip)
        List<String> richieste = readRichiesteFromPayload();
        if (richieste == null) {
            controller.caricaRichiestePending(sessione);
            System.out.println("(caricamento richieste...)");
            return;
        }

        // render elenco
        elencoView.show(richieste);

        // se c’è qualcosa, posso chiedere un id “suggerito”
        if (richieste != null && !richieste.isEmpty()) {
            selectedId = decisionView.askSelectedId();
        } else {
            selectedId = null;
        }

        // menu decisione
        dispatchMenu();
    }

    private void renderHeader() {
        System.out.println("\n=== HOME GESTORE > RICHIESTE DISDETTA ===");
        CliViewUtils.printMessages(getLastError(), getLastSuccess());
    }

    @SuppressWarnings("unchecked")
    private List<String> readRichiesteFromPayload() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_RICHIESTE);
        if (!(raw instanceof List<?>)) return null;
        return (List<String>) raw;
    }

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

    private void handleDecision(boolean approva) {
        Integer id = decisionView.readIdRichiesta(selectedId);
        if (id == null) {
            System.out.println("[ERRORE] Id richiesta non valido");
            return;
        }
        String nota = decisionView.readNota(approva ? "Nota gestore" : "Motivazione/nota gestore");

        if (approva) controller.approva(id, nota, sessione);
        else controller.rifiuta(id, nota, sessione);
    }
}