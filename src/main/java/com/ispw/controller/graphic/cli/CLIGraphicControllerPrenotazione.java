package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;

/**
 * Adapter CLI per la prenotazione campo.
 */
public class CLIGraphicControllerPrenotazione implements GraphicControllerPrenotazione {
    
    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "prenotazione";
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    /**
     * Cerca slot disponibili.
     */
    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
            return;
        }
        
        // Delega a LogicController (SessioneUtenteBean viene dalla View)
        // List<DatiDisponibilitaBean> disponibilita = 
        //     logicController.trovaSlotDisponibili(input, sessioneCorrente);
        // view.mostraDisponibilita(disponibilita);
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input) {
        if (input == null) {
            return;
        }
        
        // Delega a LogicController (SessioneUtenteBean e notifica dalla View)
        // RiepilogoPrenotazioneBean riepilogo = 
        //     logicController.nuovaPrenotazione(input, sessioneCorrente, notificaCtrl);
        // view.mostraRiepilogo(riepilogo);
    }

    @Override
    public void procediAlPagamento(DatiPagamentoBean pagamento) {
        if (pagamento == null) {
            return;
        }
        
        // Delega a LogicController (tutti i controller di notifica dalla View)
        // StatoPagamentoBean stato = logicController.completaPrenotazione(
        //     pagamento, sessioneCorrente, pagamentoCtrl, fatturaCtrl, notificaCtrl);
        // if (stato != null) { tornaAllaHome(); }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
