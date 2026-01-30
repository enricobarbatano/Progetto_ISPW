package com.ispw.model.entity;


import java.util.ArrayList;
import java.util.List;

public final class UtenteFinale extends GeneralUser {
    private Penalita penalita; // opzionale
    private final List<Prenotazione> storicoPrenotazioni = new ArrayList<>();

    public Penalita getPenalita() { return penalita; }
    public void setPenalita(Penalita penalita) { this.penalita = penalita; }

    public List<Prenotazione> getStoricoPrenotazioni() { return storicoPrenotazioni; }
}
