package com.ispw.controller.graphic;

import java.util.Map;

/**
 * Router/Navigator: cambia "schermata" (CLI menu o JavaFX scene).
 * È l’unico che mantiene il contesto di navigazione (stack, route corrente, ecc.).
 */
public interface GraphicControllerNavigation {

    /** Vai a una route senza parametri */
    default void goTo(String route) { goTo(route, Map.of()); }

    /** Vai a una route con parametri (passaggio dati tra schermate) */
    void goTo(String route, Map<String, Object> params);

    /** Torna indietro (se supportato). In CLI può tornare al menu precedente. */
    void back();

    /** Chiude l'applicazione in modo controllato */
    void exit();
}