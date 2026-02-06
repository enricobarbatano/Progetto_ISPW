package com.ispw.view.cli.console;

public class ConsoleDisdettaEsitoView {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view esito disdetta.
    // A2) IO: output su console.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) showMessage: stampa esito disdetta.
    // ========================
    public void showMessage(String esito) {
        System.out.println("\n=== ESITO DISDETTA ===");
        System.out.println(String.valueOf(esito));
    }
} 
