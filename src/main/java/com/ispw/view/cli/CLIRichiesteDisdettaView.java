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

        if (!hasRichiestePayload()) {
            controller.caricaRichiestePending(sessione);
            System.out.println("(caricamento richieste...)");
            return;
        }

        List<String> richieste = readRichiesteFromPayload();

        elencoView.show(richieste);

        if (!richieste.isEmpty()) {
            Integer selectedInput = decisionView.askSelectedId();
            selectedId = resolveRichiestaIdFromSelection(selectedInput, richieste);
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
     * Se la chiave non è presente, la view chiede il caricamento
     * delle richieste pending al graphic controller.
     */
    private boolean hasRichiestePayload() {
        return lastParams.containsKey(GraphicControllerUtils.KEY_RICHIESTE);
    }

    /**
     * Legge le richieste dal payload in modo sicuro.
     *
     * Se il payload non contiene una lista, restituisce lista vuota.
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
     * Traduce la selezione dell'utente nell'id reale della richiesta.
     *
     * Caso tipico:
     * - la lista mostra: [1] Richiesta#2 ...
     * - l'utente inserisce: 1
     * - il metodo restituisce: 2
     *
     * Se invece l'utente inserisce direttamente un id non interpretabile
     * come indice di lista, viene lasciato invariato.
     */
    private Integer resolveRichiestaIdFromSelection(Integer selectedInput, List<String> richieste) {
        if (selectedInput == null || selectedInput <= 0) {
            return null;
        }

        if (selectedInput <= richieste.size()) {
            Integer parsedId = parseRichiestaId(richieste.get(selectedInput - 1));

            if (parsedId != null) {
                return parsedId;
            }
        }

        return selectedInput;
    }

    /**
     * Estrae l'id reale da una stringa del tipo:
     *
     * Richiesta#2 pren#1 utente#2 ...
     *
     * Non usa regex per evitare ulteriori warning Sonar sui pattern.
     */
    private Integer parseRichiestaId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        int markerIndex = raw.indexOf("Richiesta#");

        if (markerIndex < 0) {
            return null;
        }

        int start = markerIndex + "Richiesta#".length();
        StringBuilder digits = new StringBuilder();

        for (int i = start; i < raw.length(); i++) {
            char current = raw.charAt(i);

            if (!Character.isDigit(current)) {
                break;
            }

            digits.append(current);
        }

        if (digits.isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(digits.toString());
        } catch (NumberFormatException e) {
            return null;
        }
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
