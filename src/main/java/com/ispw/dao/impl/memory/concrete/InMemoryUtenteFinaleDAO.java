package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

public class InMemoryUtenteFinaleDAO extends In_MemoryDAO<Integer, UtenteFinale> implements UtenteFinaleDAO {

    public InMemoryUtenteFinaleDAO() {
        super(true);
    }

    @Override
    protected Integer getId(UtenteFinale entity) {
        // TODO: return entity.getIdUtente();
        throw new UnsupportedOperationException("TODO: UtenteFinale.getIdUtente()");
    }

    @Override
    public UtenteFinale findById(int idUtente) {
        return load(idUtente);
    }

    @Override
    public UtenteFinale findByEmail(String email) {
        throw new UnsupportedOperationException("TODO: findByEmail");
    }
}
