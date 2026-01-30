package com.ispw.dao.impl.memory.concrete;

import java.util.List;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

public class InMemoryCampoDAO extends In_MemoryDAO<Integer, Campo> implements CampoDAO {

    public InMemoryCampoDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Campo entity) {
        // TODO: return entity.getIdCampo();
        throw new UnsupportedOperationException("TODO: Campo.getIdCampo()");
    }

    @Override
    public List<Campo> findAll() {
        return snapshotValues();
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }
}

