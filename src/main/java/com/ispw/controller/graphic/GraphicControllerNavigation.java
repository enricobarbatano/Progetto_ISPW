package com.ispw.controller.graphic;

import java.util.Map;

public interface GraphicControllerNavigation {

    /** Registra un controller, usando il suo routeName */
    default GraphicControllerNavigation register(NavigableController controller) {
        return register(controller.getRouteName(), controller);
    }

    /** Versione originale, se vuoi mantenerla retrocompatibile */
    GraphicControllerNavigation register(String route, NavigableController controller);

    void start(String initialRoute);

    /** Naviga verso una route senza parametri */
    default void goTo(String route) { goTo(route, Map.of()); }

    /** Naviga verso una route passando parametri */
    void goTo(String route, Map<String, Object> params);

    /** Opzionale: torna alla schermata precedente, se presente */
    default boolean back() { return false; }
}