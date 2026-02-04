package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;
import com.ispw.controller.logic.ctrl.LogicControllerPrenotazioneCampo;

/**
 * Adapter GUI per la prenotazione campo.
 */
public class GUIGraphicControllerPrenotazione implements GraphicControllerPrenotazione {
    
    @SuppressWarnings("unused")
    private LogicControllerPrenotazioneCampo logicController;
    private GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerPrenotazione() {
    }
    
    public GUIGraphicControllerPrenotazione(
        LogicControllerPrenotazioneCampo logicController,
        GraphicControllerNavigation navigator) {
        this.logicController = logicController;
        this.navigator = navigator;
    }
    
    public void setLogicController(LogicControllerPrenotazioneCampo controller) {
        this.logicController = controller;
    }
    
    @Override
    public String getRouteName() {
        return "prenotazione";
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
            return;
        }
        
        // List<DatiDisponibilitaBean> disponibilita = 
        //     logicController.trovaSlotDisponibili(input, sessioneCorrente);
        // Visualizza in TableView/JTable
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input) {
        if (input == null) {
            return;
        }
        
        // RiepilogoPrenotazioneBean riepilogo = 
        //     logicController.nuovaPrenotazione(input, sessioneCorrente, notificaCtrl);
        // Mostra dialog riepilogo
    }

    @Override
    public void procediAlPagamento(DatiPagamentoBean pagamento) {
        if (pagamento == null) {
            return;
        }
        
        // StatoPagamentoBean stato = logicController.completaPrenotazione(
        //     pagamento, sessioneCorrente, pagamentoCtrl, fatturaCtrl, notificaCtrl);
        // if (stato != null) { tornaAllaHome(); }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home", null);
        }
    }
}
