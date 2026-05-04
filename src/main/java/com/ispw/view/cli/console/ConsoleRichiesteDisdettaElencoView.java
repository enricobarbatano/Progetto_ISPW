package com.ispw.view.cli.console;

import java.util.List;

public class ConsoleRichiesteDisdettaElencoView {

    public void show(List<String> richieste) {
        System.out.println("\n=== RICHIESTE DISDETTA (PENDING) ===");
        if (richieste == null || richieste.isEmpty()) {
            System.out.println("(nessuna richiesta pending)");
            return;
        }
        for (String r : richieste) {
            System.out.println(" - " + r);
        }
    }
}