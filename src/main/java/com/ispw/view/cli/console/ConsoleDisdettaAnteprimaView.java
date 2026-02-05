package com.ispw.view.cli.console;

public class ConsoleDisdettaAnteprimaView {
    public void show(boolean possibile, float penale) {
        System.out.println("\n=== ANTEPRIMA DISDETTA ===");
        System.out.println("Possibile: " + possibile + ", penale: " + penale + "€");
    }
} 
