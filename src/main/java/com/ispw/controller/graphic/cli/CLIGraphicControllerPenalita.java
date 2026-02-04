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
            // Valida input
        }
        
        // EsitoOperazioneBean esito = 
        //     logicController.applicaPenalita(emailUtente, importo, motivazione);
        
        // if (esito != null && esito.isSuccesso()) {
        //     tornaAllaHome();
        // }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
