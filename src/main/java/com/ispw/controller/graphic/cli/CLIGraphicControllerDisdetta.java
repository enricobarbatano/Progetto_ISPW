package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;

/**
 * Adapter CLI per la disdetta prenotazione.
 */
public class CLIGraphicControllerDisdetta implements GraphicControllerDisdetta {
    
    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "disdetta";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    /**
     * Richiede prenotazioni cancellabili per l'utente.
     * View deve fornire UtenteBean dalla sessioneCorrente.
     */
    @Override
    public void richiediPrenotazioniCancellabili() {
        // TODO: implementare richiesta prenotazioni cancellabili
    }

    @Override
    public void selezionaPrenotazione(int idPrenotazione) {
        // TODO: implementare selezione prenotazione
    }

    /**
     * Richiede anteprima disdetta con calcolo rimborso.
     */
    @Override
    public void richiediAnteprimaDisdetta(int idPrenotazione) {
        // TODO: implementare anteprima disdetta
    }

    /**
     * Conferma disdetta prenotazione.
     */
    @Override
    public void confermaDisdetta(int idPrenotazione) {
        // TODO: implementare conferma disdetta
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
