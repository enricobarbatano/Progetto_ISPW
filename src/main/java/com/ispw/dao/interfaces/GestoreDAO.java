package com.ispw.dao.interfaces;

import java.util.Set;

import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

public interface GestoreDAO extends DAO<Integer, Gestore> {

    // lookup tipizzati (eviti casting)
    Gestore findById(int idGestore);
    Gestore findByEmail(String email);

    // permessi
    Set<Permesso> getPermessi(int idGestore);

    boolean hasPermesso(int idGestore, Permesso permesso);

    void assegnaPermesso(int idGestore, Permesso permesso);

    void rimuoviPermesso(int idGestore, Permesso permesso);
}
