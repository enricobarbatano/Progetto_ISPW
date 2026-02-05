package com.ispw.dao.impl.memory.concrete;

import java.util.Comparator;
import java.util.List;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

public final class InMemoryFatturaDAO extends InMemoryDAO<Integer, Fattura> implements FatturaDAO {

    public InMemoryFatturaDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Fattura entity) {
        return entity != null ? entity.getIdFattura() : 0;
    }

    @Override
    public void store(Fattura entity) {
        if (entity.getIdFattura() == 0) {
            int next = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdFattura(next);
        }
        super.store(entity);
    }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        List<Fattura> all = snapshotValues();
        return all.stream()
                  .filter(f -> f != null && f.getIdUtente() == idUtente)
                  .max(Comparator
                          .comparing(Fattura::getDataEmissione,
                                     Comparator.nullsLast(Comparator.naturalOrder()))
                          .thenComparingInt(Fattura::getIdFattura))
                  .orElse(null);
    }
}
