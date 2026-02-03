// src/main/java/com/ispw/view/cli/ConsolePrenotazioneConfirmView.java
package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsolePrenotazioneConfirmView {
    private final Scanner in = new Scanner(System.in);
    public void renderSummary(String riepilogo) {
        System.out.println("\n=== RIEPILOGO PRENOTAZIONE ===");
        System.out.println(riepilogo);
    }
    public boolean askConfirmation() {
        System.out.print("Confermare? [s/N]: ");
        return "s".equalsIgnoreCase(in.nextLine().trim());
    }
    public void showInfo(String msg)  { System.out.println(msg); }
    public void showError(String msg) { System.out.println("[ERRORE] " + msg); }
}