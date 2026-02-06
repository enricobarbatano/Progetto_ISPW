package com.ispw.view.cli.console;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsolePrenotazioneSearchView {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view ricerca disponibilita'.
    // A2) IO: input/output su standard input/output.
    // ========================

    private final Scanner in = new Scanner(System.in);

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) renderSearchForm/showCampi/showResults: output.
    // L2) readCampoId/readData/readOraInizio/readDurataMin: input.
    // L3) askSlotSelectionIndex: selezione slot.
    // ========================
    public void renderSearchForm() { System.out.println("\n=== CERCA DISPONIBILITA' ==="); }
    public int readCampoId()   { System.out.print("Id campo: "); return Integer.parseInt(in.nextLine()); }
    public String  readData()      {
        while (true) {
            System.out.print("Data (yyyy-MM-dd): ");
            String value = in.nextLine().trim();
            try {
                LocalDate.parse(value); // ISO yyyy-MM-dd
                return value;
            } catch (DateTimeParseException ex) {
                System.out.println("Formato data non valido. Usa yyyy-MM-dd (es. 2026-02-05)");
            }
        }
    }
    public String  readOraInizio() { System.out.print("Ora inizio (HH:mm): "); return in.nextLine(); }
    public int readDurataMin() { System.out.print("Durata (min): "); return Integer.parseInt(in.nextLine()); }
    public void showCampi(List<String> campi) {
        if (campi == null || campi.isEmpty()) {
            System.out.println("Nessun campo disponibile.");
            return;
        }
        System.out.println("Campi disponibili:");
        for (String c : campi) {
            System.out.println(" - " + c);
        }
    }
    public void showResults(List<String> slots) {
        if (slots.isEmpty()) { System.out.println("Nessuna disponibilità trovata."); return; }
        System.out.println("Slot disponibili:");
        for (int i = 0; i < slots.size(); i++) System.out.println(String.format(" [%d] %s", i+1, slots.get(i)));
    }
    public int askSlotSelectionIndex(int max) {
        if (max <= 0) return -1;
        System.out.print("Seleziona slot [1.." + max + "] oppure 0 per annullare: ");
        int sel = Integer.parseInt(in.nextLine());
        return (sel >= 1 && sel <= max) ? sel - 1 : -1;
    }
    public void showError(String msg) { System.out.println("[ERRORE] " + msg); }
} 