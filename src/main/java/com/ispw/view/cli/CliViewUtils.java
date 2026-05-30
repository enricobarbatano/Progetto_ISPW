package com.ispw.view.cli;

import java.util.Scanner;

/**
 * Utility comuni per le view CLI.
 *
 * RESPONSABILITÀ:
 * - stampare messaggi standard;
 * - gestire piccoli prompt comuni;
 * - evitare duplicazione nelle view CLI.
 *
 * NON:
 * - chiama controller applicativi;
 * - crea bean;
 * - accede a DAO o persistenza.
 */
public final class CliViewUtils {

    private CliViewUtils() {
        // Utility class: non deve essere istanziata.
    }

    /**
     * Chiede all'utente se vuole tornare alla home.
     * Attende che i log asincroni si completino e sincronizza lo stdout.
     *
     * @param in scanner da cui leggere la risposta
     * @param goHome azione da eseguire se l'utente conferma
     */
    public static void askReturnHome(Scanner in, Runnable goHome) {
        if (in == null) {
            return;
        }

        // Attendi completamento log asincroni
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Sincronizza stdout con lock
        synchronized (System.out) {
            System.out.flush();
            System.out.print("Torna alla home? [s/N]: ");
            System.out.flush();
        }

        String ans = in.nextLine().trim();

        if ("s".equalsIgnoreCase(ans) && goHome != null) {
            goHome.run();
        }
    }

    /**
     * Stampa eventuali messaggi di errore e successo.
     *
     * @param error messaggio di errore
     * @param success messaggio di successo
     */
    public static void printMessages(String error, String success) {
        if (error != null && !error.isBlank()) {
            System.out.println("[ERRORE] " + error);
        }

        if (success != null && !success.isBlank()) {
            System.out.println(success);
        }
    }
}
