package com.ispw.controller.graphic;

public interface GraphicControllerPenalita extends NavigableController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa email/idUtente/importo/motivazione.
    // A3) Logica delegata: ai controller grafici concreti.
    // ========================

    void selezionaUtente(String email);

    void applicaPenalita(int idUtente, float importo, String motivazione);

    void tornaAllaHome();
}