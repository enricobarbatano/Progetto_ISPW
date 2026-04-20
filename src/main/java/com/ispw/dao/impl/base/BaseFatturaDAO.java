package com.ispw.dao.impl.base;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

/**
 * Base concrete Fattura DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 */
public class BaseFatturaDAO implements FatturaDAO {

    private static final Comparator<Fattura> ORDER_BY_DATA_DESC_ID_DESC =
            Comparator.<Fattura, java.time.LocalDate>comparing(Fattura::getDataEmissione,
                    Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Fattura::getIdFattura)
                      .reversed();

    protected final Map<Integer, Fattura> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BaseFatturaDAO() { this(false); }

    protected BaseFatturaDAO(boolean persistent) { this.persistent = persistent; }

    // raw hooks
    protected Fattura rawLoad(Integer id) { return null; }
    protected void rawStore(Fattura entity) { }
    protected void rawDelete(Integer id) { }
    protected Fattura rawFindLastByUtente(int idUtente) { return null; }

    @Override
    public Fattura load(Integer id) {
        if (id == null || id <= 0) return null;
        lock.readLock().lock();
        try { Fattura cached = cache.get(id); if (cached != null) return cached; }
        finally { lock.readLock().unlock(); }

        if (persistent) {
            Fattura f = rawLoad(id);
            if (f != null) {
                lock.writeLock().lock();
                try { cache.put(f.getIdFattura(), f); } finally { lock.writeLock().unlock(); }
            }
            return f;
        }
        return null;
    }

    @Override
    public void store(Fattura entity) {
        if (entity == null) return;
        if (entity.getIdFattura() == 0) {
            if (persistent) {
                rawStore(entity);
                int id = entity.getIdFattura();
                if (id <= 0) {
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdFattura(next);
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
                    entity.setIdFattura(next);
                    cache.put(next, entity);
                } finally { lock.writeLock().unlock(); }
                return;
            }
        }

        int id = entity.getIdFattura();
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
            Fattura f = rawLoad(id);
            if (f != null) {
                lock.writeLock().lock();
                try { cache.put(f.getIdFattura(), f); } finally { lock.writeLock().unlock(); }
                return true;
            }
        }
        return false;
    }

    @Override
    public Fattura create(Integer id) { Fattura f = new Fattura(); if (id != null) f.setIdFattura(id); return f; }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        // STEP 1: try cache
        lock.readLock().lock();
        try {
            var fromCache = cache.values().stream()
                    .filter(f -> f != null && f.getIdUtente() == idUtente)
                    .sorted(ORDER_BY_DATA_DESC_ID_DESC)
                    .findFirst();
            if (fromCache.isPresent()) return fromCache.get();
        } finally { lock.readLock().unlock(); }

        // STEP 2: fallback to persistent store if configured
        if (persistent) {
            Fattura f = rawFindLastByUtente(idUtente);
            if (f != null && f.getIdFattura() > 0) {
                lock.writeLock().lock();
                try { cache.put(f.getIdFattura(), f); } finally { lock.writeLock().unlock(); }
            }
            return f;
        }

        return null;
    }
}
