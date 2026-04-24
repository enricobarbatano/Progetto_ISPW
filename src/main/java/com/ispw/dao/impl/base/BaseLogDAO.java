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
 * Base concreta del DAO SystemLog.
 *
 * - cache-first
 * - append-only
 * - IN_MEMORY se persistent=false
 * - DBMS / FS se persistent=true
 */
public class BaseLogDAO implements LogDAO {

    protected static final Comparator<SystemLog> ORDER_BY_TS_DESC_ID_DESC =
            Comparator.comparing(SystemLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(SystemLog::getIdLog)
                      .reversed();

    protected final Map<Integer, SystemLog> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BaseLogDAO() { this(false); }
    protected BaseLogDAO(boolean persistent) { this.persistent = persistent; }

    // ---------- RAW HOOKS ----------
    protected SystemLog rawLoad(Integer id) { return null; }
    protected void rawAppend(SystemLog log) { }
    protected List<SystemLog> rawFindByUtente(int idUtente) { return null; }
    protected List<SystemLog> rawFindLast(int limit) { return null; }

    // ---------- API ----------
    @Override
    public void append(SystemLog log) {
        if (log == null) throw new IllegalArgumentException("log non può essere null");
        if (log.getTimestamp() == null) log.setTimestamp(LocalDateTime.now());

        if (persistent) {
            rawAppend(log); // DBMS/FS assegna id se possibile
            int id = log.getIdLog();
            if (id <= 0) {
                id = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                log.setIdLog(id);
            }
            cache.put(id, log);
            return;
        }

        // IN_MEMORY
        int id = log.getIdLog();
        if (id == 0) {
            id = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            log.setIdLog(id);
        }
        cache.put(id, log);
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        if (persistent) {
            List<SystemLog> res = rawFindByUtente(idUtente);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_TS_DESC_ID_DESC);
            for (SystemLog l : res) cache.put(l.getIdLog(), l);
            return res;
        }

        List<SystemLog> out = new ArrayList<>();
        for (SystemLog l : cache.values()) {
            if (l != null && l.getIdUtenteCoinvolto() == idUtente) out.add(l);
        }
        out.sort(ORDER_BY_TS_DESC_ID_DESC);
        return out;
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        int safeLimit = Math.max(1, limit);

        if (persistent) {
            List<SystemLog> res = rawFindLast(safeLimit);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_TS_DESC_ID_DESC);
            for (SystemLog l : res) cache.put(l.getIdLog(), l);
            return res;
        }

        return cache.values().stream()
                .sorted(ORDER_BY_TS_DESC_ID_DESC)
                .limit(safeLimit)
                .toList();
    }

    public SystemLog load(Integer id) {
        if (id == null || id <= 0) return null;

        SystemLog cached = cache.get(id);
        if (cached != null) return cached;

        if (persistent) {
            SystemLog l = rawLoad(id);
            if (l != null) cache.put(l.getIdLog(), l);
            return l;
        }
        return null;
    }

    public void clear() {
        cache.clear();
    }
}