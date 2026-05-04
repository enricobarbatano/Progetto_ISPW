package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.cli.console.ConsoleDisdettaAnteprimaView;
import com.ispw.view.cli.console.ConsoleDisdettaElencoView;
import com.ispw.view.cli.console.ConsoleDisdettaEsitoView;
import com.ispw.view.interfaces.ViewDisdettaPrenotazione;

public class CLIDisdettaView extends GenericViewCLI implements ViewDisdettaPrenotazione, NavigableController {

    private final CLIGraphicControllerDisdetta controller;

    private final ConsoleDisdettaElencoView elencoView = new ConsoleDisdettaElencoView();
    private final ConsoleDisdettaAnteprimaView anteprimaView = new ConsoleDisdettaAnteprimaView();
    private final ConsoleDisdettaEsitoView esitoView = new ConsoleDisdettaEsitoView();

    private final Scanner in = new Scanner(System.in);

    private Integer selectedId;

    public CLIDisdettaView(CLIGraphicControllerDisdetta controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        renderHeader();

        // 1) se ho successo, mostro esito e propongo home
        if (handleSuccess()) return;

        // 2) se ho anteprima, gestisco conferma (invio richiesta)
        if (handleAnteprima()) return;

        // 3) se ho elenco, gestisco selezione prenotazione
        if (handleElenco()) return;

        // 4) altrimenti richiedo elenco prenotazioni cancellabili
        controller.richiediPrenotazioniCancellabili(sessione);
    }

    private void renderHeader() {
        // Intestazione + messaggi standard
        System.out.println("\n=== DISDETTA (RICHIESTA) ===");
        CliViewUtils.printMessages(getLastError(), getLastSuccess());
    }

    private boolean handleElenco() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PRENOTAZIONI);
        if (!(raw instanceof List<?> elencoObj)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        List<String> lista = (List<String>) elencoObj;

        elencoView.show(lista);

        // Se vuoto: torna alla home (o chiedi conferma)
        if (lista == null || lista.isEmpty()) {
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        System.out.print("Id prenotazione da disdire (0 = home): ");
        Integer id = readPositiveIntOrZero();

        if (id == null) {
            System.out.println("[ERRORE] Id non valido");
            // ricarica elenco (round-trip)
            controller.richiediPrenotazioniCancellabili(sessione);
            return true;
        }

        if (id == 0) {
            controller.tornaAllaHome();
            return true;
        }

        selectedId = id;
        controller.richiediAnteprimaDisdetta(id, sessione);
        return true;
    }

    private boolean handleAnteprima() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_ANTEPRIMA);
        if (!(raw instanceof Map<?, ?> anteprima)) {
            return false;
        }

        Object possibile = anteprima.get(GraphicControllerUtils.KEY_POSSIBILE);
        Object penale = anteprima.get(GraphicControllerUtils.KEY_PENALE);

        boolean poss = possibile instanceof Boolean b && b;
        float pen = penale instanceof Number n ? n.floatValue() : 0f;

        anteprimaView.show(poss, pen);

        if (!poss) {
            System.out.println("Disdetta non consentita.");
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        // ✅ UC complesso: qui NON annulliamo subito, inviamo richiesta
        System.out.print("Inviare richiesta di disdetta? [s/N]: ");
        String ans = in.nextLine().trim();

        if ("s".equalsIgnoreCase(ans) && selectedId != null) {
            controller.confermaDisdetta(selectedId, sessione);
        } else {
            // se non conferma: torna elenco (o home). Scelgo elenco per UX.
            controller.richiediPrenotazioniCancellabili(sessione);
        }
        return true;
    }

    private boolean handleSuccess() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_SUCCESSO);
        if (raw == null) {
            return false;
        }

        esitoView.showMessage(String.valueOf(raw));
        CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
        return true;
    }

    /**
     * Legge un intero da console:
     * - ritorna 0 se l'utente inserisce 0
     * - ritorna >0 se valido
     * - ritorna null se input non numerico
     */
    private Integer readPositiveIntOrZero() {
        String s = in.nextLine().trim();
        if (s.isEmpty()) return null;
        try {
            int v = Integer.parseInt(s);
            return (v >= 0) ? v : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}