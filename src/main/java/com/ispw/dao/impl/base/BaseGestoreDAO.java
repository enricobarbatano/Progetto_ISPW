package com.ispw.dao.impl.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

/**
 * Base concreta del DAO Gestore.
 *
 * Responsabilità:
 * - comportamento cache-first;
 * - seed iniziale solo in modalità IN_MEMORY seeded;
 * - delega raw* ai provider persistenti.
 *
 * Tri-state persistence flag:
 * - TRUE  : DBMS/FS, usa raw*;
 * - FALSE : IN_MEMORY puro, senza seed;
 * - NULL  : IN_MEMORY seeded, carica seed/gestori.json una sola volta.
 */
public class BaseGestoreDAO implements GestoreDAO {

    protected final Map<Integer, Gestore> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Boolean persistent;

    private volatile boolean seeded = false;
    private final AtomicInteger nextId = new AtomicInteger(1);

    public BaseGestoreDAO() {
        this(null);
    }

    protected BaseGestoreDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -------- RAW HOOKS (DB/FS) --------

    @SuppressWarnings("java:S1172")
    protected Gestore rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(Gestore entity) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected void rawDelete(Integer id) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected Gestore rawFindByEmail(String email) {
        return null;
    }

    protected List<Gestore> rawFindAll() {
        return List.of();
    }

    // -----------------------
    // Seed logic
    // -----------------------

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

            for (Gestore gestore : readSeedGestori()) {
                addSeedGestoreIfValid(gestore);
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

    private void addSeedGestoreIfValid(Gestore gestore) {
        if (gestore != null && gestore.getIdUtente() > 0) {
            if (gestore.getEmail() != null) {
                gestore.setEmail(normalizeEmail(gestore.getEmail()));
            }
            cache.put(gestore.getIdUtente(), gestore);
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

    private List<Gestore> readSeedGestori() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("gestori.json");
            if (!Files.exists(file)) {
                return List.of();
            }

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, Gestore.class);

            List<Gestore> result = om.readValue(file.toFile(), listType);
            return result != null ? result : List.of();
        } catch (IOException ex) {
            return List.of();
        }
    }

    // -------- DAO API --------

    @Override
    public Gestore load(Integer id) {
        if (id == null || id <= 0) return null;

        ensureSeeded();

        lock.readLock().lock();
        try {
            Gestore cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            Gestore gestore = rawLoad(id);
            if (gestore != null && gestore.getIdUtente() > 0) {
                if (gestore.getEmail() != null) {
                    gestore.setEmail(normalizeEmail(gestore.getEmail()));
                }
                lock.writeLock().lock();
                try {
                    cache.put(gestore.getIdUtente(), gestore);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return gestore;
        }

        return null;
    }

    @Override
    public List<Gestore> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            List<Gestore> result = rawFindAll();
            if (result == null) return new ArrayList<>();

            lock.writeLock().lock();
            try {
                for (Gestore gestore : result) {
                    if (gestore != null && gestore.getIdUtente() > 0) {
                        if (gestore.getEmail() != null) {
                            gestore.setEmail(normalizeEmail(gestore.getEmail()));
                        }
                        cache.put(gestore.getIdUtente(), gestore);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }

            result.sort(Comparator.comparingInt(Gestore::getIdUtente));
            return result;
        }

        lock.readLock().lock();
        try {
            List<Gestore> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(Gestore::getIdUtente));
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void store(Gestore entity) {
        if (entity == null) return;

        ensureSeeded();

        if (entity.getEmail() != null) {
            entity.setEmail(normalizeEmail(entity.getEmail()));
        }

        if (entity.getIdUtente() == 0 && Boolean.TRUE.equals(persistent)) {
            rawStore(entity);
            int id = entity.getIdUtente();

            if (id <= 0) {
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdUtente(next);
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }
            } else {
                lock.writeLock().lock();
                try {
                    cache.put(id, entity);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return;
        }

        if (entity.getIdUtente() == 0) {
            lock.writeLock().lock();
            try {
                if (nextId.get() <= 0) {
                    recomputeNextIdUnsafe();
                }
                int id = nextId.getAndIncrement();
                entity.setIdUtente(id);
                cache.put(id, entity);
            } finally {
                lock.writeLock().unlock();
            }
            return;
        }

        int id = entity.getIdUtente();
        lock.writeLock().lock();
        try {
            cache.put(id, entity);
        } finally {
            lock.writeLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            rawStore(entity);
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;

        ensureSeeded();

        lock.writeLock().lock();
        try {
            cache.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            rawDelete(id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;

        ensureSeeded();

        lock.readLock().lock();
        try {
            if (cache.containsKey(id)) return true;
        } finally {
            lock.readLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            Gestore gestore = rawLoad(id);
            if (gestore != null && gestore.getIdUtente() > 0) {
                if (gestore.getEmail() != null) {
                    gestore.setEmail(normalizeEmail(gestore.getEmail()));
                }
                lock.writeLock().lock();
                try {
                    cache.put(gestore.getIdUtente(), gestore);
                } finally {
                    lock.writeLock().unlock();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public Gestore create(Integer id) {
        Gestore gestore = new Gestore();
        gestore.setIdUtente(id != null ? id : 0);
        return gestore;
    }

    @Override
    public Gestore findById(int idGestore) {
        return load(idGestore);
    }

    @Override
    public Gestore findByEmail(String email) {
        ensureSeeded();

        final String normalized = normalizeEmail(email);
        if (normalized == null || normalized.isBlank()) return null;

        lock.readLock().lock();
        try {
            Optional<Gestore> fromCache = cache.values().stream()
                    .filter(g -> g != null && g.getEmail() != null &&
                            normalizeEmail(g.getEmail()).equals(normalized))
                    .findFirst();

            if (fromCache.isPresent()) {
                return fromCache.get();
            }
        } finally {
            lock.readLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            Gestore gestore = rawFindByEmail(normalized);
            if (gestore != null && gestore.getIdUtente() > 0) {
                if (gestore.getEmail() != null) {
                    gestore.setEmail(normalizeEmail(gestore.getEmail()));
                }
                lock.writeLock().lock();
                try {
                    cache.put(gestore.getIdUtente(), gestore);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return gestore;
        }

        return null;
    }

    // -------- Permessi --------

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        Gestore gestore = load(idGestore);
        return (gestore == null || gestore.getPermessi() == null)
                ? Set.of()
                : Set.copyOf(gestore.getPermessi());
    }

    @Override
    public boolean hasPermesso(int idGestore, Permesso permesso) {
        Gestore gestore = load(idGestore);
        return gestore != null
                && gestore.getPermessi() != null
                && gestore.getPermessi().contains(permesso);
    }

    @Override
    public void assegnaPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");

        Gestore gestore = load(idGestore);
        if (gestore == null || gestore.getPermessi() == null) return;

        if (!gestore.getPermessi().contains(permesso)) {
            gestore.getPermessi().add(permesso);
            store(gestore);
        }
    }

    @Override
    public void rimuoviPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");

        Gestore gestore = load(idGestore);
        if (gestore == null || gestore.getPermessi() == null) return;

        if (gestore.getPermessi().remove(permesso)) {
            store(gestore);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
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