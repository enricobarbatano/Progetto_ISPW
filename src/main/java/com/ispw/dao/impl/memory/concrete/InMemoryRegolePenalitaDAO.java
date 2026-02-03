package com.ispw.dao.impl.memory.concrete;



import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

public final class InMemoryRegolePenalitaDAO implements RegolePenalitaDAO {
    private final java.util.concurrent.atomic.AtomicReference<RegolePenalita> ref = new java.util.concurrent.atomic.AtomicReference<>();
    public InMemoryRegolePenalitaDAO() { 
        
    /*Nota: costruttore no-args intenzionalmente vuoto.
     * Questa implementazione In-Memory non richiede bootstrap o risorse esterne:
     * lo store è già inizializzato con una AtomicReference.
     * Il costruttore deve restare pubblico per l'uso tramite factory/reflection
     * nei test e nel wiring del progetto.
     */

    }
    @Override public RegolePenalita get() { return ref.get(); }   // vedi Nota 2 sotto
    @Override public void save(RegolePenalita r) { ref.set(r); }
    
}

