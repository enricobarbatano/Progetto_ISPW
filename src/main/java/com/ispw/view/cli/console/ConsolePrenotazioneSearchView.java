package com.ispw.view.cli.console;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console view per la ricerca disponibilità.
 *
 * RESPONSABILITÀ:
 * - stampare il form di ricerca;
 * - leggere input da console;
 * - mostrare campi e slot disponibili.
 *
 * NON:
 * - crea bean;
 * - chiama controller;
 * - contiene logica applicativa.
 */
public class ConsolePrenotazioneSearchView {

    private final Scanner in = new Scanner(System.in);

    public void renderSearchForm() {
        System.out.println("\n=== CERCA DISPONIBILITA' ===");
    }

    public int readCampoId() {
        return readPositiveInt("Id campo: ");
    }

    public String readData() {
        while (true) {
            System.out.print("Data (yyyy-MM-dd): ");
            String value = in.nextLine().trim();

            try {
                LocalDate.parse(value);
                return value;
            } catch (DateTimeParseException ex) {
                System.out.println("Formato data non valido. Usa yyyy-MM-dd, es. 2026-02-05.");
            }
        }
    }

    public String readOraInizio() {
        while (true) {
            System.out.print("Ora inizio (HH:mm): ");
            String value = in.nextLine().trim();

            try {
                LocalTime.parse(value);
                return value;
            } catch (DateTimeParseException ex) {
                System.out.println("Formato ora non valido. Usa HH:mm, es. 18:00.");
            }
        }
    }

    public int readDurataMin() {
        return readPositiveInt("Durata (min): ");
    }

    public void showCampi(List<String> campi) {
        if (campi == null || campi.isEmpty()) {
            System.out.println("Nessun campo disponibile.");
            return;
        }

        System.out.println("Campi disponibili:");
        for (String campo : campi) {
            System.out.println(" - " + campo);
        }
    }

    public void showResults(List<String> slots) {
        if (slots == null || slots.isEmpty()) {
            System.out.println("Nessuna disponibilita trovata.");
            return;
        }

        System.out.println("Slot disponibili:");
        for (int i = 0; i < slots.size(); i++) {
            System.out.println(String.format(" [%d] %s", i + 1, slots.get(i)));
        }
    }

    public int askSlotSelectionIndex(int max) {
        if (max <= 0) {
            return -1;
        }

        while (true) {
            System.out.print("Seleziona slot [1.." + max + "] oppure 0 per annullare: ");
            Integer value = parseIntOrNull(in.nextLine().trim());

            if (value == null) {
                System.out.println("Numero non valido.");
                continue;
            }

            if (value == 0) {
                return -1;
            }

            if (value >= 1 && value <= max) {
                return value - 1;
            }

            System.out.println("Selezione fuori intervallo.");
        }
    }

    public void showError(String msg) {
        if (msg != null && !msg.isBlank()) {
            System.out.println("[ERRORE] " + msg);
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            Integer value = parseIntOrNull(in.nextLine().trim());

            if (value != null && value > 0) {
                return value;
            }

            System.out.println("Inserisci un numero positivo.");
        }
    }

    private Integer parseIntOrNull(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}