package com.ispw.dao.impl.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.UtenteFinale;

/**
 * Base concrete GeneralUser DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 *
 * DBMS/FileSystem subclasses should extend this class and override
 * the protected raw* methods to perform actual I/O. To mark a subclass
 * as persistent set the `persistent` flag via the protected constructor.
 */
public class BaseGeneralUserDAO implements GeneralUserDAO {

    protected final Map<Integer, GeneralUser> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // When false (default) this instance behaves as pure IN_MEMORY.
    // Subclasses that perform persistence should call super(true).
    private final boolean persistent;

    public BaseGeneralUserDAO() {
        this(false);
    }

    protected BaseGeneralUserDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // Protected raw operations
    // Subclasses override these to provide DB/FS I/O.
    // Default implementations are no-op / null and are valid for IN_MEMORY base.
    // -----------------------
    protected GeneralUser rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(GeneralUser entity) {
        // default: no-op for IN_MEMORY base
    }

    protected void rawDelete(Integer id) {
        // default: no-op for IN_MEMORY base
    }

    protected List<GeneralUser> rawFindAll() {
        return null;
    }

    protected GeneralUser rawFindByEmail(String email) {
        return null;
    }

    // -----------------------
    // DAO interface implementations (cache-first template)
    // -----------------------
    @Override
    public GeneralUser load(Integer id) {
        if (id == null || id <= 0) return null;

        // STEP 1: Check cache
        lock.readLock().lock();
        try {
            GeneralUser cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        // STEP 2: Fallback to persistent rawLoad if configured
        if (persistent) {
            GeneralUser g = rawLoad(id);
            if (g != null) {
                lock.writeLock().lock();
                try {
                    cache.put(g.getIdUtente(), g);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return g;
        }

        // IN_MEMORY and not present -> not found
        return null;
    }

    @Override
    public void store(GeneralUser entity) {
        if (entity == null) return;

        if (entity.getIdUtente() == 0) {
            if (persistent) {
                rawStore(entity); // expected to set entity.id when inserted
                int id = entity.getIdUtente();
                if (id <= 0) {
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdUtente(next);
                        cache.put(entity.getIdUtente(), entity);
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
            } else {
                // IN_MEMORY: generate id locally
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdUtente(next);
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;
            }
        }

        // Existing entity: update cache first
        int id = entity.getIdUtente();
        lock.writeLock().lock();
        try {
            cache.put(id, entity);
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) {
            rawStore(entity);
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;

        // Remove from cache first
        lock.writeLock().lock();
        try {
            cache.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) {
            rawDelete(id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;

        // STEP 1: check cache
        lock.readLock().lock();
        try {
            if (cache.containsKey(id)) return true;
        } finally {
            lock.readLock().unlock();
        }

        // STEP 2: fallback to rawLoad for persistent providers
        if (persistent) {
            GeneralUser g = rawLoad(id);
            if (g != null) {
                lock.writeLock().lock();
                try {
                    cache.put(g.getIdUtente(), g);
                } finally {
                    lock.writeLock().unlock();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public GeneralUser create(Integer id) {
        /**
         * Nota: per compatibilita' con il codice esistente questa factory
         * crea di default un `UtenteFinale` (non un `Gestore`). Il `Gestore`
         * e' seedato altrove nel bootstrap; non cambiare i controller che
         * si aspettano questo comportamento.
         */
        final UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    @Override
    public List<GeneralUser> findAll() {
        if (persistent) {
            List<GeneralUser> res = rawFindAll();
            if (res == null) return new ArrayList<>();
            lock.writeLock().lock();
            try {
                for (GeneralUser g : res) {
                    if (g == null) continue;
                    int id = g.getIdUtente();
                    if (id > 0) cache.put(id, g);
                }
            } finally {
                lock.writeLock().unlock();
            }
            return res;
        }

        lock.readLock().lock();
        try {
            return new ArrayList<>(cache.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public GeneralUser findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        // STEP 1: check cache
        lock.readLock().lock();
        try {
            Optional<GeneralUser> fromCache = cache.values().stream()
                    .filter(u -> {
                        String ue = u.getEmail();
                        if (ue == null) return false;
                        String uNorm = normalizeEmail(ue);
                        return uNorm != null && uNorm.equals(norm);
                    })
                    .findFirst();
            if (fromCache.isPresent()) return fromCache.get();
        } finally {
            lock.readLock().unlock();
        }

        // STEP 2: fallback to persistent rawFindByEmail
        if (persistent) {
            GeneralUser g = rawFindByEmail(norm);
            if (g != null) {
                lock.writeLock().lock();
                try {
                    cache.put(g.getIdUtente(), g);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return g;
        }

        return null;
    }

    @Override
    public GeneralUser findById(int idUtente) {
        return load(idUtente);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Compatibilità: pulisce la cache (usato dai test tramite reflection).
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
