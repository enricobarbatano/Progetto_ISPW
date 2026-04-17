package com.ispw.dao.impl.base;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

/**
 * Base concrete Pagamento DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 *
 * DBMS/FileSystem subclasses should extend this class and override
 * the protected raw* methods to perform actual I/O. To mark a subclass
 * as persistent set the `persistent` flag via the protected constructor.
 */
public class BasePagamentoDAO implements PagamentoDAO {

    private static final Comparator<Pagamento> ORDER_BY_DATA_DESC_ID_DESC =
            Comparator.comparing(Pagamento::getDataPagamento, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Pagamento::getIdPagamento)
                      .reversed();

    protected final Map<Integer, Pagamento> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // When false (default) this instance behaves as pure IN_MEMORY.
    // Subclasses that perform persistence should call super(true).
    private final boolean persistent;

    public BasePagamentoDAO() {
        this(false);
    }

    protected BasePagamentoDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // Protected raw operations
    // Subclasses override these to provide DB/FS I/O.
    // Default implementations are no-op / null and are valid for IN_MEMORY base.
    // -----------------------
    protected Pagamento rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(Pagamento entity) {
        // default: no-op for IN_MEMORY base
    }

    protected void rawDelete(Integer id) {
        // default: no-op for IN_MEMORY base
    }

    protected Pagamento rawFindByPrenotazione(int idPrenotazione) {
        return null;
    }

    // -----------------------
    // DAO interface implementations (cache-first template)
    // -----------------------
    @Override
    public Pagamento load(Integer id) {
        if (id == null || id <= 0) return null;

        // STEP 1: Check cache
        lock.readLock().lock();
        try {
            Pagamento cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        // STEP 2: Fallback to persistent rawLoad if configured
        if (persistent) {
            Pagamento p = rawLoad(id);
            if (p != null) {
                lock.writeLock().lock();
                try {
                    cache.put(p.getIdPagamento(), p);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return p;
        }

        // IN_MEMORY and not present -> not found
        return null;
    }

    @Override
    public void store(Pagamento entity) {
        if (entity == null) return;

        // If new entity (id == 0) we must handle differently for persistent vs in-memory:
        if (entity.getIdPagamento() == 0) {
            if (persistent) {
                // For persistent providers, let rawStore generate id (e.g., DB auto-generated key)
                rawStore(entity); // expected to set entity.id when inserted
                int id = entity.getIdPagamento();
                if (id <= 0) {
                    // Defensive: if rawStore didn't set id, generate a synthetic one in cache space.
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdPagamento(next);
                        cache.put(entity.getIdPagamento(), entity);
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
                    entity.setIdPagamento(next);
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;
            }
        }

        // Existing entity: update cache first
        int id = entity.getIdPagamento();
        lock.writeLock().lock();
        try {
            cache.put(id, entity);
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) {
            // persist change (update)
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
        lock.readLock().lock();
        try {
            return cache.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Pagamento create(Integer id) {
        Pagamento p = new Pagamento();
        if (id != null && id > 0) p.setIdPagamento(id);
        return p;
    }

    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        // STEP 1: search cache
        lock.readLock().lock();
        try {
            Optional<Pagamento> fromCache = cache.values().stream()
                    .filter(p -> p != null && p.getIdPrenotazione() == idPrenotazione)
                    .sorted(ORDER_BY_DATA_DESC_ID_DESC)
                    .findFirst();
            if (fromCache.isPresent()) return fromCache.get();
        } finally {
            lock.readLock().unlock();
        }

        // STEP 2: fallback to raw query if persistent
        if (persistent) {
            Pagamento p = rawFindByPrenotazione(idPrenotazione);
            if (p != null) {
                lock.writeLock().lock();
                try {
                    cache.put(p.getIdPagamento(), p);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return p;
        }

        return null;
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
