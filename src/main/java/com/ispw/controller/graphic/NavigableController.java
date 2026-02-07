package com.ispw.controller.graphic;

import java.util.Map;

public interface NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia base per controller grafici navigabili.
    // A2) IO verso GUI/CLI: params di navigazione in ingresso.

    String getRouteName();
    default void onShow() { onShow(Map.of()); }
    void onShow(Map<String, Object> params);
    default void onHide() { }
}
