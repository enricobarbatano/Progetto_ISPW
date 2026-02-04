package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;

/**
 * Adapter GUI per la disdetta prenotazione.
 */
public class GUIGraphicControllerDisdetta implements GraphicControllerDisdetta {
    
    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "disdetta";
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void richiediPrenotazioniCancellabili() {
    }

    @Override
    public void selezionaPrenotazione(int idPrenotazione) {
    }

    @Override
    public void richiediAnteprimaDisdetta(int idPrenotazione) {
    }

    @Override
    public void confermaDisdetta(int idPrenotazione) {
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home", null);
        }
    }
}
