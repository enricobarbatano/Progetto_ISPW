package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

public final class InMemoryRegoleTempisticheDAO implements RegoleTempisticheDAO {
    private final java.util.concurrent.atomic.AtomicReference<RegoleTempistiche> ref = new java.util.concurrent.atomic.AtomicReference<>();
    public InMemoryRegoleTempisticheDAO() { }
    @Override public RegoleTempistiche get() { return ref.get(); } // vedi Nota 2 sotto
    @Override public void save(RegoleTempistiche r) { ref.set(r); }
   
}
