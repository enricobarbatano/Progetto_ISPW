package com.ispw.dao.impl.memory.concrete;



import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

public final class InMemoryRegolePenalitaDAO implements RegolePenalitaDAO {
    private final java.util.concurrent.atomic.AtomicReference<RegolePenalita> ref = new java.util.concurrent.atomic.AtomicReference<>();
    public InMemoryRegolePenalitaDAO() { }
    @Override public RegolePenalita get() { return ref.get(); }   // vedi Nota 2 sotto
    @Override public void save(RegolePenalita r) { ref.set(r); }
    /* opzionale per i test */ void clear() { ref.set(null); }
}

