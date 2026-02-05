package com.ispw.dao.impl.memory.concrete;


import java.util.Comparator;
import java.util.List;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public final class InMemoryPenalitaDAO extends InMemoryDAO<Integer, Penalita> implements PenalitaDAO {
    public InMemoryPenalitaDAO() { super(true); }
    @Override protected Integer getId(Penalita e) { return e != null ? e.getIdPenalita() : 0; }
    @Override
    public void store(Penalita entity) {
        if (entity.getIdPenalita() == 0) {
            int next = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPenalita(next);
        }
        super.store(entity);
    }
    @Override public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        return snapshotValues().stream()
            .filter(p -> p != null && p.getIdUtente() == idUtente)
            .sorted(Comparator.comparing(Penalita::getDataEmissione,
                    Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Penalita::getIdPenalita)
                .reversed())
            .toList();
    }
}
