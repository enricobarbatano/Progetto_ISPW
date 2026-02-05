package com.ispw.controller.graphic;

import java.util.List;

import com.ispw.bean.DatiDisponibilitaBean;

public final class GraphicControllerPrenotazioneUtils {

    private GraphicControllerPrenotazioneUtils() {
        // utility class
    }

    public static List<String> formatSlotDisponibili(List<DatiDisponibilitaBean> slot) {
        if (slot == null) {
            return List.of();
        }
        return slot.stream()
            .map(s -> s.getData() + " " + s.getOraInizio() + "-" + s.getOraFine() + " (€" + s.getCosto() + ")")
            .toList();
    }
}
