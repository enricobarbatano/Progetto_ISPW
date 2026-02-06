package com.ispw.controller.graphic;

import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerDisdetta extends NavigableController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa bean SessioneUtenteBean.
    // A3) Logica delegata: ai controller grafici concreti.
    // ========================

    void richiediPrenotazioniCancellabili(SessioneUtenteBean sessione);
    void selezionaPrenotazione(int idPrenotazione);
    void richiediAnteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione);
    void confermaDisdetta(int idPrenotazione, SessioneUtenteBean sessione);
    void tornaAllaHome();
}
