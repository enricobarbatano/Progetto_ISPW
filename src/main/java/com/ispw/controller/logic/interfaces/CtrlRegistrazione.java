package com.ispw.controller.logic.interfaces;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteBean;

public interface CtrlRegistrazione {
    
EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput);

    void confermaNuovoAccount(UtenteBean utente);

    void finalizzaAttivazioneAccount(int idUtente);

}
