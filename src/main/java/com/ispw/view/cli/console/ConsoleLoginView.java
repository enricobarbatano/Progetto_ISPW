package com.ispw.view.cli.console;

import java.util.Scanner;

/**
 * Console view per il login.
 *
 * Si occupa solo di input/output su console.
 */
public class ConsoleLoginView {

    private final Scanner in = new Scanner(System.in);

    /**
     * Stampa il menu di login.
     */
    public void render() {
        System.out.println("\n=== LOGIN ===");
        System.out.println("1) Login");
        System.out.println("2) Registrazione");
        System.out.println("0) Esci");
    }

    /**
     * Legge la scelta dell'utente.
     */
    public String readChoice() {
        System.out.print("Scelta: ");
        return in.nextLine().trim();
    }

    /**
     * Legge l'email.
     */
    public String readEmail() {
        System.out.print("Email: ");
        return in.nextLine().trim();
    }

    /**
     * Legge la password.
     */
    public String readPassword() {
        System.out.print("Password: ");
        return in.nextLine();
    }

    /**
     * Mostra un errore solo se il messaggio è presente.
     */
    public void showError(String msg) {
        if (msg != null && !msg.isBlank()) {
            System.out.println("[ERRORE] " + msg);
        }
    }

    /**
     * Mostra un messaggio informativo.
     */
    public void showInfo(String msg) {
        if (msg != null && !msg.isBlank()) {
            System.out.println(msg);
        }
    }

    /**
     * Mostra un messaggio di benvenuto.
     */
    public void showWelcome(String name) {
        if (name != null && !name.isBlank()) {
            System.out.println("Benvenuto, " + name + "!");
        }
    }
}