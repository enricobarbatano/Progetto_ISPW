package com.ispw.dao.impl.memory.concrete;


import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.ispw.dao.impl.memory.InMemoryDAO;    // abstract
import com.ispw.dao.interfaces.GeneralUserDAO;  // concreto da istanziare
import com.ispw.model.entity.GeneralUser;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO InMemory per GeneralUser.
 * Responsabilita': gestire persistenza volatile per test/uso locale.
 *
 * SEZIONE LOGICA
 * Delega a InMemoryDAO e gestisce ID e ricerca per email.
 */
public final class InMemoryGeneralUserDAO extends InMemoryDAO<Integer, GeneralUser> implements GeneralUserDAO {

    public InMemoryGeneralUserDAO() {
        super(true); // store condiviso tra istanze della stessa classe concreta
    }

    @Override
    protected Integer getId(GeneralUser entity) {
        return entity.getIdUtente();
    }

    @Override
    public void store(GeneralUser entity) {
        Objects.requireNonNull(entity, "entity non puÃ² essere null");
        if (entity.getIdUtente() == 0) {
            int next = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }
        super.store(entity);
    }

    @Override
    public GeneralUser create(Integer id) {
        GeneralUser u = super.create(id);
        if (u != null && id != null) {
            u.setIdUtente(id);
        }
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

    @Override
    public List<GeneralUser> findAll() {
        return snapshotValues();
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
