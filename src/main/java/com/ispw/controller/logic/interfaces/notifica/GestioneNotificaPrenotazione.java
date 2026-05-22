package com.ispw.controller.logic.interfaces.notifica;

import com.ispw.bean.UtenteBean;

public interface GestioneNotificaPrenotazione {
    void inviaConfermaPrenotazione(UtenteBean utente, String dettaglio);
    
}
