package com.ispw.bootstrap;

import java.util.Scanner;

public final class ConsoleMenu {

    // Scanner su System.in: non chiuderlo (evita di chiudere lo stream globale della JVM).
    private final Scanner sc = new Scanner(System.in);

    /**
     * Mostra un semplice menu testuale e chiede una scelta numerica tra 1..options.length.
     * Usa System.out per evitare log confusionari in una CLI interattiva.
     */
    public int askOption(String title, String... options) {
        while (true) {
            // Intestazione e opzioni
            System.out.println("\n=== " + title + " ===");
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ") " + options[i]);
            }

            // Prompt
            System.out.print("Scelta: ");
            final String line = sc.nextLine().trim();

            try {
                final int choice = Integer.parseInt(line);
                if (choice >= 1 && choice <= options.length) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // no-op: gestito sotto con warning
            }

            System.out.println("Input non valido. Riprova.");
        }
    }
}