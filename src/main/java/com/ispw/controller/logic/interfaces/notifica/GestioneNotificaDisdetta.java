package com.ispw.controller.logic.interfaces.notifica;

import com.ispw.bean.UtenteBean;

public interface GestioneNotificaDisdetta {
    void inviaConfermaCancellazione(UtenteBean utente, String dettaglio);
}
