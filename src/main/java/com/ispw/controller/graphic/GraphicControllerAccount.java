package com.ispw.controller.graphic;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerAccount extends NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa bean SessioneUtenteBean e Map.
    // A3) Logica delegata: ai controller grafici concreti.

    void loadAccount(SessioneUtenteBean sessione);

    void aggiornaDatiAccount(Map<String, Object> nuoviDati);

    void cambiaPassword(String vecchiaPassword, String nuovaPassword, SessioneUtenteBean sessione);

    void logout();

    void tornaAllaHome(SessioneUtenteBean sessione);
}
