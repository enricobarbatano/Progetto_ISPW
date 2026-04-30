package com.ispw.dao.impl.base;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

/**
 * Base concreta del DAO SystemLog.
 *
 * - cache-first
 * - append-only
 *
 * Tri-state persistence flag:
 * - persistent == TRUE  : DBMS/FS (rawAppend/rawFind*)
 * - persistent == FALSE : IN_MEMORY puro (no seed)
 * - persistent == NULL  : IN_MEMORY seeded (load once from seed/system_log.json; never persist back)
 */
public class BaseLogDAO implements LogDAO {

    protected static final Comparator<SystemLog> ORDER_BY_TS_DESC_ID_DESC =
            Comparator.comparing(SystemLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(SystemLog::getIdLog)
                      .reversed();

    protected final Map<Integer, SystemLog> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Boolean persistent;

    private volatile boolean seeded = false;
    private volatile int nextId = 1;

    /** Default: IN_MEMORY seeded (persistent == null) */
    public BaseLogDAO() { this(null); }
    protected BaseLogDAO(Boolean persistent) { this.persistent = persistent; }

    // ---------- RAW HOOKS ----------
    protected SystemLog rawLoad(Integer id) { return null; }
    protected void rawAppend(SystemLog log) { }
    protected List<SystemLog> rawFindByUtente(int idUtente) { return null; }
    protected List<SystemLog> rawFindLast(int limit) { return null; }

    // ---------- Seed logic (ONLY when persistent == null) ----------
    private void ensureSeeded() {
        if (persistent != null) return;
        if (seeded) return;

        lock.writeLock().lock();
        try {
            if (seeded) return;

            // ✅ prima controlla cache
            if (!cache.isEmpty()) {
                recomputeNextIdUnsafe();
                seeded = true;
                return;
            }

            List<SystemLog> initial = readSeedLogs();
            if (initial != null) {
                for (SystemLog l : initial) {
                    if (l == null) continue;
                    int id = l.getIdLog();
                    if (id <= 0) continue;
                    cache.put(id, l);
                }
            }

            recomputeNextIdUnsafe();
            seeded = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void recomputeNextIdUnsafe() {
        int max = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        nextId = max + 1;
        if (nextId <= 0) nextId = 1;
    }

    private List<SystemLog> readSeedLogs() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("system_log.json");
            if (!Files.exists(file)) return List.of();

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, SystemLog.class);

            return om.readValue(file.toFile(), listType);
        } catch (Exception ex) {
            return List.of(); // best-effort
        }
    }

    // ---------- API ----------
    @Override
    public void append(SystemLog log) {
        if (log == null) throw new IllegalArgumentException("log non può essere null");
        if (log.getTimestamp() == null) log.setTimestamp(LocalDateTime.now());

        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            rawAppend(log); // DBMS/FS assegna id se possibile
            int id = log.getIdLog();
            if (id <= 0) {
                id = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                log.setIdLog(id);
            }
            cache.put(id, log);
            return;
        }

        // IN_MEMORY (persistent == null or false)
        int id = log.getIdLog();
        if (id == 0) {
            lock.writeLock().lock();
            try {
                if (nextId <= 0) recomputeNextIdUnsafe();
                id = nextId++;
                log.setIdLog(id);
                cache.put(id, log);
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            cache.put(id, log);
        }
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
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
        ensureSeeded();

        int safeLimit = Math.max(1, limit);

        if (Boolean.TRUE.equals(persistent)) {
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
        ensureSeeded();

        if (id == null || id <= 0) return null;

        SystemLog cached = cache.get(id);
        if (cached != null) return cached;

        if (Boolean.TRUE.equals(persistent)) {
            SystemLog l = rawLoad(id);
            if (l != null) cache.put(l.getIdLog(), l);
            return l;
        }
        return null;
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            seeded = false;
            nextId = 1;
        } finally {
            lock.writeLock().unlock();
        }
    }
}