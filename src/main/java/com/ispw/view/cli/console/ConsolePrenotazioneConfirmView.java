package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsolePrenotazioneConfirmView {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view conferma prenotazione.
    // A2) IO: input/output su standard input/output.
    // ========================

    private final Scanner in = new Scanner(System.in);

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) renderSummary: mostra riepilogo.
    // L2) askConfirmation: conferma utente.
    // L3) showInfo/showError: messaggi.
    // ========================
    public void renderSummary(String riepilogo) {
        System.out.println("\n=== RIEPILOGO PRENOTAZIONE ===");
        System.out.println(riepilogo);
    }
    public boolean askConfirmation() {
        System.out.print("Confermare? [s/N]: ");
        return "s".equalsIgnoreCase(in.nextLine().trim());
    }
    public void showInfo(String msg)  { System.out.println(msg); }
    public void showError(String msg) { System.err.println("[ERRORE] " + msg); }
} 