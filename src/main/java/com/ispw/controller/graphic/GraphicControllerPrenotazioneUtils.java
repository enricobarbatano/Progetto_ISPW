package com.ispw.controller.graphic;

import java.util.List;

import com.ispw.bean.DatiDisponibilitaBean;

public final class GraphicControllerPrenotazioneUtils {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: utility del layer graphic (stateless).
    // A2) IO verso GUI/CLI: formatta DatiDisponibilitaBean per la view.

    private GraphicControllerPrenotazioneUtils() {
    }

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) formatSlotDisponibili: lista formattata dei slot disponibili.

    public static List<String> formatSlotDisponibili(List<DatiDisponibilitaBean> slot) {
        if (slot == null) {
            return List.of();
        }
        return slot.stream()
            .map(s -> s.getData() + " " + s.getOraInizio() + "-" + s.getOraFine() + " (â‚¬" + s.getCosto() + ")")
            .toList();
    }
}
