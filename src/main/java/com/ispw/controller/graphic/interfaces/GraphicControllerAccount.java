package com.ispw.controller.graphic.interfaces;

import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerAccount extends NavigableController {

    void loadAccount(SessioneUtenteBean sessione);

    void aggiornaDatiAccount(
            int idUtente,
            String nome,
            String cognome,
            String email,
            SessioneUtenteBean sessione
    );

    void cambiaPassword(
            String vecchiaPassword,
            String nuovaPassword,
            SessioneUtenteBean sessione
    );

    void logout();

    void tornaAllaHome(SessioneUtenteBean sessione);
}