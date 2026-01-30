package com.ispw.dao.impl.memory.concrete;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

public class InMemoryGestoreDAO extends In_MemoryDAO<Integer, Gestore> implements GestoreDAO {

    // indice permessi separato (puoi anche metterli dentro Gestore se già li contiene)
    private final ConcurrentMap<Integer, Set<Permesso>> permessiByGestore = new ConcurrentHashMap<>();

    public InMemoryGestoreDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Gestore entity) {
        // TODO: return entity.getIdUtente() o getIdGestore();
        throw new UnsupportedOperationException("TODO: Gestore.getId...");
    }

    @Override
    public Gestore findById(int idGestore) {
        return load(idGestore);
    }

    @Override
    public Gestore findByEmail(String email) {
        throw new UnsupportedOperationException("TODO: findByEmail");
    }

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        return permessiByGestore.getOrDefault(idGestore, Set.of());
    }

    @Override
    public boolean hasPermesso(int idGestore, Permesso permesso) {
        return permessiByGestore.getOrDefault(idGestore, Set.of()).contains(permesso);
    }

    @Override
    public void assegnaPermesso(int idGestore, Permesso permesso) {
        permessiByGestore.compute(idGestore, (k, v) -> {
            Set<Permesso> s = (v == null) ? new HashSet<>() : new HashSet<>(v);
            s.add(permesso);
            return Set.copyOf(s);
        });
    }

    @Override
    public void rimuoviPermesso(int idGestore, Permesso permesso) {
        permessiByGestore.computeIfPresent(idGestore, (k, v) -> {
            Set<Permesso> s = new HashSet<>(v);
            s.remove(permesso);
            return Set.copyOf(s);
        });
    }
}
