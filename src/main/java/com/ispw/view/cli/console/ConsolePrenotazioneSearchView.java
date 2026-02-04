// src/main/java/com/ispw/view/cli/ConsolePrenotazioneSearchView.java
package com.ispw.view.cli.console;

import java.util.Scanner;
import java.util.logging.Logger;

public class ConsolePrenotazioneSearchView {
    private static final Logger logger = Logger.getLogger(ConsolePrenotazioneSearchView.class.getName());
    private final Scanner in = new Scanner(System.in);
    public void renderSearchForm() { logger.info("\n=== CERCA DISPONIBILITA' ==="); }
    public int readCampoId()   { System.out.print("Id campo: "); return Integer.parseInt(in.nextLine()); }
    public String  readData()      { System.out.print("Data (yyyy-MM-dd): "); return in.nextLine(); }
    public String  readOraInizio() { System.out.print("Ora inizio (HH:mm): "); return in.nextLine(); }
    public int readDurataMin() { System.out.print("Durata (min): "); return Integer.parseInt(in.nextLine()); }
    public void showResults(java.util.List<String> slots) {
        if (slots.isEmpty()) { logger.info("Nessuna disponibilità trovata."); return; }
        logger.info("Slot disponibili:");
        for (int i = 0; i < slots.size(); i++) logger.info(String.format(" [%d] %s", i+1, slots.get(i)));
    }
    public int askSlotSelectionIndex(int max) {
        if (max <= 0) return -1;
        System.out.print("Seleziona slot [1.." + max + "] oppure 0 per annullare: ");
        int sel = Integer.parseInt(in.nextLine());
        return (sel >= 1 && sel <= max) ? sel - 1 : -1;
    }
    public void showError(String msg) { logger.severe("[ERRORE] " + msg); }
} 