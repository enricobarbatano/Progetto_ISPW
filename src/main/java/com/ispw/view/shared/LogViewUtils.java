package com.ispw.view.shared;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

public final class LogViewUtils {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: utility condivisa per view log.
    // A2) IO: filtra ruolo e legge lista log dai params.
    // ========================

    private LogViewUtils() {
    }

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) isGestore: verifica accesso.
    // L2) readLogs: estrazione e normalizzazione logs.
    // ========================

    public static boolean isGestore(SessioneUtenteBean sessione) {
        return sessione != null
            && sessione.getUtente() != null
            && sessione.getUtente().getRuolo() == Ruolo.GESTORE;
    }

    public static List<String> readLogs(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Object raw = params.get(GraphicControllerUtils.KEY_LOGS);
        if (!(raw instanceof List<?> logs)) {
            return null;
        }
        return logs.stream().map(String::valueOf).toList();
    }
}
