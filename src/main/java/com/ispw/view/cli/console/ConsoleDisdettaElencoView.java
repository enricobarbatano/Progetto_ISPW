package com.ispw.view.cli.console;

import java.util.List;

public class ConsoleDisdettaElencoView {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view elenco disdetta.
    // A2) IO: output su console.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) show: stampa elenco prenotazioni.
    public void show(List<String> lista) {
        System.out.println("\n=== PRENOTAZIONI CANCELLABILI ===");
        if (lista == null || lista.isEmpty()) {
            System.out.println("(vuoto)");
            return;
        }
        int i = 1;
        for (var r : lista) System.out.println(String.format(" [%d] %s", i++, r));
    }
}
