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

/**
 * View CLI per la richiesta di disdetta lato utente.
 *
 * Responsabilità:
 * - mostrare le prenotazioni cancellabili;
 * - mostrare l'anteprima della disdetta;
 * - mostrare l'esito della richiesta;
 * - raccogliere input da console;
 * - delegare le azioni al graphic controller.
 *
 * Non contiene:
 * - logica applicativa;
 * - chiamate a logic controller;
 * - accesso a DAO o persistenza;
 * - creazione di bean.
 */
public class CLIDisdettaView extends GenericViewCLI
        implements ViewDisdettaPrenotazione, NavigableController {

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

        boolean hasSuccess = lastParams.containsKey(GraphicControllerUtils.KEY_SUCCESSO);
        boolean hasAnteprima = lastParams.containsKey(GraphicControllerUtils.KEY_ANTEPRIMA);
        boolean hasElenco = lastParams.containsKey(GraphicControllerUtils.KEY_PRENOTAZIONI);
        boolean hasError = lastParams.containsKey(GraphicControllerUtils.KEY_ERROR);

        if (!hasSuccess && !hasAnteprima && !hasElenco && !hasError) {
            controller.richiediPrenotazioniCancellabili(sessione);
            return;
        }

        renderHeader();

        if (handleSuccess()) {
            return;
        }

        if (handleAnteprima()) {
            return;
        }

        /*
         * S3626:
         * handleElenco() è l'ultima istruzione del metodo.
         * Non serve un return dopo questa chiamata.
         */
        handleElenco();
    }

    /**
     * Stampa intestazione e messaggi standard.
     */
    private void renderHeader() {
        System.out.println("\n=== DISDETTA (RICHIESTA) ===");
        CliViewUtils.printMessages(getLastError(), getLastSuccess());
    }

    /**
     * Gestisce l'elenco delle prenotazioni cancellabili.
     *
     * @return true se il payload contiene un elenco gestibile, false altrimenti
     */
    private boolean handleElenco() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PRENOTAZIONI);

        if (!(raw instanceof List<?> elencoObj)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        List<String> lista = (List<String>) elencoObj;

        elencoView.show(lista);

        /*
         * S2589:
         * lista non può essere null in questo punto.
         * Resta solo il controllo sulla lista vuota.
         */
        if (lista.isEmpty()) {
            CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
            return true;
        }

        System.out.print("Inserisci ID prenotazione, es. 30 oppure 0 = home: ");
        Integer id = readPositiveIntOrZero();

        if (id == null) {
            System.out.println("[ERRORE] Id non valido");
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

    /**
     * Gestisce il payload di anteprima disdetta.
     *
     * @return true se il payload contiene un'anteprima gestibile, false altrimenti
     */
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

        /*
         * UC complesso:
         * qui non si annulla direttamente la prenotazione,
         * ma si invia una richiesta di disdetta.
         */
        System.out.print("Inviare richiesta di disdetta? [s/N]: ");
        String ans = in.nextLine().trim();

        if ("s".equalsIgnoreCase(ans) && selectedId != null) {
            controller.confermaDisdetta(selectedId, sessione);
        } else {
            controller.richiediPrenotazioniCancellabili(sessione);
        }

        return true;
    }

    /**
     * Gestisce il payload di successo.
     *
     * @return true se il payload contiene un esito di successo, false altrimenti
     */
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
     * - ritorna 0 se l'utente inserisce 0;
     * - ritorna > 0 se valido;
     * - ritorna null se input non numerico.
     */
    private Integer readPositiveIntOrZero() {
        String s = in.nextLine().trim();

        if (s.isEmpty()) {
            return null;
        }

        try {
            int v = Integer.parseInt(s);
            return v >= 0 ? v : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

