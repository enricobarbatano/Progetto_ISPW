package com.ispw.controller.graphic;

import java.util.Map;

/**
 * Graphic controller per UC di gestione/configurazione regole (campo, tempistiche, penalità).
 * Stateless: le modifiche arrivano dalla UI e vengono applicate via logic controller.
 */
public interface GraphicControllerRegole extends NavigableController {

    /**
     * Richiede la lista dei campi (per selezione/gestione regole).
     */
    void richiediListaCampi();

    /**
     * Seleziona un campo su cui operare.
     */
    void selezionaCampo(int idCampo);

    /**
     * Aggiorna regole/stato del campo (attivo, manutenzione, ecc.).
     * @param regolaCampo mappa con i parametri della regola del campo.
     */
    void aggiornaStatoCampo(Map<String, Object> regolaCampo);

    /**
     * Aggiorna le regole di tempistiche (durata slot, orari, preavviso, ecc.).
     * @param tempistiche mappa con i parametri di tempistica.
     */
    void aggiornaTempistiche(Map<String, Object> tempistiche);

    /**
     * Aggiorna la regola penalità (valore, preavviso minimo, ecc.).
     * @param penalita mappa con i parametri della penalità.
     */
    void aggiornaPenalita(Map<String, Object> penalita);

    /**
     * Torna alla home/menu principale della sessione corrente.
     */
    void tornaAllaHome();
}