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
    }

    /**
     * Richiede prenotazioni cancellabili per l'utente.
     * View deve fornire UtenteBean dalla sessioneCorrente.
     */
    @Override
    public void richiediPrenotazioniCancellabili() {
        // UtenteBean utente = view.getSessioneCorrente().getUtente();
        // List<RiepilogoPrenotazioneBean> prenotazioni = 
        //     logicController.ottieniPrenotazioniCancellabili(utente);
        // view.mostraPrenotazioni(prenotazioni);
    }

    @Override
    public void selezionaPrenotazione(int idPrenotazione) {
        // Memorizza selezione per operazioni successive
    }

    /**
     * Richiede anteprima disdetta con calcolo rimborso.
     */
    @Override
    public void richiediAnteprimaDisdetta(int idPrenotazione) {
        // SessioneUtenteBean sessione = view.getSessioneCorrente();
        // EsitoDisdettaBean esito = 
        //     logicController.anteprimaDisdetta(idPrenotazione, sessione);
        // view.mostraAnteprima(esito);
    }

    /**
     * Conferma disdetta prenotazione.
     */
    @Override
    public void confermaDisdetta(int idPrenotazione) {
        // EsitoOperazioneBean esito = 
        //     logicController.eseguiAnnullamento(idPrenotazione, ...);
        // if (esito.isSuccesso()) { tornaAllaHome(); }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
