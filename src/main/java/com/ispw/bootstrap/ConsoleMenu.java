package com.ispw.bootstrap;

import java.util.Scanner;
import java.util.logging.Logger;

public final class ConsoleMenu {

    // Scanner su System.in: non chiuderlo (evita di chiudere lo stream globale della JVM).
    private final Scanner sc = new Scanner(System.in);

    @SuppressWarnings("java:S1312") // Logger con nome classe: convenzione del progetto
    private static final Logger LOGGER = Logger.getLogger(ConsoleMenu.class.getName());

    /**
     * Mostra un semplice menu testuale e chiede una scelta numerica tra 1..options.length.
     * I messaggi passano dal logger per conformità a Sonar (S106).
     */
    public int askOption(String title, String... options) {
        while (true) {
            // Intestazione e opzioni
            LOGGER.info(() -> "\n=== " + title + " ===");
            for (int i = 0; i < options.length; i++) {
                final int idx = i + 1;          // effectively final
                final String opt = options[i];  // effectively final
                LOGGER.info(() -> idx + ") " + opt);
            }

            // Prompt
            LOGGER.info("Scelta: ");
            final String line = sc.nextLine().trim();

            try {
                final int choice = Integer.parseInt(line);
                if (choice >= 1 && choice <= options.length) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // no-op: gestito sotto con warning
            }

            LOGGER.warning("Input non valido. Riprova.");
        }
    }
}