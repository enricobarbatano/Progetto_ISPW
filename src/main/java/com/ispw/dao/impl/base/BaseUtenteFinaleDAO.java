package com.ispw.dao.impl.base;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
 * - persistent == TRUE  : subclass is persistent (DBMS/FS) and should use raw* I/O
 * - persistent == FALSE : pure IN_MEMORY (no seed, no raw I/O)
 * - persistent == NULL  : IN_MEMORY seeded (load once from seed/utenti_finali.json into cache; never persist back)
 *
 * DBMS/FileSystem subclasses should extend this class and call super(Boolean.TRUE).
 */
public class BaseUtenteFinaleDAO implements UtenteFinaleDAO {

    protected final Map<Integer, UtenteFinale> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // ✅ Tri-state: null => seed-enabled in-memory
    private final Boolean persistent;

    // Seed-on-first-use state (only used when persistent == null)
    private volatile boolean seeded = false;
    private volatile int nextId = 1;

    /** Default: IN_MEMORY seeded (persistent == null) */
    public BaseUtenteFinaleDAO() { this(null); }

    /** Protected constructor for subclasses */
    protected BaseUtenteFinaleDAO(Boolean persistent) { this.persistent = persistent; }

    // -----------------------
    // Protected raw operations
    // Subclasses override these to provide DB/FS I/O.
    // Default implementations are no-op / null and are valid for IN_MEMORY base.
    // -----------------------
    protected UtenteFinale rawLoad(Integer id) { return null; }
    protected void rawStore(UtenteFinale entity) { }
    protected void rawDelete(Integer id) { }
    protected UtenteFinale rawFindByEmail(String email) { return null; }
    protected List<UtenteFinale> rawFindAll() { return null; }

    // -----------------------
    // Seed logic (ONLY when persistent == null)
    // -----------------------
    private void ensureSeeded() {
        // Seed ONLY in the specific mode requested: persistent == null
        if (persistent != null) return;
        if (seeded) return;

        lock.writeLock().lock();
        try {
            if (seeded) return; // double-check
            if (!cache.isEmpty()) {
                recomputeNextIdUnsafe();
                seeded = true;
                return;
            }

            List<UtenteFinale> initial = readSeedUtentiFinali();
            if (initial != null) {
                for (UtenteFinale u : initial) {
                    if (u == null) continue;
                    int id = u.getIdUtente();
                    if (id <= 0) continue;

                    // normalizza email in cache (evita mismatch nei findByEmail)
                    if (u.getEmail() != null) {
                        u.setEmail(normalizeEmail(u.getEmail()));
                    }
                    cache.put(id, u);
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

    private List<UtenteFinale> readSeedUtentiFinali() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("utenti_finali.json");
            if (!Files.exists(file)) return List.of();

            ObjectMapper om = new ObjectMapper();
            // utile se UtenteFinale contiene LocalDate/LocalDateTime (anche se non ci sono, è safe)
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, UtenteFinale.class);

            return om.readValue(file.toFile(), listType);
        } catch (Exception ex) {
            // best-effort: se seed fallisce, parti vuoto
            return List.of();
        }
    }

    // -----------------------
    // DAO interface implementations (cache-first template)
    // -----------------------
    @Override
    public UtenteFinale load(Integer id) {
        if (id == null || id <= 0) return null;

        ensureSeeded();

        lock.readLock().lock();
        try {
            UtenteFinale cached = cache.get(id);
            if (cached != null) return cached;
        } finally { lock.readLock().unlock(); }

        if (Boolean.TRUE.equals(persistent)) {
            UtenteFinale u = rawLoad(id);
            if (u != null && u.getIdUtente() > 0) {
                if (u.getEmail() != null) u.setEmail(normalizeEmail(u.getEmail()));
                lock.writeLock().lock();
                try { cache.put(u.getIdUtente(), u); } finally { lock.writeLock().unlock(); }
            }
            return u;
        }
        return null;
    }

    @Override
    public List<UtenteFinale> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            List<UtenteFinale> res = rawFindAll();
            if (res == null) return new ArrayList<>();

            // normalizza + cache update
            lock.writeLock().lock();
            try {
                for (UtenteFinale u : res) {
                    if (u != null && u.getIdUtente() > 0) {
                        if (u.getEmail() != null) u.setEmail(normalizeEmail(u.getEmail()));
                        cache.put(u.getIdUtente(), u);
                    }
                }
            } finally { lock.writeLock().unlock(); }

            // opzionale: ordine deterministico
            res.sort(Comparator.comparingInt(UtenteFinale::getIdUtente));
            return res;
        }

        lock.readLock().lock();
        try {
            List<UtenteFinale> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(UtenteFinale::getIdUtente));
            return out;
        } finally { lock.readLock().unlock(); }
    }

    @Override
    public void store(UtenteFinale entity) {
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
            UtenteFinale u = rawLoad(id);
            if (u != null) {
                if (u.getEmail() != null) u.setEmail(normalizeEmail(u.getEmail()));
                if (u.getIdUtente() > 0) {
                    lock.writeLock().lock();
                    try { cache.put(u.getIdUtente(), u); } finally { lock.writeLock().unlock(); }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public UtenteFinale create(Integer id) {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    @Override
    public UtenteFinale findById(int idUtente) { return load(idUtente); }

    @Override
    public UtenteFinale findByEmail(String email) {
        ensureSeeded();

        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        lock.readLock().lock();
        try {
            Optional<UtenteFinale> fromCache = cache.values().stream()
                .filter(u -> {
                    if (u == null) return false;
                    String e = u.getEmail();
                    if (e == null) return false;
                    String en = normalizeEmail(e);
                    return en != null && en.equals(norm);
                })
                .findFirst();
            if (fromCache.isPresent()) return fromCache.get();
        } finally { lock.readLock().unlock(); }

        if (Boolean.TRUE.equals(persistent)) {
            UtenteFinale u = rawFindByEmail(norm);
            if (u != null && u.getIdUtente() > 0) {
                if (u.getEmail() != null) u.setEmail(normalizeEmail(u.getEmail()));
                lock.writeLock().lock();
                try { cache.put(u.getIdUtente(), u); } finally { lock.writeLock().unlock(); }
            }
            return u;
        }
        return null;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Compatibilità: pulisce la cache (usato dai test tramite reflection).
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