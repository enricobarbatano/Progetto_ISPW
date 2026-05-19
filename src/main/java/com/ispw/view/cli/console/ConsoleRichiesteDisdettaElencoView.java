package com.ispw.view.cli.console;

import java.util.List;

/**
 * Console view per mostrare le richieste di disdetta pending.
 *
 * RESPONSABILITÀ:
 * - stampare su console l'elenco delle richieste di disdetta;
 * - gestire il caso di lista vuota.
 *
 * NON:
 * - legge input utente;
 * - chiama controller grafici;
 * - crea bean;
 * - accede a DAO o logic controller.
 */
public class ConsoleRichiesteDisdettaElencoView {

    /**
     * Stampa la lista delle richieste pending.
     *
     * @param richieste elenco testuale delle richieste di disdetta
     */
    public void show(List<String> richieste) {
        System.out.println("\n=== RICHIESTE DISDETTA (PENDING) ===");

        if (richieste == null || richieste.isEmpty()) {
            System.out.println("(nessuna richiesta pending)");
            return;
        }

        int index = 1;
        for (String richiesta : richieste) {
            System.out.println("[" + index++ + "] " + richiesta);
        }
    }
}