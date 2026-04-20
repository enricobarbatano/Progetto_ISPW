package com.ispw.dao.impl.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

/**
 * Base concrete Campo DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 *
 * DBMS/FileSystem subclasses should extend this class and override
 * the protected raw* methods to perform actual I/O. To mark a subclass
 * as persistent set the `persistent` flag via the protected constructor.
 */
public class BaseCampoDAO implements CampoDAO {

    protected final Map<Integer, Campo> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // When false (default) this instance behaves as pure IN_MEMORY.
    // Subclasses that perform persistence should call super(true).
    private final boolean persistent;

    public BaseCampoDAO() { this(false); }

    protected BaseCampoDAO(boolean persistent) { this.persistent = persistent; }

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
    // DAO interface implementations (cache-first template)
    // -----------------------
    @Override
    public Campo load(Integer id) {
        if (id == null || id <= 0) return null;

        lock.readLock().lock();
        try {
            Campo cached = cache.get(id);
            if (cached != null) return cached;
        } finally { lock.readLock().unlock(); }

        if (persistent) {
            Campo c = rawLoad(id);
            if (c != null) {
                lock.writeLock().lock();
                try { cache.put(c.getIdCampo(), c); } finally { lock.writeLock().unlock(); }
            }
            return c;
        }
        return null;
    }

    @Override
    public void store(Campo entity) {
        if (entity == null) return;

        if (entity.getIdCampo() == 0) {
            if (persistent) {
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
            } else {
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdCampo(next);
                    cache.put(next, entity);
                } finally { lock.writeLock().unlock(); }
                return;
            }
        }

        int id = entity.getIdCampo();
        lock.writeLock().lock();
        try { cache.put(id, entity); } finally { lock.writeLock().unlock(); }
        if (persistent) rawStore(entity);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;
        lock.writeLock().lock();
        try { cache.remove(id); } finally { lock.writeLock().unlock(); }
        if (persistent) rawDelete(id);
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;
        lock.readLock().lock();
        try { if (cache.containsKey(id)) return true; } finally { lock.readLock().unlock(); }
        if (persistent) {
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
        if (persistent) {
            List<Campo> res = rawFindAll();
            if (res == null) return new ArrayList<>();
            res.sort(Comparator.comparingInt(Campo::getIdCampo));
            lock.writeLock().lock();
            try {
                for (Campo c : res) { if (c != null) cache.put(c.getIdCampo(), c); }
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
     */
    public void clear() {
        lock.writeLock().lock();
        try { cache.clear(); } finally { lock.writeLock().unlock(); }
    }
}
