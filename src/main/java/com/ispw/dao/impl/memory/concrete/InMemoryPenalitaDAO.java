package com.ispw.dao.impl.memory.concrete;

import java.util.List;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public class InMemoryPenalitaDAO extends In_MemoryDAO<Integer, Penalita> implements PenalitaDAO {

    public InMemoryPenalitaDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Penalita entity) {
        // TODO: return entity.getIdPenalita();
        throw new UnsupportedOperationException("TODO: Penalita.getIdPenalita()");
    }

    @Override
    public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        throw new UnsupportedOperationException("TODO: recuperaPenalitaUtente");
    }
}
