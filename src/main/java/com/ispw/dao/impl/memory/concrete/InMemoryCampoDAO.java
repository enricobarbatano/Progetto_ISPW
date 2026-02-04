package com.ispw.dao.impl.memory.concrete;

import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

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
        return new ArrayList<>(snapshotValues());
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }
}
