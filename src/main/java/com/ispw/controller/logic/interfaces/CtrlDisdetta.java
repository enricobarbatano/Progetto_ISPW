package com.ispw.controller.logic.interfaces;

import java.util.List;

import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RichiestaDisdettaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;

public interface CtrlDisdetta {
    
    List<RiepilogoPrenotazioneBean> ottieniPrenotazioniCancellabili(UtenteBean utente);

    EsitoDisdettaBean anteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione);

    EsitoOperazioneBean richiediDisdetta(
            int idPrenotazione,
            String notaUtente,
            SessioneUtenteBean sessione
    );

    List<RichiestaDisdettaBean> listaRichiestePending(SessioneUtenteBean sessioneGestore);

    EsitoOperazioneBean valutaRichiestaDisdetta(
            int idRichiesta,
            boolean approva,
            String notaGestore,
            SessioneUtenteBean sessioneGestore
    );

}
