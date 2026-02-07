package com.ispw.controller.graphic;

public interface GraphicControllerLog extends NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: limite intero per log.
    // A3) Logica delegata: ai controller grafici concreti.

    void richiediLog(int limit);

    void tornaAllaHome();
}
