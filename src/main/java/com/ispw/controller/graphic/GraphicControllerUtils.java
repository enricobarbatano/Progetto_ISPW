package com.ispw.controller.graphic;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility per i controller grafici: gestione messaggi standard e navigazione errori.
 */
public final class GraphicControllerUtils {

    private GraphicControllerUtils() {
        // utility class
    }

    public static void notifyError(Logger log,
                                   GraphicControllerNavigation navigator,
                                   String route,
                                   String prefix,
                                   String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (log != null) {
            log.warning(() -> prefix + " " + message);
        }
        if (navigator != null) {
            navigator.goTo(route, Map.of("error", message));
        }
    }

    public static void handleOnShow(Logger log, Map<String, Object> params, String prefix) {
        if (params == null || params.isEmpty()) {
            return;
        }
        Object error = params.get("error");
        if (error != null) {
            if (log != null) {
                log.warning(() -> prefix + " " + error);
            }
            return;
        }
        Object rawMessage = params.get("message");
        if (rawMessage == null) {
            rawMessage = params.get("successo");
        }
        if (rawMessage != null && log != null) {
            final String msg = String.valueOf(rawMessage);
            log.info(() -> prefix + " " + msg);
        }
    }
}
