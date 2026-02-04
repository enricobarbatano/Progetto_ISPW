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
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    /**
     * Cerca slot disponibili.
     */
    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
            return;
        }
        // TODO: implementare richiesta disponibilita
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input) {
        if (input == null) {
            return;
        }
        // TODO: implementare creazione prenotazione
    }

    @Override
    public void procediAlPagamento(DatiPagamentoBean pagamento) {
        if (pagamento == null) {
            return;
        }
        // TODO: implementare completamento pagamento
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
