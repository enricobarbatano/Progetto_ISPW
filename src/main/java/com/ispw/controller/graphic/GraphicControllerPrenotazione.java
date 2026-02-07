package com.ispw.controller.graphic;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerPrenotazione extends NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa bean ParametriVerificaBean, DatiInputPrenotazioneBean, DatiPagamentoBean.
    // A3) Logica delegata: ai controller grafici concreti.

    void cercaDisponibilita(ParametriVerificaBean input);
    void creaPrenotazione(DatiInputPrenotazioneBean input, SessioneUtenteBean sessione);
    void procediAlPagamento(DatiPagamentoBean pagamento, SessioneUtenteBean sessione);
    void tornaAllaHome();
}
