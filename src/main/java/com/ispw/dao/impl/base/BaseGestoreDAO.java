package com.ispw.dao.impl.base;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

/**
 * Base concreta del DAO Gestore (cache-first).
 * Acts as IN_MEMORY provider when instantiated directly.
 */
public class BaseGestoreDAO implements GestoreDAO {

    protected final Map<Integer, Gestore> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BaseGestoreDAO() { this(false); }
    protected BaseGestoreDAO(boolean persistent) { this.persistent = persistent; }

    // -------- RAW HOOKS (DB/FS) --------
    protected Gestore rawLoad(Integer id) { return null; }
    protected void rawStore(Gestore entity) { }
    protected void rawDelete(Integer id) { }
    protected Gestore rawFindByEmail(String email) { return null; }

    // -------- DAO API --------

    @Override
    public Gestore load(Integer id) {
        if (id == null || id <= 0) return null;

        lock.readLock().lock();
        try {
            Gestore cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        if (persistent) {
            Gestore g = rawLoad(id);
            if (g != null && g.getIdUtente() > 0) {
                lock.writeLock().lock();
                try { cache.put(g.getIdUtente(), g); }
                finally { lock.writeLock().unlock(); }
            }
            return g;
        }
        return null;
    }

    @Override
    public void store(Gestore entity) {
        if (entity == null) return;

        if (entity.getIdUtente() == 0) {
            if (persistent) {
                rawStore(entity);
                int id = entity.getIdUtente();
                if (id <= 0) {
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdUtente(next);
                        cache.put(next, entity);
                    } finally { lock.writeLock().unlock(); }
                } else {
                    lock.writeLock().lock();
                    try { cache.put(id, entity); } finally { lock.writeLock().unlock(); }
                }
                return;
            } else {
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdUtente(next);
                    cache.put(next, entity);
                } finally { lock.writeLock().unlock(); }
                return;
            }
        }

        int id = entity.getIdUtente();
        lock.writeLock().lock();
        try { cache.put(id, entity); } finally { lock.writeLock().unlock(); }
        if (persistent) rawStore(entity);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;
        lock.writeLock().lock();
        try { cache.remove(id); } finally { lock.writeLock().unlock(); }
        if (persistent) rawDelete(id);
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;
        lock.readLock().lock();
        try { if (cache.containsKey(id)) return true; } finally { lock.readLock().unlock(); }

        if (persistent) {
            Gestore g = rawLoad(id);
            if (g != null && g.getIdUtente() > 0) {
                lock.writeLock().lock();
                try { cache.put(g.getIdUtente(), g); } finally { lock.writeLock().unlock(); }
                return true;
            }
        }
        return false;
    }

    @Override
    public Gestore create(Integer id) {
        Gestore g = new Gestore();
        g.setIdUtente(id != null ? id : 0);
        return g;
    }

    @Override
    public Gestore findById(int idGestore) {
        return load(idGestore);
    }

    @Override
    public Gestore findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        lock.readLock().lock();
        try {
            Optional<Gestore> fromCache = cache.values().stream()
                .filter(g -> g != null && g.getEmail() != null &&
                             normalizeEmail(g.getEmail()).equals(norm))
                .findFirst();
            if (fromCache.isPresent()) return fromCache.get();
        } finally { lock.readLock().unlock(); }

        if (persistent) {
            Gestore g = rawFindByEmail(norm);
            if (g != null && g.getIdUtente() > 0) {
                lock.writeLock().lock();
                try { cache.put(g.getIdUtente(), g); } finally { lock.writeLock().unlock(); }
            }
            return g;
        }
        return null;
    }

    // -------- Permessi --------

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        Gestore g = load(idGestore);
        return (g == null || g.getPermessi() == null) ? Set.of() : Set.copyOf(g.getPermessi());
    }

    @Override
    public boolean hasPermesso(int idGestore, Permesso permesso) {
        Gestore g = load(idGestore);
        return g != null && g.getPermessi() != null && g.getPermessi().contains(permesso);
    }

    @Override
    public void assegnaPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");
        Gestore g = load(idGestore);
        if (g == null || g.getPermessi() == null) return;
        if (!g.getPermessi().contains(permesso)) {
            g.getPermessi().add(permesso);
            store(g);
        }
    }

    @Override
    public void rimuoviPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");
        Gestore g = load(idGestore);
        if (g == null || g.getPermessi() == null) return;
        if (g.getPermessi().remove(permesso)) {
            store(g);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void clear() {
        lock.writeLock().lock();
        try { cache.clear(); } finally { lock.writeLock().unlock(); }
    }
}
