
// src/main/java/com/ispw/controller/graphic/GraphicControllerPrenotazione.java
package com.ispw.controller.graphic;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;

public interface GraphicControllerPrenotazione extends NavigableController {
    void cercaDisponibilita(ParametriVerificaBean input);                // -> LogicControllerPrenotazioneCampo.trovaSlotDisponibili(...)
    void creaPrenotazione(DatiInputPrenotazioneBean input);              // -> LogicControllerPrenotazioneCampo.nuovaPrenotazione(...)
    void procediAlPagamento(DatiPagamentoBean pagamento);                // -> LogicControllerGestionePagamento.richiediPagamentoPrenotazione(...)
    void tornaAllaHome();                                                // nav "home"
}
