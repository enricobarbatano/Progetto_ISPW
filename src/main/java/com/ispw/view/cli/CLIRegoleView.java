package com.ispw.view.cli;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.interfaces.ViewGestioneRegole;

/**
 * View CLI per la gestione delle regole.
 *
 * RESPONSABILITÀ:
 * - mostra lista campi
 * - raccoglie input utente
 * - chiama il GraphicController con parametri semplici
 *
 * IMPORTANTE:
 * NON crea Bean
 * NON usa Map per input
 * ✅ passa solo valori primitivi
 */
public class CLIRegoleView extends GenericViewCLI
        implements ViewGestioneRegole, NavigableController {

    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerRegole controller;

    private Integer selectedCampoId;
    private List<String> lastCampi;

    public CLIRegoleView(CLIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        showCampiIfPresent();
        showSelectedCampo();

        printMenu();

        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> controller.richiediListaCampi();
            case "2" -> handleSelezionaCampo();
            case "3" -> handleAggiornaCampo();
            case "4" -> handleAggiornaTempistiche();
            case "5" -> handleAggiornaPenalita();
            case "0" -> controller.tornaAllaHome();
            default -> System.out.println("Scelta non valida");
        }
    }

    // =========================================================
    // VISUALIZZAZIONE
    // =========================================================

    private void showCampiIfPresent() {

        Object raw = lastParams.get(GraphicControllerUtils.KEY_CAMPI);

        if (!(raw instanceof List<?> campiObj)) {
            controller.richiediListaCampi();
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> campi = (List<String>) campiObj;

        lastCampi = campi;

        System.out.println("\n=== CAMPI ===");

        int i = 1;
        for (String c : campi) {
            System.out.println("[" + i++ + "] " + c);
        }
    }

    private void showSelectedCampo() {

        Object rawId = lastParams.get(GraphicControllerUtils.KEY_ID_CAMPO);

        if (rawId instanceof Integer id) {
            selectedCampoId = id;
            System.out.println("Campo selezionato: " + id);
        }
    }

    private void printMenu() {

        System.out.println("\n1) Lista campi");
        System.out.println("2) Seleziona campo");
        System.out.println("3) Aggiorna stato campo");
        System.out.println("4) Aggiorna tempistiche");
        System.out.println("5) Aggiorna penalita");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
    }

    // =========================================================
    // HANDLER INPUT
    // =========================================================

    private void handleSelezionaCampo() {

        System.out.print("Id campo: ");
        int id = Integer.parseInt(in.nextLine());

        controller.selezionaCampo(id);
    }

    private void handleAggiornaCampo() {

        int id = (selectedCampoId != null) ? selectedCampoId : readIdCampo();

        System.out.print("Attivo? (true/false): ");
        boolean attivo = Boolean.parseBoolean(in.nextLine());

        System.out.print("Manutenzione? (true/false): ");
        boolean manut = Boolean.parseBoolean(in.nextLine());

        // ✅ NO Map → chiamata diretta
        controller.aggiornaStatoCampo(id, attivo, manut);
    }

    private void handleAggiornaTempistiche() {

        try {
            System.out.print("Durata slot (min): ");
            int durata = Integer.parseInt(in.nextLine());

            System.out.print("Ora apertura (HH:mm): ");
            LocalTime apertura = LocalTime.parse(in.nextLine());

            System.out.print("Ora chiusura (HH:mm): ");
            LocalTime chiusura = LocalTime.parse(in.nextLine());

            System.out.print("Preavviso minimo (min): ");
            int preavviso = Integer.parseInt(in.nextLine());

            // ✅ parametri semplici
            controller.aggiornaTempistiche(
                    preavviso,
                    durata,
                    apertura,
                    chiusura
            );

        } catch (RuntimeException e) {
            System.err.println("Dati non validi");
        }
    }

    private void handleAggiornaPenalita() {

        try {
            System.out.print("Valore penalita: ");
            BigDecimal valore = new BigDecimal(in.nextLine());

            System.out.print("Preavviso minimo (min): ");
            int preavviso = Integer.parseInt(in.nextLine());

            // ✅ niente Map
            controller.aggiornaPenalita(preavviso, valore);

        } catch (RuntimeException e) {
            System.err.println("Dati non validi");
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private int readIdCampo() {
        System.out.print("Id campo: ");
        return Integer.parseInt(in.nextLine());
    }
}