package com.ispw.controller.logic.interfaces;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;

public interface CtrlAccesso {
    
    SessioneUtenteBean verificaCredenziali(DatiLoginBean datiLogin);

    void saveLog(SessioneUtenteBean sessione);

}
