// src/main/java/com/ispw/view/cli/ConsolePrenotazioneSearchView.java
package com.ispw.view.cli.console;

import java.util.Scanner;
public class ConsolePrenotazioneSearchView {
    private final Scanner in = new Scanner(System.in);
    public void renderSearchForm() { System.out.println("\n=== CERCA DISPONIBILITA' ==="); }
    public int readCampoId()   { System.out.print("Id campo: "); return Integer.parseInt(in.nextLine()); }
    public String  readData()      { System.out.print("Data (yyyy-MM-dd): "); return in.nextLine(); }
    public String  readOraInizio() { System.out.print("Ora inizio (HH:mm): "); return in.nextLine(); }
    public int readDurataMin() { System.out.print("Durata (min): "); return Integer.parseInt(in.nextLine()); }
    public void showResults(java.util.List<String> slots) {
        if (slots.isEmpty()) { System.out.println("Nessuna disponibilità trovata."); return; }
        System.out.println("Slot disponibili:");
        for (int i = 0; i < slots.size(); i++) System.out.printf(" [%d] %s%n", i+1, slots.get(i));
    }
    public int askSlotSelectionIndex(int max) {
        if (max <= 0) return -1;
        System.out.print("Seleziona slot [1.." + max + "] oppure 0 per annullare: ");
        int sel = Integer.parseInt(in.nextLine());
        return (sel >= 1 && sel <= max) ? sel - 1 : -1;
    }
    public void showError(String msg) { System.out.println("[ERRORE] " + msg); }
}