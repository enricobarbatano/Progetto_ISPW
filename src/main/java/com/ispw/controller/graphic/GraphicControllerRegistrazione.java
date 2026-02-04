package com.ispw.controller.graphic;

import java.util.Map;

/**
 * Graphic controller per UC di registrazione.
 * Stateless: i dati registrazione arrivano dalla UI e non vengono mantenuti nel controller.
 */
public interface GraphicControllerRegistrazione extends NavigableController {

    /**
     * Invia i dati di registrazione.
     * @param datiRegistrazione dati raccolti dalla view (CLI/GUI). Tipicamente: nome, email, password, ecc.
     */
    void inviaDatiRegistrazione(Map<String, Object> datiRegistrazione);

    /**
     * Naviga alla schermata di login (route iniziale).
     */
    void vaiAlLogin();
}