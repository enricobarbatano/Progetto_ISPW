package com.ispw.dao.impl.base;

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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

/**
 * Base concreta del DAO Gestore (cache-first).
 *
 * Tri-state persistence flag:
 * - persistent == TRUE  : subclass is persistent (DBMS/FS) and should use raw* I/O
 * - persistent == FALSE : pure IN_MEMORY (no seed, no raw I/O)
 * - persistent == NULL  : IN_MEMORY seeded (load once from seed/gestori.json into cache; never persist back)
 *
 * DBMS/FileSystem subclasses should extend this class and call super(Boolean.TRUE).
 */
public class BaseGestoreDAO implements GestoreDAO {

    protected final Map<Integer, Gestore> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // ✅ Tri-state: null => seed-enabled in-memory
    private final Boolean persistent;

    // Seed-on-first-use state (only used when persistent == null)
    private volatile boolean seeded = false;
    private volatile int nextId = 1;

    /** Default: IN_MEMORY seeded (persistent == null) */
    public BaseGestoreDAO() { this(null); }

    /** Protected constructor for subclasses */
    protected BaseGestoreDAO(Boolean persistent) { this.persistent = persistent; }

    // -------- RAW HOOKS (DB/FS) --------
    protected Gestore rawLoad(Integer id) { return null; }
    protected void rawStore(Gestore entity) { }
    protected void rawDelete(Integer id) { }
    protected Gestore rawFindByEmail(String email) { return null; }
    protected List<Gestore> rawFindAll() { return null; }

    // -----------------------
    // Seed logic (ONLY when persistent == null)
    // -----------------------
    private void ensureSeeded() {
        // Seed ONLY when persistent == null
        if (persistent != null) return;
        if (seeded) return;

        lock.writeLock().lock();
        try {
            if (seeded) return; // double-check
            // ✅ prima controlla la cache: se non è vuota, NON caricare seed
            if (!cache.isEmpty()) {
                recomputeNextIdUnsafe();
                seeded = true;
                return;
            }

            List<Gestore> initial = readSeedGestori();
            if (initial != null) {
                for (Gestore g : initial) {
                    if (g == null) continue;
                    int id = g.getIdUtente();
                    if (id <= 0) continue;

                    // normalizza email per coerenza con findByEmail
                    if (g.getEmail() != null) {
                        g.setEmail(normalizeEmail(g.getEmail()));
                    }
                    cache.put(id, g);
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

    private List<Gestore> readSeedGestori() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("gestori.json");
            if (!Files.exists(file)) return List.of();

            ObjectMapper om = new ObjectMapper();
            // Safe even if Gestore doesn't have Java time fields
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, Gestore.class);

            return om.readValue(file.toFile(), listType);
        } catch (Exception ex) {
            // best-effort: se seed fallisce, parti vuoto
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
            Gestore g = rawLoad(id);
            if (g != null && g.getIdUtente() > 0) {
                if (g.getEmail() != null) g.setEmail(normalizeEmail(g.getEmail()));
                lock.writeLock().lock();
                try { cache.put(g.getIdUtente(), g); }
                finally { lock.writeLock().unlock(); }
            }
            return g;
        }
        return null;
    }

    @Override
    public List<Gestore> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            List<Gestore> res = rawFindAll();
            if (res == null) return new ArrayList<>();

            // normalizza + cache update
            lock.writeLock().lock();
            try {
                for (Gestore g : res) {
                    if (g != null && g.getIdUtente() > 0) {
                        if (g.getEmail() != null) g.setEmail(normalizeEmail(g.getEmail()));
                        cache.put(g.getIdUtente(), g);
                    }
                }
            } finally { lock.writeLock().unlock(); }

            res.sort(Comparator.comparingInt(Gestore::getIdUtente));
            return res;
        }

        lock.readLock().lock();
        try {
            List<Gestore> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(Gestore::getIdUtente));
            return out;
        } finally { lock.readLock().unlock(); }
    }

    @Override
    public void store(Gestore entity) {
        if (entity == null) return;

        ensureSeeded();

        // normalizza email in ingresso
        if (entity.getEmail() != null) {
            entity.setEmail(normalizeEmail(entity.getEmail()));
        }

        // Persistent provider: delegate id generation to rawStore if id==0
        if (entity.getIdUtente() == 0 && Boolean.TRUE.equals(persistent)) {
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
        }

        // IN_MEMORY (persistent == null or false): assign new id if needed
        if (entity.getIdUtente() == 0) {
            lock.writeLock().lock();
            try {
                if (nextId <= 0) recomputeNextIdUnsafe();
                int id = nextId++;
                entity.setIdUtente(id);
                cache.put(id, entity);
            } finally { lock.writeLock().unlock(); }
            return;
        }

        int id = entity.getIdUtente();
        lock.writeLock().lock();
        try { cache.put(id, entity); } finally { lock.writeLock().unlock(); }

        if (Boolean.TRUE.equals(persistent)) rawStore(entity);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;

        ensureSeeded();

        lock.writeLock().lock();
        try { cache.remove(id); } finally { lock.writeLock().unlock(); }
        if (Boolean.TRUE.equals(persistent)) rawDelete(id);
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;

        ensureSeeded();

        lock.readLock().lock();
        try { if (cache.containsKey(id)) return true; } finally { lock.readLock().unlock(); }

        if (Boolean.TRUE.equals(persistent)) {
            Gestore g = rawLoad(id);
            if (g != null && g.getIdUtente() > 0) {
                if (g.getEmail() != null) g.setEmail(normalizeEmail(g.getEmail()));
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
        ensureSeeded();

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

        if (Boolean.TRUE.equals(persistent)) {
            Gestore g = rawFindByEmail(norm);
            if (g != null && g.getIdUtente() > 0) {
                if (g.getEmail() != null) g.setEmail(normalizeEmail(g.getEmail()));
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

    /**
     * Compatibilità: pulisce la cache (usato dai test).
     * NEW: resetta anche lo stato di seeding + nextId.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            seeded = false;
            nextId = 1;
        } finally { lock.writeLock().unlock(); }
    }
}