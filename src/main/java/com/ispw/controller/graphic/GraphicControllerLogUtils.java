package com.ispw.controller.graphic;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.ispw.bean.LogEntryBean;
import com.ispw.bean.LogsBean;

public final class GraphicControllerLogUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private GraphicControllerLogUtils() {
        // utility class
    }

    public static List<String> formatLogs(LogsBean logs) {
        if (logs == null || logs.getLogs() == null || logs.getLogs().isEmpty()) {
            return List.of("Nessun log disponibile");
        }
        return logs.getLogs().stream()
            .map(GraphicControllerLogUtils::formatLog)
            .toList();
    }

    private static String formatLog(LogEntryBean log) {
        if (log == null) {
            return "";
        }
        String ts = log.getTimestamp() != null ? log.getTimestamp().format(FMT) : "-";
        String tipo = log.getTipoOperazione() != null ? log.getTipoOperazione() : "-";
        String desc = log.getDescrizione() != null ? log.getDescrizione() : "";
        return String.format("[%s] %s | utente=%d | %s", ts, tipo, log.getIdUtenteCoinvolto(), desc);
    }
}
