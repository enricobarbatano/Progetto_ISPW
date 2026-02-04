package com.ispw.view.cli.console;

import java.util.logging.Logger;

import com.ispw.bean.EsitoOperazioneBean;

public class ConsoleDisdettaEsitoView {
    private static final Logger logger = Logger.getLogger(ConsoleDisdettaEsitoView.class.getName());
    public void show(EsitoOperazioneBean esito) {
        logger.info("\n=== ESITO DISDETTA ===");
        logger.info(String.valueOf(esito));
    }
} 
