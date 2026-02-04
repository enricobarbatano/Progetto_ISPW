package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;

public class GUIGraphicControllerPrenotazione implements GraphicControllerPrenotazione {
    
    @Override
    public String getRouteName() {
        return null;
    }

    @Override
    public void setNavigator(com.ispw.controller.graphic.GraphicControllerNavigation navigator) {
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input) {
    }

    @Override
    public void procediAlPagamento(DatiPagamentoBean pagamento) {
    }

    @Override
    public void tornaAllaHome() {
    }
}
