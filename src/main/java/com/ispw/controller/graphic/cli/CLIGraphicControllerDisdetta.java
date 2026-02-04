package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;

public class CLIGraphicControllerDisdetta implements GraphicControllerDisdetta {
    
    @Override
    public String getRouteName() {
        return null;
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
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
    }
}
