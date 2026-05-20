package com.ispw.dao.impl.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

/**
 * Base concrete Penalita DAO implementing cache-first behavior.
 */
public class BasePenalitaDAO implements PenalitaDAO {

    private static final Comparator<Penalita> ORDER_BY_DATA_DESC_ID_DESC =
            Comparator.comparing(Penalita::getDataEmissione, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Penalita::getIdPenalita)
                      .reversed();

    protected final Map<Integer, Penalita> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BasePenalitaDAO() { this(false); }

    protected BasePenalitaDAO(boolean persistent) { this.persistent = persistent; }

    // raw hooks
    @SuppressWarnings("java:S1172")
    protected Penalita rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(Penalita entity) {
        // no-op: base in-memory implementation
    }

    protected void rawDelete(Integer id) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected List<Penalita> rawFindByUtente(int idUtente) {
        return List.of();
    }

    @Override
    public Penalita load(Integer id) {
        if (id == null || id <= 0) return null;
        lock.readLock().lock();
        try { Penalita cached = cache.get(id); if (cached != null) return cached; }
        finally { lock.readLock().unlock(); }

        if (persistent) {
            Penalita p = rawLoad(id);
            if (p != null) {
                lock.writeLock().lock();
                try { cache.put(p.getIdPenalita(), p); } finally { lock.writeLock().unlock(); }
            }
            return p;
        }
        return null;
    }

    @Override
    public void store(Penalita entity) {
        if (entity == null) return;
        if (entity.getIdPenalita() == 0) {
            if (persistent) {
                rawStore(entity);
                int id = entity.getIdPenalita();
                if (id <= 0) {
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdPenalita(next);
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
                    entity.setIdPenalita(next);
                    cache.put(next, entity);
                } finally { lock.writeLock().unlock(); }
                return;
            }
        }

        int id = entity.getIdPenalita();
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
            Penalita p = rawLoad(id);
            if (p != null) {
                lock.writeLock().lock();
                try { cache.put(p.getIdPenalita(), p); } finally { lock.writeLock().unlock(); }
                return true;
            }
        }
        return false;
    }

    @Override
    public Penalita create(Integer id) {
        Penalita p = new Penalita();
        if (id != null && id > 0) {
            p.setIdPenalita(id);
        }
        return p;
    }

    @Override
    public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        if (persistent) {
            List<Penalita> res = rawFindByUtente(idUtente);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_DATA_DESC_ID_DESC);
            lock.writeLock().lock();
            try { for (Penalita p : res) if (p != null) cache.put(p.getIdPenalita(), p); } finally { lock.writeLock().unlock(); }
            return res;
        }

        lock.readLock().lock();
        try {
            var out = new ArrayList<Penalita>();
            for (Penalita p : cache.values()) { if (p != null && p.getIdUtente() == idUtente) out.add(p); }
            out.sort(ORDER_BY_DATA_DESC_ID_DESC);
            return out;
        } finally { lock.readLock().unlock(); }
    }
}
