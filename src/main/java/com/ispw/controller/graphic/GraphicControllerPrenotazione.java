
// src/main/java/com/ispw/controller/graphic/GraphicControllerPrenotazione.java
package com.ispw.controller.graphic;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerPrenotazione extends NavigableController {
    void cercaDisponibilita(ParametriVerificaBean input);                // -> LogicControllerPrenotazioneCampo.trovaSlotDisponibili(...)
    void creaPrenotazione(DatiInputPrenotazioneBean input, SessioneUtenteBean sessione);  // -> LogicControllerPrenotazioneCampo.nuovaPrenotazione(...)
    void procediAlPagamento(DatiPagamentoBean pagamento, SessioneUtenteBean sessione);   // -> LogicControllerPrenotazioneCampo.completaPrenotazione(...)
    void tornaAllaHome();                                                // nav "home"
}
