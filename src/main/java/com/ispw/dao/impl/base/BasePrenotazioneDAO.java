package com.ispw.dao.impl.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * Base concrete Prenotazione DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 *
 * DBMS/FileSystem subclasses should extend this class and override
 * the protected raw* methods to perform actual I/O. To mark a subclass
 * as persistent set the `persistent` flag via the protected constructor.
 */
public class BasePrenotazioneDAO implements PrenotazioneDAO {

    private static final Comparator<Prenotazione> ORDER_BY_DATA_ORA_ID =
            Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Prenotazione::getIdPrenotazione);

    protected final Map<Integer, Prenotazione> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // When false (default) this instance behaves as pure IN_MEMORY.
    // Subclasses that perform persistence should call super(true).
    private final boolean persistent;

    public BasePrenotazioneDAO() {
        this(false);
    }

    protected BasePrenotazioneDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // Protected raw operations
    // Subclasses override these to provide DB/FS I/O.
    // Default implementations are no-op / null and are valid for IN_MEMORY base.
    // -----------------------
    protected Prenotazione rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(Prenotazione entity) {
        // default: no-op for IN_MEMORY base
    }

    protected void rawDelete(Integer id) {
        // default: no-op for IN_MEMORY base
    }

    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        return null;
    }

    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        return null;
    }

    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        // default: no-op
    }

    // -----------------------
    // DAO interface implementations (cache-first template)
    // -----------------------
    @Override
    public Prenotazione load(Integer id) {
        if (id == null || id <= 0) return null;

        // STEP 1: Check cache
        lock.readLock().lock();
        try {
            Prenotazione cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        // STEP 2: Fallback to persistent rawLoad if configured
        if (persistent) {
            Prenotazione p = rawLoad(id);
            if (p != null) {
                lock.writeLock().lock();
                try {
                    cache.put(p.getIdPrenotazione(), p);
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
    public void store(Prenotazione entity) {
        if (entity == null) return;

        // If new entity (id == 0) we must handle differently for persistent vs in-memory:
        if (entity.getIdPrenotazione() == 0) {
            if (persistent) {
                rawStore(entity); // expected to set entity.id when inserted
                int id = entity.getIdPrenotazione();
                if (id <= 0) {
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdPrenotazione(next);
                        cache.put(entity.getIdPrenotazione(), entity);
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
                    entity.setIdPrenotazione(next);
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;
            }
        }

        // Existing entity: update cache first
        int id = entity.getIdPrenotazione();
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
            Prenotazione p = rawLoad(id);
            if (p != null) {
                lock.writeLock().lock();
                try {
                    cache.put(p.getIdPrenotazione(), p);
                } finally {
                    lock.writeLock().unlock();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public Prenotazione create(Integer id) {
        Prenotazione p = new Prenotazione();
        if (id != null && id > 0) p.setIdPrenotazione(id);
        return p;
    }

    @Override
    public Prenotazione findById(int idPrenotazione) {
        return load(idPrenotazione);
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        // If persistent provider, always query the persistent store to avoid
        // returning incomplete results based only on a potentially partial cache.
        if (persistent) {
            List<Prenotazione> res = rawFindByUtente(idUtente);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_DATA_ORA_ID);
            lock.writeLock().lock();
            try {
                for (Prenotazione p : res) {
                    if (p != null) cache.put(p.getIdPrenotazione(), p);
                }
            } finally {
                lock.writeLock().unlock();
            }
            return res;
        }

        // IN_MEMORY: read from cache only
        lock.readLock().lock();
        try {
            List<Prenotazione> out = new ArrayList<>();
            for (Prenotazione p : cache.values()) {
                if (p != null && p.getIdUtente() == idUtente) out.add(p);
            }
            out.sort(ORDER_BY_DATA_ORA_ID);
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        if (persistent) {
            List<Prenotazione> res = rawFindByUtenteAndStato(idUtente, stato);
            if (res == null) return new ArrayList<>();
            res.sort(ORDER_BY_DATA_ORA_ID);
            lock.writeLock().lock();
            try {
                for (Prenotazione p : res) {
                    if (p != null) cache.put(p.getIdPrenotazione(), p);
                }
            } finally {
                lock.writeLock().unlock();
            }
            return res;
        }

        // IN_MEMORY: read from cache only
        lock.readLock().lock();
        try {
            List<Prenotazione> out = new ArrayList<>();
            for (Prenotazione p : cache.values()) {
                if (p != null && p.getIdUtente() == idUtente && p.getStato() == stato) out.add(p);
            }
            out.sort(ORDER_BY_DATA_ORA_ID);
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        // update cache if present
        boolean updatedInCache = false;
        lock.writeLock().lock();
        try {
            Prenotazione p = cache.get(idPrenotazione);
            if (p != null) {
                p.setStato(nuovoStato);
                cache.put(idPrenotazione, p);
                updatedInCache = true;
            }
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) {
            rawUpdateStato(idPrenotazione, nuovoStato);
        }
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
