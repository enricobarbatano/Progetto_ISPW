
// src/main/java/com/ispw/controller/graphic/GraphicControllerDisdetta.java
package com.ispw.controller.graphic;

public interface GraphicControllerDisdetta extends NavigableController {
    void richiediPrenotazioniCancellabili();   // -> LogicControllerDisdettaPrenotazione.ottieniPrenotazioniCancellabili(...)
    void selezionaPrenotazione(int idPrenotazione);
    void richiediAnteprimaDisdetta(int idPrenotazione); // -> ...anteprimaDisdetta(...)
    void confermaDisdetta(int idPrenotazione);          // -> ...eseguiAnnullamento(...)
    void tornaAllaHome();
}
