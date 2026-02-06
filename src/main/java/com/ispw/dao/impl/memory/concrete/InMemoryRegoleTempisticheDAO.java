package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO InMemory per RegoleTempistiche.
 * Responsabilita': gestire persistenza volatile per test/uso locale.
 *
 * SEZIONE LOGICA
 * Mantiene una sola regola in AtomicReference.
 */
public final class InMemoryRegoleTempisticheDAO implements RegoleTempisticheDAO {
    private final java.util.concurrent.atomic.AtomicReference<RegoleTempistiche> ref = new java.util.concurrent.atomic.AtomicReference<>();
    public InMemoryRegoleTempisticheDAO() { 
          /*Nota: costruttore no-args intenzionalmente vuoto.
     * Questa implementazione In-Memory non richiede bootstrap o risorse esterne:
     * lo store è già inizializzato con una AtomicReference.
     * Il costruttore deve restare pubblico per l'uso tramite factory/reflection
     * nei test e nel wiring del progetto.
     */
    }
    @Override public RegoleTempistiche get() { return ref.get(); } // vedi Nota 2 sotto
    @Override public void save(RegoleTempistiche r) { ref.set(r); }
   
}
