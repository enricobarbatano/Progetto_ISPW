package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

public final class InMemoryUtenteFinaleDAO extends InMemoryDAO<Integer, UtenteFinale> implements UtenteFinaleDAO {
    public InMemoryUtenteFinaleDAO() { super(true); }
    @Override protected Integer getId(UtenteFinale e) { return e != null ? e.getIdUtente() : 0; }
    @Override public UtenteFinale findById(int idUtente) { return load(idUtente); }
    @Override public UtenteFinale findByEmail(String email) {
        if (email == null) return null;
        String norm = email.trim().toLowerCase();
        return snapshotValues().stream()
            .filter(u -> u != null && u.getEmail() != null && u.getEmail().trim().toLowerCase().equals(norm))
            .findFirst().orElse(null);
    }
}

