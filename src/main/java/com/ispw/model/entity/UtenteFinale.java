package com.ispw.model.entity;


import java.util.ArrayList;
import java.util.List;

public final class UtenteFinale extends GeneralUser {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity utente finale.
    // A2) IO: penalita e storico prenotazioni.
    private Penalita penalita; // opzionale
    private final List<Prenotazione> storicoPrenotazioni = new ArrayList<>();

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: penalita e storico.

    public Penalita getPenalita() { return penalita; }
    public void setPenalita(Penalita penalita) { this.penalita = penalita; }

    public List<Prenotazione> getStoricoPrenotazioni() { return storicoPrenotazioni; }
}
