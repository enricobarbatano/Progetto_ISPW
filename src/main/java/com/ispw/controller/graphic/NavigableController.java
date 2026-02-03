package com.ispw.controller.graphic;

import java.util.Map;

/** Controller grafico “navigabile” (adapter UI ↔ use case) */
public interface NavigableController {
    String getRouteName();

    /** Inject del router */
    void setNavigator(GraphicControllerNavigation navigator);

    /** Lifecycle: mostrata senza parametri */
    default void onShow() { onShow(Map.of()); }

    /** Lifecycle: mostrata con parametri */
    void onShow(Map<String, Object> params);

    /** Lifecycle: in uscita dalla schermata (opzionale) */
    default void onHide() {}
}