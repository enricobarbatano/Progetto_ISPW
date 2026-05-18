package com.ispw.controller.logic.interfaces;

import java.util.List;

import com.ispw.bean.CampiBean;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.StatoPagamentoBean;

public interface CtrlPrenotazione {
    
    CampiBean listaCampi();

    List<DatiDisponibilitaBean> trovaSlotDisponibili(ParametriVerificaBean param);

    RiepilogoPrenotazioneBean nuovaPrenotazione(
            DatiInputPrenotazioneBean input,
            SessioneUtenteBean sessione
    );

    StatoPagamentoBean completaPrenotazione(
            DatiPagamentoBean dati,
            SessioneUtenteBean sessione
    );

}
