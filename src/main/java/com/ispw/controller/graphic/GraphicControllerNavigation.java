package com.ispw.controller.graphic;

import java.util.Map;

public interface GraphicControllerNavigation {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia di routing UI (CLI/GUI).
    // A2) IO verso GUI/CLI: route e parametri tra schermate.

    default void goTo(String route) { goTo(route, Map.of()); }
    void goTo(String route, Map<String, Object> params);
    void back();
    void exit();
}
