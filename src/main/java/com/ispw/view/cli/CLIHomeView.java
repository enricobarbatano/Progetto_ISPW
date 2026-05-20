package com.ispw.view.cli;
import java.util.Map;
import java.util.Scanner;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewHomeProfilo;

/**
 * View CLI della home.
 *
 * RESPONSABILITÀ:
 * - mostrare il menu principale;
 * - mostrare opzioni diverse in base al ruolo;
 * - leggere la scelta dell'utente;
 * - delegare la navigazione al navigator grafico.
 *
 * NON:
 * - crea bean;
 * - chiama logic controller;
 * - accede a DAO o persistenza;
 * - contiene logica applicativa dei casi d'uso.
 */
public class CLIHomeView extends GenericViewCLI implements ViewHomeProfilo, NavigableController {

    private final Scanner in = new Scanner(System.in);
    private final GraphicControllerNavigation navigator;

    public CLIHomeView(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_HOME;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        Ruolo ruolo = getRuoloCorrente();

        printHeader(ruolo);
        printMenu(ruolo);

        String scelta = in.nextLine().trim();
        handleScelta(scelta, ruolo);
    }

    /**
     * Estrae il ruolo dalla sessione corrente.
     */
    private Ruolo getRuoloCorrente() {
        SessioneUtenteBean s = sessione;

        return s != null && s.getUtente() != null
                ? s.getUtente().getRuolo()
                : null;
    }

    /**
     * Stampa intestazione della home e ruolo corrente.
     */
    private void printHeader(Ruolo ruolo) {
        System.out.println("\n=== HOME ===");

        if (ruolo != null) {
            System.out.println("Ruolo: " + ruolo);
        } else {
            System.out.println("(ruolo non disponibile)");
        }
    }

    /**
     * Stampa il menu comune e quello specifico per ruolo.
     */
    private void printMenu(Ruolo ruolo) {
        System.out.println("1) Account");

        if (ruolo == Ruolo.UTENTE) {
            printMenuUtente();
        } else if (ruolo == Ruolo.GESTORE) {
            printMenuGestore();
        }

        System.out.println("0) Logout");
        System.out.print("Scelta: ");
    }

    /**
     * Stampa le opzioni disponibili per utente finale.
     */
    private void printMenuUtente() {
        System.out.println("2) Prenotazione");
        System.out.println("3) Disdetta (richiesta)");
    }

    /**
     * Stampa le opzioni disponibili per gestore.
     */
    private void printMenuGestore() {
        System.out.println("2) Regole");
        System.out.println("3) Penalita");
        System.out.println("4) Log");
        System.out.println("5) Richieste disdetta");
    }

    /**
     * Gestisce la scelta inserita dall'utente.
     *
     * La logica è la stessa della versione iniziale,
     * ma viene divisa in metodi più piccoli per ridurre la complessità cognitiva.
     */
    private void handleScelta(String scelta, Ruolo ruolo) {
        switch (scelta) {
            case "1" -> goTo(GraphicControllerUtils.ROUTE_ACCOUNT);
            case "2" -> handleOpzioneDue(ruolo);
            case "3" -> handleOpzioneTre(ruolo);
            case "4" -> handleSoloGestore(ruolo, GraphicControllerUtils.ROUTE_LOGS);
            case "5" -> handleSoloGestore(ruolo, GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA);
            case "0" -> goTo(GraphicControllerUtils.ROUTE_LOGIN);
            default -> sceltaNonValida();
        }
    }

    /**
     * Gestisce l'opzione 2:
     * - gestore: regole;
     * - utente: prenotazione.
     */
    private void handleOpzioneDue(Ruolo ruolo) {
        if (ruolo == Ruolo.GESTORE) {
            goTo(GraphicControllerUtils.ROUTE_REGOLE);
            return;
        }

        if (ruolo == Ruolo.UTENTE) {
            goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE);
            return;
        }

        sceltaNonValida();
    }

    /**
     * Gestisce l'opzione 3:
     * - gestore: penalità;
     * - utente: disdetta.
     */
    private void handleOpzioneTre(Ruolo ruolo) {
        if (ruolo == Ruolo.GESTORE) {
            goTo(GraphicControllerUtils.ROUTE_PENALITA);
            return;
        }

        if (ruolo == Ruolo.UTENTE) {
            goTo(GraphicControllerUtils.ROUTE_DISDETTA);
            return;
        }

        sceltaNonValida();
    }

    /**
     * Gestisce le opzioni disponibili solo per il gestore.
     */
    private void handleSoloGestore(Ruolo ruolo, String route) {
        if (ruolo == Ruolo.GESTORE) {
            goTo(route);
            return;
        }

        sceltaNonValida();
    }

    /**
     * Stampa messaggio di scelta non valida.
     */
    private void sceltaNonValida() {
        System.out.println("Scelta non valida");
    }

    /**
     * Naviga verso una route.
     *
     * Se la sessione è disponibile, viene passata nei parametri.
     */
    private void goTo(String route) {
        if (navigator == null) {
            return;
        }

        if (sessione != null) {
            navigator.goTo(route, Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
        } else {
            navigator.goTo(route);
        }
    }
}
