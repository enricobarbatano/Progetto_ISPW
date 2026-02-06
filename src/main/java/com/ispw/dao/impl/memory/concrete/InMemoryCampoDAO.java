package com.ispw.dao.impl.memory.concrete;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO InMemory per Campo.
 * Responsabilita': gestire persistenza volatile per test/uso locale.
 *
 * SEZIONE LOGICA
 * Delega a InMemoryDAO e applica filtri/ordinamenti specifici.
 */
public final class InMemoryCampoDAO extends InMemoryDAO<Integer, Campo> implements CampoDAO {

    public InMemoryCampoDAO() {
        super(true); // store condiviso per eventuali istanze
    }

    @Override
    protected Integer getId(Campo entity) {
        return entity != null ? entity.getIdCampo() : 0;
    }

    @Override
    public List<Campo> findAll() {
        // snapshotValues() è già thread-safe; restituiamo una copia difensiva
        List<Campo> all = new ArrayList<>(snapshotValues());
        all.sort(Comparator.comparingInt(Campo::getIdCampo));
        return all;
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }
}
