package com.ispw.dao.impl.memory.concrete;

import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

/**
 * DAO In-Memory per Campo.
 * - Usa la base In_MemoryDAO (ConcurrentHashMap + lock).
 * - sharedStore=true per condividere lo store fra istanze (utile in test).
 */
public class InMemoryCampoDAO extends In_MemoryDAO<Integer, Campo> implements CampoDAO {

    public InMemoryCampoDAO() {
        super(true); // store condiviso per tutta la classe DAO
    }

    @Override
    protected Integer getId(Campo entity) {
        return entity.getIdCampo();
    }

    @Override
    public List<Campo> findAll() {
        return new ArrayList<>(snapshotValues());
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }
}