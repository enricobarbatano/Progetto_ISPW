package com.ispw.controller.logic.interfaces;

import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteBean;
import com.ispw.exception.registration.RegistrationException;

public interface CtrlRegistrazione {
    
EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput) throws RegistrationException;

    void confermaNuovoAccount(UtenteBean utente);

    void finalizzaAttivazioneAccount(int idUtente);

}
