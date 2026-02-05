// src/main/java/com/ispw/view/cli/ConsoleDisdettaElencoView.java
package com.ispw.view.cli.console;

import java.util.List;

public class ConsoleDisdettaElencoView {
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