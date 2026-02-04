package com.ispw.view.cli.console;

import java.util.logging.Logger;

import com.ispw.bean.EsitoDisdettaBean;

public class ConsoleDisdettaAnteprimaView {
    private static final Logger logger = Logger.getLogger(ConsoleDisdettaAnteprimaView.class.getName());
    public void show(EsitoDisdettaBean anteprima) {
        logger.info("\n=== ANTEPRIMA DISDETTA ===");
        logger.info(String.valueOf(anteprima));
    }
} 
