package com.ispw.dao.impl.memory.concrete;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

public final class InMemoryGestoreDAO extends In_MemoryDAO<Integer, Gestore> implements GestoreDAO {

    public InMemoryGestoreDAO() {
        super(true); // store condiviso tra eventuali istanze
    }

    @Override
    protected Integer getId(Gestore entity) {
        return entity != null ? entity.getIdUtente() : 0; // chiave = idUtente (coerente con FileSystem/DBMS)
    }

    @Override
    public Gestore findById(int idGestore) {
        return load(idGestore);
    }

    @Override
    public Gestore findByEmail(String email) {
        if (email == null) return null;
        final String norm = email.trim().toLowerCase(Locale.ROOT);
        return snapshotValues().stream()
                .filter(g -> g != null
                        && g.getEmail() != null
                        && g.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        final Gestore g = load(idGestore);
        if (g == null || g.getPermessi() == null) return Set.of();
        // l’entity espone List<Permesso>; ritorno un Set “read-only” come copia difensiva
        return Set.copyOf(g.getPermessi());
    }

    @Override
    public boolean hasPermesso(int idGestore, Permesso permesso) {
        final Gestore g = load(idGestore);
        return g != null && g.getPermessi() != null && g.getPermessi().contains(permesso);
    }

    @Override
    public void assegnaPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");
        final Gestore g = load(idGestore);
        if (g == null || g.getPermessi() == null) return;
        if (!g.getPermessi().contains(permesso)) {
            g.getPermessi().add(permesso);
            // opzionale: store(g) per rendere esplicita la persistenza sulla mappa (in-memory è per riferimento)
            store(g);
        }
    }

    @Override
    public void rimuoviPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");
        final Gestore g = load(idGestore);
        if (g == null || g.getPermessi() == null) return;
        if (g.getPermessi().remove(permesso)) {
            store(g);
        }
    }
}