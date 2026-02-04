package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;

public class CLIGraphicControllerPenalita implements GraphicControllerPenalita {
    
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
    public void selezionaUtente(String email) {
    }

    @Override
    public void applicaPenalita(float importo, String motivazione) {
    }

    @Override
    public void tornaAllaHome() {
    }
}
