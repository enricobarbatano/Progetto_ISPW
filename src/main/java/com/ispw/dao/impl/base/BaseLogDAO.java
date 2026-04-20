package com.ispw.dao.impl.base;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

/**
 * Base concrete SystemLog DAO implementing cache-first and append-only semantics.
 */
public class BaseLogDAO implements LogDAO {

    private static final Comparator<SystemLog> ORDER_BY_TS_DESC_ID_DESC =
            Comparator.comparing(SystemLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(SystemLog::getIdLog)
                      .reversed();

    protected final Map<Integer, SystemLog> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BaseLogDAO() { this(false); }

    protected BaseLogDAO(boolean persistent) { this.persistent = persistent; }

    // raw hooks
    protected SystemLog rawLoad(Integer id) { return null; }
    protected void rawAppend(SystemLog log) { }
    protected List<SystemLog> rawFindByUtente(int idUtente) { return null; }
    protected List<SystemLog> rawFindLast(int limit) { return null; }

    @Override
    public void append(SystemLog log) {
        if (log == null) throw new IllegalArgumentException("log non può essere null");

        // ensure timestamp
        if (log.getTimestamp() == null) log.setTimestamp(LocalDateTime.now());

        if (persistent) {
            rawAppend(log); // may set id via generated keys
            int id = log.getIdLog();
            if (id <= 0) {
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    log.setIdLog(next);
                    cache.put(next, log);
                } finally { lock.writeLock().unlock(); }
            } else {
                lock.writeLock().lock();
                try { cache.put(id, log); } finally { lock.writeLock().unlock(); }
            }
            return;
        }

        // IN_MEMORY
        lock.writeLock().lock();
        try {
            if (log.getIdLog() == 0) {
                int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                log.setIdLog(next);
            }
            cache.put(log.getIdLog(), log);
        } finally { lock.writeLock().unlock(); }
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        if (persistent) {
            List<SystemLog> res = rawFindByUtente(idUtente);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_TS_DESC_ID_DESC);
            lock.writeLock().lock();
            try { for (SystemLog l : res) if (l != null) cache.put(l.getIdLog(), l); } finally { lock.writeLock().unlock(); }
            return res;
        }

        lock.readLock().lock();
        try {
            var out = new ArrayList<SystemLog>();
            for (SystemLog l : cache.values()) { if (l != null && l.getIdUtenteCoinvolto() == idUtente) out.add(l); }
            out.sort(ORDER_BY_TS_DESC_ID_DESC);
            return out;
        } finally { lock.readLock().unlock(); }
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        final int safeLimit = Math.max(1, limit);
        if (persistent) {
            List<SystemLog> res = rawFindLast(safeLimit);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_TS_DESC_ID_DESC);
            lock.writeLock().lock();
            try { for (SystemLog l : res) if (l != null) cache.put(l.getIdLog(), l); } finally { lock.writeLock().unlock(); }
            return res;
        }

        lock.readLock().lock();
        try {
            var out = new ArrayList<SystemLog>(cache.values());
            out.sort(ORDER_BY_TS_DESC_ID_DESC);
            return out.stream().limit(safeLimit).toList();
        } finally { lock.readLock().unlock(); }
    }

    public SystemLog load(Integer id) {
        if (id == null || id <= 0) return null;
        lock.readLock().lock();
        try { SystemLog cached = cache.get(id); if (cached != null) return cached; } finally { lock.readLock().unlock(); }

        if (persistent) {
            SystemLog l = rawLoad(id);
            if (l != null) {
                lock.writeLock().lock();
                try { cache.put(l.getIdLog(), l); } finally { lock.writeLock().unlock(); }
            }
            return l;
        }
        return null;
    }

    public void store(SystemLog entity) { append(entity); }

    /** Append-only: delete not allowed */
    public void delete(Integer id) { throw new UnsupportedOperationException("SystemLog è append-only: delete non consentita"); }

    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;
        lock.readLock().lock();
        try { if (cache.containsKey(id)) return true; } finally { lock.readLock().unlock(); }
        if (persistent) {
            SystemLog l = rawLoad(id);
            if (l != null) {
                lock.writeLock().lock();
                try { cache.put(l.getIdLog(), l); } finally { lock.writeLock().unlock(); }
                return true;
            }
        }
        return false;
    }

    public SystemLog create(Integer id) {
        SystemLog l = new SystemLog();
        l.setIdLog(id != null ? id : 0);
        l.setTimestamp(LocalDateTime.now());
        return l;
    }

    /**
     * Compatibilità: pulisce la cache (usato dai test tramite reflection).
     */
    public void clear() {
        lock.writeLock().lock();
        try { cache.clear(); } finally { lock.writeLock().unlock(); }
    }
}
