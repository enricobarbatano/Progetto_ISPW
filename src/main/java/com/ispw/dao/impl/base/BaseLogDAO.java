package com.ispw.dao.impl.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
 * Responsabilità:
 * - cache-first;
 * - append-only;
 * - seed solo in modalità IN_MEMORY seeded.
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
    private final AtomicInteger nextId = new AtomicInteger(1);

    public BaseLogDAO() {
        this(null);
    }

    protected BaseLogDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // ---------- RAW HOOKS ----------

    @SuppressWarnings("java:S1172")
    protected SystemLog rawLoad(Integer id) {
        return null;
    }

    protected void rawAppend(SystemLog log) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected List<SystemLog> rawFindByUtente(int idUtente) {
        return List.of();
    }

    @SuppressWarnings("java:S1172")
    protected List<SystemLog> rawFindLast(int limit) {
        return List.of();
    }

    // ---------- Seed logic ----------

    private void ensureSeeded() {
        if (persistent != null) return;
        if (seeded) return;

        lock.writeLock().lock();
        try {
            if (seeded) return;

            if (!cache.isEmpty()) {
                markSeededUnsafe();
                return;
            }

            for (SystemLog log : readSeedLogs()) {
                addSeedLogIfValid(log);
            }

            markSeededUnsafe();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void markSeededUnsafe() {
        recomputeNextIdUnsafe();
        seeded = true;
    }

    private void addSeedLogIfValid(SystemLog log) {
        if (log != null && log.getIdLog() > 0) {
            cache.put(log.getIdLog(), log);
        }
    }

    private void recomputeNextIdUnsafe() {
        int max = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        int computedNextId = max + 1;
        if (computedNextId <= 0) {
            computedNextId = 1;
        }
        nextId.set(computedNextId);
    }

    private List<SystemLog> readSeedLogs() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("system_log.json");

            if (!Files.exists(file)) {
                return List.of();
            }

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, SystemLog.class);

            List<SystemLog> result = om.readValue(file.toFile(), listType);
            return result != null ? result : List.of();
        } catch (IOException ex) {
            return List.of();
        }
    }

    // ---------- API ----------

    @Override
    public void append(SystemLog log) {
        if (log == null) {
            throw new IllegalArgumentException("log non può essere null");
        }

        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
        }

        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            rawAppend(log);

            int id = log.getIdLog();
            if (id <= 0) {
                id = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                log.setIdLog(id);
            }

            cache.put(id, log);
            return;
        }

        int id = log.getIdLog();

        if (id == 0) {
            lock.writeLock().lock();
            try {
                if (nextId.get() <= 0) {
                    recomputeNextIdUnsafe();
                }
                id = nextId.getAndIncrement();
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
            List<SystemLog> result = rawFindByUtente(idUtente);
            if (result == null) return new ArrayList<>();

            result.sort(ORDER_BY_TS_DESC_ID_DESC);

            for (SystemLog log : result) {
                cache.put(log.getIdLog(), log);
            }

            return result;
        }

        List<SystemLog> out = new ArrayList<>();

        for (SystemLog log : cache.values()) {
            if (log != null && log.getIdUtenteCoinvolto() == idUtente) {
                out.add(log);
            }
        }

        out.sort(ORDER_BY_TS_DESC_ID_DESC);
        return out;
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        ensureSeeded();

        int safeLimit = Math.max(1, limit);

        if (Boolean.TRUE.equals(persistent)) {
            List<SystemLog> result = rawFindLast(safeLimit);
            if (result == null) return new ArrayList<>();

            result.sort(ORDER_BY_TS_DESC_ID_DESC);

            for (SystemLog log : result) {
                cache.put(log.getIdLog(), log);
            }

            return result;
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
            SystemLog log = rawLoad(id);

            if (log != null) {
                cache.put(log.getIdLog(), log);
            }

            return log;
        }

        return null;
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            seeded = false;
            nextId.set(1);
        } finally {
            lock.writeLock().unlock();
        }
    }
}