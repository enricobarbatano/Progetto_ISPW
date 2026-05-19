package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerPenalita;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.interfaces.ViewGestionePenalita;

/**
 * View CLI per la gestione penalità.
 *
 * RESPONSABILITÀ:
 * - mostrare la lista utenti ricevuta dal graphic controller;
 * - leggere id utente, importo e motivazione da console;
 * - chiamare il graphic controller con parametri semplici.
 *
 * NON:
 * - crea bean;
 * - chiama logic controller;
 * - accede a DAO o persistenza;
 * - costruisce Map applicative per la logica.
 */
public class CLIPenalitaView extends GenericViewCLI
        implements ViewGestionePenalita, NavigableController {

    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerPenalita controller;

    private Integer lastId;

    public CLIPenalitaView(CLIGraphicControllerPenalita controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        renderUtentiIfPresent();
        printMenu();

        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> controller.richiediListaUtenti();
            case "2" -> handleApplicaPenalita();
            case "0" -> controller.tornaAllaHome();
            default -> System.out.println("Scelta non valida");
        }
    }

    /**
     * Mostra gli utenti se presenti nel payload.
     */
    private void renderUtentiIfPresent() {
        Object rawUtenti = lastParams.get(GraphicControllerUtils.KEY_UTENTI);

        if (!(rawUtenti instanceof List<?> utentiObj)) {
            return;
        }

        List<String> utenti = utentiObj.stream()
                .map(Object::toString)
                .toList();

        System.out.println("\n=== UTENTI ===");

        if (utenti.isEmpty()) {
            System.out.println("(nessun utente disponibile)");
            return;
        }

        for (String utente : utenti) {
            System.out.println(" - " + utente);
        }

        Integer selected = readOptionalPositiveInt("Id utente da selezionare (0 = annulla): ");

        if (selected != null && selected > 0) {
            lastId = selected;
        }
    }

    /**
     * Stampa il menu principale.
     */
    private void printMenu() {
        System.out.println("\n1) Lista utenti");
        System.out.println("2) Applica penalita");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
    }

    /**
     * Legge i dati della penalità e delega al controller grafico.
     */
    private void handleApplicaPenalita() {
        Integer id = readIdUtente();

        if (id == null) {
            System.out.println("[ERRORE] Id utente non valido");
            return;
        }

        Float importo = readPositiveFloat("Importo: ");

        if (importo == null) {
            System.out.println("[ERRORE] Importo non valido");
            return;
        }

        System.out.print("Motivazione: ");
        String motivazione = in.nextLine().trim();

        if (motivazione.isBlank()) {
            System.out.println("[ERRORE] Motivazione obbligatoria");
            return;
        }

        controller.applicaPenalita(id, importo, motivazione);
    }

    /**
     * Legge l'id utente, usando lastId come valore suggerito.
     */
    private Integer readIdUtente() {
        if (lastId != null && lastId > 0) {
            System.out.print("Id utente (invio per confermare " + lastId + "): ");
            String raw = in.nextLine().trim();

            if (raw.isBlank()) {
                return lastId;
            }

            return parsePositiveInt(raw);
        }

        return readRequiredPositiveInt("Id utente: ");
    }

    /**
     * Legge un intero positivo obbligatorio.
     */
    private Integer readRequiredPositiveInt(String prompt) {
        System.out.print(prompt);
        return parsePositiveInt(in.nextLine().trim());
    }

    /**
     * Legge un intero positivo opzionale.
     */
    private Integer readOptionalPositiveInt(String prompt) {
        System.out.print(prompt);
        String raw = in.nextLine().trim();

        if (raw.isBlank()) {
            return null;
        }

        Integer value = parseNonNegativeInt(raw);
        return value != null && value > 0 ? value : null;
    }

    /**
     * Legge un float positivo.
     *
     * Accetta sia punto sia virgola come separatore decimale.
     */
    private Float readPositiveFloat(String prompt) {
        System.out.print(prompt);
        String raw = in.nextLine().trim().replace(',', '.');

        try {
            float value = Float.parseFloat(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parsing intero positivo.
     */
    private Integer parsePositiveInt(String raw) {
        Integer value = parseNonNegativeInt(raw);
        return value != null && value > 0 ? value : null;
    }

    /**
     * Parsing intero non negativo.
     */
    private Integer parseNonNegativeInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            int value = Integer.parseInt(raw.trim());
            return value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
