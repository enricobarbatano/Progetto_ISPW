package com.ispw.view.cli.console;

import java.util.List;

/**
 * Console view per mostrare le prenotazioni cancellabili.
 *
 * La lista mostra l'id reale della prenotazione già presente nella stringa,
 * ad esempio "Prenotazione #30".
 * Per questo motivo non viene mostrato un indice [1], [2], ...
 * così l'utente non confonde l'indice con l'id prenotazione.
 */
public class ConsoleDisdettaElencoView {

    /**
     * Mostra le prenotazioni cancellabili.
     *
     * @param lista elenco testuale delle prenotazioni cancellabili
     */
    public void show(List<String> lista) {
        System.out.println("\n=== PRENOTAZIONI CANCELLABILI ===");

        if (lista == null || lista.isEmpty()) {
            System.out.println("(vuoto)");
            return;
        }

        for (String r : lista) {
            System.out.println(" - " + r);
        }
    }
}