package com.ispw.controller.graphic;

/**
 * Graphic controller per UC di gestione penalità (tipicamente lato gestore: selezione utente, applicazione sanzione).
 * Stateless: i dati selezionati possono essere passati via params o recuperati da input UI al momento dell'azione.
 */
public interface GraphicControllerPenalita extends NavigableController {

    /**
     * Seleziona un utente bersaglio tramite email (o altro identificativo) per operazioni sulle penalità.
     */
    void selezionaUtente(String email);

    /**
     * Applica una penalità all'utente precedentemente selezionato.
     * I dati necessari arrivano dalla UI (CLI/GUI).
     */
    void applicaPenalita(int idUtente, float importo, String motivazione);

    /**
     * Torna alla home/menu principale della sessione corrente.
     */
    void tornaAllaHome();
}