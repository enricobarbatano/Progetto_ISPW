package com.ispw.bootstrap;


import java.util.Scanner;

public final class ConsoleMenu {
    private final Scanner sc = new Scanner(System.in);

    public int askOption(String title, String... options) {
        while (true) {
            System.out.println("\n=== " + title + " ===");
            for (int i = 0; i < options.length; i++) {
                System.out.printf("%d) %s%n", (i + 1), options[i]);
            }
            System.out.print("Scelta: ");
            String line = sc.nextLine().trim();

            try {
                int choice = Integer.parseInt(line);
                if (choice >= 1 && choice <= options.length) return choice;
            } catch (NumberFormatException ignored) {}

            System.out.println("Input non valido. Riprova.");
        }
    }
}

