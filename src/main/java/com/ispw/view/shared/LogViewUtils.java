package com.ispw.view.shared;

import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.model.enums.Ruolo;

public final class LogViewUtils {

    private LogViewUtils() {
    }

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
