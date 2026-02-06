package com.ispw.view.cli.console;

public class ConsoleDisdettaAnteprimaView {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view anteprima disdetta.
    // A2) IO: output su console.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) show: stampa anteprima disdetta.
    // ========================
    public void show(boolean possibile, float penale) {
        System.out.println("\n=== ANTEPRIMA DISDETTA ===");
        System.out.println("Possibile: " + possibile + ", penale: " + penale + "€");
    }
} 
