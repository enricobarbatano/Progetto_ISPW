package com.ispw.dao.impl.memory.concrete;


import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;    // abstract
import com.ispw.model.entity.UtenteFinale;  // concreto da istanziare

import java.util.Locale;
import java.util.Objects;

/**
 * DAO InMemory per GeneralUser.
 * SonarCloud-friendly:
 * - store condiviso per classe (super(true));
 * - id autoincrement se 0; validazioni / early return.
 */
public final class InMemoryGeneralUserDAO extends In_MemoryDAO<Integer, GeneralUser> implements GeneralUserDAO {

    public InMemoryGeneralUserDAO() {
        super(true); // store condiviso tra istanze della stessa classe concreta
    }

    @Override
    protected Integer getId(GeneralUser entity) {
        return entity.getIdUtente();
    }

    @Override
    public void store(GeneralUser entity) {
        Objects.requireNonNull(entity, "entity non può essere null");
        if (entity.getIdUtente() == 0) {
            int next = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }
        super.store(entity);
    }

    @Override
    protected GeneralUser newEntity(Integer id) {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    @Override
    public GeneralUser findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;
        return snapshotValues().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    public GeneralUser findById(int idUtente) {
        return load(idUtente);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
