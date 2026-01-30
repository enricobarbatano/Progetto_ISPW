package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

public class InMemoryFatturaDAO extends In_MemoryDAO<Integer, Fattura> implements FatturaDAO {

    public InMemoryFatturaDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Fattura entity) {
        // TODO: return entity.getIdFattura();
        throw new UnsupportedOperationException("TODO: Fattura.getIdFattura()");
    }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        throw new UnsupportedOperationException("TODO: findLastByUtente");
    }
}
