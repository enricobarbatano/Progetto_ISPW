package com.ispw.view.cli.console;

import java.util.Scanner;

/**
 * Console view per la decisione sulle richieste di disdetta.
 *
 * Si occupa solo di input/output su console.
 */
public class ConsoleRichiesteDisdettaDecisionView {

    private final Scanner in;

    public ConsoleRichiesteDisdettaDecisionView(Scanner in) {
        this.in = in;
    }

    /**
     * Chiede un id richiesta da gestire.
     */
    public Integer askSelectedId() {
        System.out.print("Id richiesta da gestire (0 = nessuna): ");
        String raw = in.nextLine().trim();
        Integer id = parsePositiveInt(raw);
        return id != null && id > 0 ? id : null;
    }

    /**
     * Legge la scelta del menu.
     */
    public String readMenuChoice() {
        System.out.println("\n1) Ricarica richieste");
        System.out.println("2) Approva richiesta");
        System.out.println("3) Rifiuta richiesta");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
        return in.nextLine().trim();
    }

    /**
     * Legge l'id richiesta, eventualmente confermando quello suggerito.
     */
    public Integer readIdRichiesta(Integer suggested) {
        if (suggested != null && suggested > 0) {
            System.out.print("Id richiesta (invio per confermare " + suggested + "): ");
            String raw = in.nextLine().trim();

            if (raw.isBlank()) {
                return suggested;
            }

            return parsePositiveInt(raw);
        }

        System.out.print("Id richiesta: ");
        return parsePositiveInt(in.nextLine().trim());
    }

    /**
     * Legge una nota opzionale.
     */
    public String readNota(String label) {
        System.out.print(label + " (opzionale): ");
        return in.nextLine();
    }

    /**
     * Parsing sicuro di un intero positivo.
     */
    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}