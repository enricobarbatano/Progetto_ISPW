// src/main/java/com/ispw/view/cli/ConsoleDisdettaElencoView.java
package com.ispw.view.cli.console;

import java.util.List;

import com.ispw.bean.RiepilogoPrenotazioneBean;

public class ConsoleDisdettaElencoView {
    public void show(List<RiepilogoPrenotazioneBean> lista) {
        System.out.println("\n=== PRENOTAZIONI CANCELLABILI ===");
        if (lista == null || lista.isEmpty()) {
            System.out.println("(vuoto)");
            return;
        }
        int i = 1;
        for (var r : lista) System.out.printf(" [%d] %s%n", i++, r);
    }
}