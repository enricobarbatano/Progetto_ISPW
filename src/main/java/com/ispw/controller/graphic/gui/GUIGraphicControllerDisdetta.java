package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.logic.ctrl.LogicControllerDisdettaPrenotazione;

/**
 * Adapter GUI per la disdetta prenotazione.
 */
public class GUIGraphicControllerDisdetta implements GraphicControllerDisdetta {
    
    @SuppressWarnings("unused")
    private LogicControllerDisdettaPrenotazione logicController;
    private GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerDisdetta() {
    }
    
    public GUIGraphicControllerDisdetta(
        LogicControllerDisdettaPrenotazione logicController,
        GraphicControllerNavigation navigator) {
        this.logicController = logicController;
        this.navigator = navigator;
    }
    
    public void setLogicController(LogicControllerDisdettaPrenotazione controller) {
        this.logicController = controller;
    }
    
    @Override
    public String getRouteName() {
        return "disdetta";
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
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
