package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegole;

public class CLIGraphicControllerRegole implements GraphicControllerRegole {
    
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
    public void richiediListaCampi() {
    }

    @Override
    public void selezionaCampo(int idCampo) {
    }

    @Override
    public void aggiornaStatoCampo(Map<String, Object> regolaCampo) {
    }

    @Override
    public void aggiornaTempistiche(Map<String, Object> tempistiche) {
    }

    @Override
    public void aggiornaPenalita(Map<String, Object> penalita) {
    }

    @Override
    public void tornaAllaHome() {
    }
}
