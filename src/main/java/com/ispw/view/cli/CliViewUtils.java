package com.ispw.view.cli;

import java.util.Scanner;

public final class CliViewUtils {

    private CliViewUtils() {
        // utility
    }

    public static void askReturnHome(Scanner in, Runnable goHome) {
        System.out.print("Torna alla home? [s/N]: ");
        String ans = in.nextLine().trim();
        if ("s".equalsIgnoreCase(ans) && goHome != null) {
            goHome.run();
        }
    }

    public static void printMessages(String error, String success) {
        if (error != null && !error.isBlank()) {
            System.err.println("[ERRORE] " + error);
        }
        if (success != null && !success.isBlank()) {
            System.out.println(success);
        }
    }
}
