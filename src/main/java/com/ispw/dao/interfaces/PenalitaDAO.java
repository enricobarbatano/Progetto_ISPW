package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.Penalita;


public interface PenalitaDAO extends DAO<Integer, Penalita> {

    /** Recupera tutte le penalità di un utente */
    List<Penalita> recuperaPenalitaUtente(int idUtente);
}
