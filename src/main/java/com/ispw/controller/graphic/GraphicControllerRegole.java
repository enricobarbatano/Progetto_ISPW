package com.ispw.controller.graphic;

import java.util.Map;

public interface GraphicControllerRegole extends NavigableController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: interfaccia del layer graphic (DIP).
    // A2) IO verso GUI/CLI: usa Map<String, Object> per parametri regole.
    // A3) Logica delegata: ai controller grafici concreti.
    // ========================

    void richiediListaCampi();
    void selezionaCampo(int idCampo);
    void aggiornaStatoCampo(Map<String, Object> regolaCampo);
    void aggiornaTempistiche(Map<String, Object> tempistiche);
    void aggiornaPenalita(Map<String, Object> penalita);
    void tornaAllaHome();
}