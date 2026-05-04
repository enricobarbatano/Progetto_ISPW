package com.ispw.controller.graphic.interfaces;

import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerRichiesteDisdetta extends NavigableController {

    void caricaRichiestePending(SessioneUtenteBean sessioneGestore);
    void approva(int idRichiesta, String nota, SessioneUtenteBean sessioneGestore);
    void rifiuta(int idRichiesta, String nota, SessioneUtenteBean sessioneGestore);
    void tornaAllaHome();
}