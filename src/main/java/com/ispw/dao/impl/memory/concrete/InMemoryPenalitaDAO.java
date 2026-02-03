package com.ispw.dao.impl.memory.concrete;


import java.util.List;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public final class InMemoryPenalitaDAO extends In_MemoryDAO<Integer, Penalita> implements PenalitaDAO {
    public InMemoryPenalitaDAO() { super(true); }
    @Override protected Integer getId(Penalita e) { return e != null ? e.getIdPenalita() : 0; }
    @Override public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        return snapshotValues().stream()
            .filter(p -> p != null && p.getIdUtente() == idUtente)
            .toList();
    }
}
