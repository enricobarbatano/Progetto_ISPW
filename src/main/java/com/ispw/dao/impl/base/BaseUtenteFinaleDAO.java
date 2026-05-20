package com.ispw.dao.impl.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

/**
 * Base concrete UtenteFinale DAO implementing cache-first behavior.
 *
 * Tri-state persistence flag:
 * - TRUE  : DBMS/FS, usa raw*;
 * - FALSE : IN_MEMORY puro;
 * - NULL  : IN_MEMORY seeded.
 */
public class BaseUtenteFinaleDAO implements UtenteFinaleDAO {

    protected final Map<Integer, UtenteFinale> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Boolean persistent;

    private volatile boolean seeded = false;
    private final AtomicInteger nextId = new AtomicInteger(1);

    public BaseUtenteFinaleDAO() {
        this(null);
    }

    protected BaseUtenteFinaleDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS
    // -----------------------

    @SuppressWarnings("java:S1172")
    protected UtenteFinale rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(UtenteFinale entity) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected void rawDelete(Integer id) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected UtenteFinale rawFindByEmail(String email) {
        return null;
    }

    protected List<UtenteFinale> rawFindAll() {
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

            for (UtenteFinale utente : readSeedUtentiFinali()) {
                addSeedUtenteIfValid(utente);
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

    private void addSeedUtenteIfValid(UtenteFinale utente) {
        if (utente != null && utente.getIdUtente() > 0) {
            if (utente.getEmail() != null) {
                utente.setEmail(normalizeEmail(utente.getEmail()));
            }
            cache.put(utente.getIdUtente(), utente);
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

    private List<UtenteFinale> readSeedUtentiFinali() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("utenti_finali.json");

            if (!Files.exists(file)) {
                return List.of();
            }

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, UtenteFinale.class);

            List<UtenteFinale> result = om.readValue(file.toFile(), listType);
            return result != null ? result : List.of();
        } catch (IOException ex) {
            return List.of();
        }
    }

    // -----------------------
    // DAO API
    // -----------------------

    @Override
    public UtenteFinale load(Integer id) {
        if (id == null || id <= 0) return null;

        ensureSeeded();

        lock.readLock().lock();
        try {
            UtenteFinale cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            UtenteFinale utente = rawLoad(id);

            if (utente != null && utente.getIdUtente() > 0) {
                if (utente.getEmail() != null) {
                    utente.setEmail(normalizeEmail(utente.getEmail()));
                }

                lock.writeLock().lock();
                try {
                    cache.put(utente.getIdUtente(), utente);
                } finally {
                    lock.writeLock().unlock();
                }
            }

            return utente;
        }

        return null;
    }

    @Override
    public List<UtenteFinale> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            List<UtenteFinale> result = rawFindAll();

            if (result == null) {
                return new ArrayList<>();
            }

            lock.writeLock().lock();
            try {
                for (UtenteFinale utente : result) {
                    if (utente != null && utente.getIdUtente() > 0) {
                        if (utente.getEmail() != null) {
                            utente.setEmail(normalizeEmail(utente.getEmail()));
                        }
                        cache.put(utente.getIdUtente(), utente);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }

            result.sort(Comparator.comparingInt(UtenteFinale::getIdUtente));
            return result;
        }

        lock.readLock().lock();
        try {
            List<UtenteFinale> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(UtenteFinale::getIdUtente));
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void store(UtenteFinale entity) {
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
            UtenteFinale utente = rawLoad(id);

            if (utente != null) {
                if (utente.getEmail() != null) {
                    utente.setEmail(normalizeEmail(utente.getEmail()));
                }

                if (utente.getIdUtente() > 0) {
                    lock.writeLock().lock();
                    try {
                        cache.put(utente.getIdUtente(), utente);
                    } finally {
                        lock.writeLock().unlock();
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public UtenteFinale create(Integer id) {
        UtenteFinale utente = new UtenteFinale();
        utente.setIdUtente(id != null ? id : 0);
        return utente;
    }

    @Override
    public UtenteFinale findById(int idUtente) {
        return load(idUtente);
    }

    @Override
    public UtenteFinale findByEmail(String email) {
        ensureSeeded();

        final String normalized = normalizeEmail(email);
        if (normalized == null || normalized.isBlank()) return null;

        lock.readLock().lock();
        try {
            Optional<UtenteFinale> fromCache = cache.values().stream()
                    .filter(u -> {
                        if (u == null) return false;

                        String currentEmail = u.getEmail();
                        if (currentEmail == null) return false;

                        String normalizedCurrent = normalizeEmail(currentEmail);
                        return normalizedCurrent != null && normalizedCurrent.equals(normalized);
                    })
                    .findFirst();

            if (fromCache.isPresent()) {
                return fromCache.get();
            }
        } finally {
            lock.readLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            UtenteFinale utente = rawFindByEmail(normalized);

            if (utente != null && utente.getIdUtente() > 0) {
                if (utente.getEmail() != null) {
                    utente.setEmail(normalizeEmail(utente.getEmail()));
                }

                lock.writeLock().lock();
                try {
                    cache.put(utente.getIdUtente(), utente);
                } finally {
                    lock.writeLock().unlock();
                }
            }

            return utente;
        }

        return null;
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