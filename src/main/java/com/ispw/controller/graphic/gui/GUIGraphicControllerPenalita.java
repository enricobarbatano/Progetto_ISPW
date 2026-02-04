package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;

/**
 * Adapter GUI per gestione applicazione penalità.
 */
public class GUIGraphicControllerPenalita implements GraphicControllerPenalita {
    
    @SuppressWarnings("unused")
    private LogicControllerApplicaPenalita logicController;
    private GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerPenalita() {
    }
    
    public GUIGraphicControllerPenalita(
        LogicControllerApplicaPenalita logicController,
        GraphicControllerNavigation navigator) {
        this.logicController = logicController;
        this.navigator = navigator;
    }
    
    public void setLogicController(LogicControllerApplicaPenalita controller) {
        this.logicController = controller;
    }
    
    @Override
    public String getRouteName() {
        return "penalita";
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void selezionaUtente(String email) {
    }

    @Override
    public void applicaPenalita(float importo, String motivazione) {
        if (motivazione == null || importo <= 0) {
            // Valida input GUI
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home", null);
        }
    }
}
