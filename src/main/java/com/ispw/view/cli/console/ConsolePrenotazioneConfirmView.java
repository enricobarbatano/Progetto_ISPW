// src/main/java/com/ispw/view/cli/ConsolePrenotazioneConfirmView.java
package com.ispw.view.cli.console;

import java.util.Scanner;
import java.util.logging.Logger;

public class ConsolePrenotazioneConfirmView {
    private static final Logger logger = Logger.getLogger(ConsolePrenotazioneConfirmView.class.getName());
    private final Scanner in = new Scanner(System.in);
    public void renderSummary(String riepilogo) {
        logger.info("\n=== RIEPILOGO PRENOTAZIONE ===");
        logger.info(riepilogo);
    }
    public boolean askConfirmation() {
        System.out.print("Confermare? [s/N]: ");
        return "s".equalsIgnoreCase(in.nextLine().trim());
    }
    public void showInfo(String msg)  { logger.info(msg); }
    public void showError(String msg) { logger.severe("[ERRORE] " + msg); }
} 