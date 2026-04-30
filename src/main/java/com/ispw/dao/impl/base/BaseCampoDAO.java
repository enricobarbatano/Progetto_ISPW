package com.ispw.dao.impl.base;

import java.nio.file.Files;
import java.nio.file.Path;
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
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

/**
 * Base concrete Campo DAO implementing cache-first behavior.
 *
 * Tri-state persistence flag:
 * - persistent == TRUE  : subclass is persistent (DBMS/FS) and should use raw* I/O
 * - persistent == FALSE : pure IN_MEMORY (no seed, no raw I/O)
 * - persistent == NULL  : IN_MEMORY seeded (load once from seed/campi.json into cache; never persist back)
 *
 * DBMS/FileSystem subclasses should extend this class and call super(Boolean.TRUE).
 */
public class BaseCampoDAO implements CampoDAO {

    protected final Map<Integer, Campo> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // ✅ Tri-state: null => seed-enabled in-memory
    private final Boolean persistent;

    // Seed-on-first-use state (only used when persistent == null)
    private volatile boolean seeded = false;
    private volatile int nextId = 1;

    /** Default: IN_MEMORY seeded (persistent == null) */
    public BaseCampoDAO() { this(null); }

    /** Protected constructor for subclasses */
    protected BaseCampoDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // Protected raw operations
    // Subclasses override these to provide DB/FS I/O.
    // Default implementations are no-op / null and are valid for IN_MEMORY base.
    // -----------------------
    protected Campo rawLoad(Integer id) { return null; }
    protected void rawStore(Campo entity) { }
    protected void rawDelete(Integer id) { }
    protected List<Campo> rawFindAll() { return null; }

    // -----------------------
    // Seed logic (ONLY when persistent == null)
    //Se la cache è vuota e persistent == null, allora carica i dati iniziali dai JSON nella cartella seed/ dentro la cache, una sola volta.
    //Dopo di che l’app lavora solo in RAM e non scrive mai su disco.
    // -----------------------
    private void ensureSeeded() {
        // Seed ONLY in the specific mode requested: persistent == null
        if (persistent != null) return;
        if (seeded) return;

        lock.writeLock().lock();
        try {
            if (seeded) return; 
            if (!cache.isEmpty()) {
                recomputeNextIdUnsafe();
                seeded = true;
                return;
            }

            List<Campo> initial = readSeedCampi();
            if (initial != null) {
                for (Campo c : initial) {
                    if (c == null) continue;
                    int id = c.getIdCampo();
                    if (id <= 0) continue;
                    cache.put(id, c);
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

    private List<Campo> readSeedCampi() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("campi.json");
            if (!Files.exists(file)) return List.of();

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, Campo.class);

            return om.readValue(file.toFile(), listType);
        } catch (Exception ex) {
            // best-effort: if seed fails, start empty
            return List.of();
        }
    }

    // -----------------------
    // DAO interface implementations (cache-first template)
    // -----------------------
    @Override
    public Campo load(Integer id) {
        if (id == null || id <= 0) return null;

        ensureSeeded();

        lock.readLock().lock();
        try {
            Campo cached = cache.get(id);
            if (cached != null) return cached;
        } finally { lock.readLock().unlock(); }

        // Persistent providers use raw I/O
        if (Boolean.TRUE.equals(persistent)) {
            Campo c = rawLoad(id);
            if (c != null) {
                lock.writeLock().lock();
                try { cache.put(c.getIdCampo(), c); } finally { lock.writeLock().unlock(); }
            }
            return c;
        }

        // persistent == FALSE or NULL but not found in cache => not found
        return null;
    }

    @Override
    public void store(Campo entity) {
        if (entity == null) return;

        ensureSeeded();

        // If persistent provider: delegate id generation to rawStore if id==0
        if (entity.getIdCampo() == 0 && Boolean.TRUE.equals(persistent)) {
            rawStore(entity); // expected to set id when inserted
            int id = entity.getIdCampo();

            if (id <= 0) {
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdCampo(next);
                    cache.put(next, entity);
                } finally { lock.writeLock().unlock(); }
            } else {
                lock.writeLock().lock();
                try { cache.put(id, entity); } finally { lock.writeLock().unlock(); }
            }
            return;
        }

        // IN_MEMORY (persistent == null or false): assign new id if needed
        if (entity.getIdCampo() == 0) {
            lock.writeLock().lock();
            try {
                if (nextId <= 0) recomputeNextIdUnsafe();
                int id = nextId++;
                entity.setIdCampo(id);
                cache.put(id, entity);
            } finally { lock.writeLock().unlock(); }
            return;
        }

        int id = entity.getIdCampo();
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
            Campo c = rawLoad(id);
            if (c != null) {
                lock.writeLock().lock();
                try { cache.put(c.getIdCampo(), c); } finally { lock.writeLock().unlock(); }
                return true;
            }
        }
        return false;
    }

    @Override
    public Campo create(Integer id) {
        Campo c = new Campo();
        if (id != null && id > 0) c.setIdCampo(id);
        return c;
    }

    @Override
    public List<Campo> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            List<Campo> res = rawFindAll();
            if (res == null) return new ArrayList<>();
            res.sort(Comparator.comparingInt(Campo::getIdCampo));
            lock.writeLock().lock();
            try {
                for (Campo c : res) {
                    if (c != null) cache.put(c.getIdCampo(), c);
                }
            } finally { lock.writeLock().unlock(); }
            return res;
        }

        lock.readLock().lock();
        try {
            List<Campo> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(Campo::getIdCampo));
            return out;
        } finally { lock.readLock().unlock(); }
    }

    @Override
    public Campo findById(int idCampo) { return load(idCampo); }

    /**
     * Compatibilità: pulisce la cache (usato dai test tramite reflection).
     * NEW: resetta anche seeding + nextId.
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