package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;

/**
 * Adapter GUI per gestione applicazione penalità.
 */
public class GUIGraphicControllerPenalita implements GraphicControllerPenalita {
    
    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "penalita";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    @Override
    public void selezionaUtente(String email) {
        // Metodo intenzionalmente vuoto: implementazione GUI specifica da completare
    }

    @Override
    public void applicaPenalita(float importo, String motivazione) {
        if (motivazione == null || importo <= 0) {
            // TODO: validare input GUI
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home", null);
        }
    }
}
