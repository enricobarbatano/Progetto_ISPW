package com.ispw.dao.interfaces;

import java.util.Set;

import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

public interface GestoreDAO extends DAO<Integer, Gestore> {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita Gestore.
    // A2) IO: lookup e gestione permessi.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findById/findByEmail: lookup.
    // L2) getPermessi/hasPermesso/assegna/rimuovi: permessi.
    Gestore findById(int idGestore);
    Gestore findByEmail(String email);
    Set<Permesso> getPermessi(int idGestore);

    boolean hasPermesso(int idGestore, Permesso permesso);

    void assegnaPermesso(int idGestore, Permesso permesso);

    void rimuoviPermesso(int idGestore, Permesso permesso);
}
