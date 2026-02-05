package com.ispw.controller.graphic;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.ispw.model.entity.SystemLog;

public final class GraphicControllerLogUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private GraphicControllerLogUtils() {
        // utility class
    }

    public static List<String> formatLogs(List<SystemLog> logs) {
        if (logs == null) {
            return List.of();
        }
        return logs.stream()
            .map(GraphicControllerLogUtils::formatLog)
            .toList();
    }

    private static String formatLog(SystemLog log) {
        if (log == null) {
            return "";
        }
        String ts = log.getTimestamp() != null ? log.getTimestamp().format(FMT) : "-";
        String tipo = log.getTipoOperazione() != null ? log.getTipoOperazione().name() : "-";
        String desc = log.getDescrizione() != null ? log.getDescrizione() : "";
        return String.format("[%s] %s | utente=%d | %s", ts, tipo, log.getIdUtenteCoinvolto(), desc);
    }
}
