package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;

/**
 * Adapter CLI per gestione applicazione penalità.
 */
public class CLIGraphicControllerPenalita implements GraphicControllerPenalita {
    
    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
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
        // Memorizza utente selezionato per operazione successiva
    }

    /**
     * Applica penalità a un utente.
     */
    @Override
    public void applicaPenalita(float importo, String motivazione) {
        if (motivazione == null || importo <= 0) {
            return;  // Valida input
        }
        // TODO: implementare applicazione penalita
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
