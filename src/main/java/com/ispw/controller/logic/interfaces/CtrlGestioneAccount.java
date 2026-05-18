package com.ispw.controller.logic.interfaces;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.LogsBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;

public interface CtrlGestioneAccount {
    
    LogsBean listaUltimiLog(int limit);

    DatiAccountBean recuperaInformazioniAccount(SessioneUtenteBean sessione);

    EsitoOperazioneBean aggiornaDatiAccount(DatiAccountBean nuovidati);

    EsitoOperazioneBean cambiaPassword(
            String vecchiaPwd,
            String nuovaPwd,
            SessioneUtenteBean sessione
    );

    void confermaModificaAccount(UtenteBean utente);

}
