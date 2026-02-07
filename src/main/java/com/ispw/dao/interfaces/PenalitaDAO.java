package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.Penalita;

public interface PenalitaDAO extends DAO<Integer, Penalita> {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita Penalita.
    // A2) IO: query penalita per utente.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) recuperaPenalitaUtente: elenco penalita per utente.
    List<Penalita> recuperaPenalitaUtente(int idUtente);
}
