package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsoleRichiesteDisdettaDecisionView {

    private final Scanner in;

    public ConsoleRichiesteDisdettaDecisionView(Scanner in) {
        this.in = in;
    }

    public Integer askSelectedId() {
        System.out.print("Id richiesta da gestire (0 = nessuna): ");
        String raw = in.nextLine().trim();
        Integer id = parseIntOrNull(raw);
        return (id != null && id > 0) ? id : null;
    }

    public String readMenuChoice() {
        System.out.println("\n1) Ricarica richieste");
        System.out.println("2) Approva richiesta");
        System.out.println("3) Rifiuta richiesta");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
        return in.nextLine().trim();
    }

    public Integer readIdRichiesta(Integer suggested) {
        if (suggested != null) {
            System.out.print("Id richiesta (invio per confermare " + suggested + "): ");
            String raw = in.nextLine().trim();
            if (raw.isBlank()) return suggested;
            return parseIntOrNull(raw);
        }
        System.out.print("Id richiesta: ");
        return parseIntOrNull(in.nextLine().trim());
    }

    public String readNota(String label) {
        System.out.print(label + " (opzionale): ");
        return in.nextLine();
    }

    private Integer parseIntOrNull(String raw) {
        try {
            int v = Integer.parseInt(raw);
            return v > 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }
}