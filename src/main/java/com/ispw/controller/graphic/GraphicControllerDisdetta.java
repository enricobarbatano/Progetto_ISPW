
// src/main/java/com/ispw/controller/graphic/GraphicControllerDisdetta.java
package com.ispw.controller.graphic;

import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerDisdetta extends NavigableController {
    void richiediPrenotazioniCancellabili(SessioneUtenteBean sessione);   // -> LogicControllerDisdettaPrenotazione.ottieniPrenotazioniCancellabili(...)
    void selezionaPrenotazione(int idPrenotazione);
    void richiediAnteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione); // -> ...anteprimaDisdetta(...)
    void confermaDisdetta(int idPrenotazione, SessioneUtenteBean sessione);          // -> ...eseguiAnnullamento(...)
    void tornaAllaHome();
}
