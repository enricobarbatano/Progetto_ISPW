package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsoleLoginView {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view login.
    // A2) IO: input/output su standard input/output.
    // ========================

    private final Scanner in = new Scanner(System.in);

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) render: stampa menu.
    // L2) readChoice/readEmail/readPassword: input utente.
    // L3) showError/showWelcome: messaggi.
    // ========================
    public void render() {
        System.out.println("\n=== LOGIN ===");
        System.out.println("1) Login");
        System.out.println("2) Registrazione");
    }

    public String readChoice() {
        System.out.print("Scelta: ");
        return in.nextLine().trim();
    }
    public String readEmail() { System.out.print("Email: "); return in.nextLine(); }
    public String readPassword() { System.out.print("Password: "); return in.nextLine(); }
    public void showError(String msg) { System.out.println("[ERRORE] " + msg); }
    public void showWelcome(String name) { System.out.println("Benvenuto, " + name + "!"); }
}