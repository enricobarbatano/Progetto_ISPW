// src/main/java/com/ispw/view/cli/ConsoleDisdettaElencoView.java
package com.ispw.view.cli.console;

import java.util.List;
import java.util.logging.Logger;

import com.ispw.bean.RiepilogoPrenotazioneBean;

public class ConsoleDisdettaElencoView {
    private static final Logger logger = Logger.getLogger(ConsoleDisdettaElencoView.class.getName());
    public void show(List<RiepilogoPrenotazioneBean> lista) {
        logger.info("\n=== PRENOTAZIONI CANCELLABILI ===");
        if (lista == null || lista.isEmpty()) {
            logger.info("(vuoto)");
            return;
        }
        int i = 1;
        for (var r : lista) logger.info(String.format(" [%d] %s", i++, r));
    }
}