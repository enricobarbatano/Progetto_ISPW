package com.ispw.dao.impl.base;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

/**
 * Base concreta del DAO Fattura con comportamento cache-first.
 * - Istanza diretta => IN_MEMORY (persistent=false)
 * - Subclass DBMS/FS => persistent=true, usa raw* come I/O
 */
public class BaseFatturaDAO implements FatturaDAO {

    /**
     * Comparator "ultima fattura": dataEmissione desc (null in fondo), poi idFattura desc.
     * Evito reversed globale per non invertire nullsLast.
     */
    protected static final Comparator<Fattura> ORDER_LAST_BY_DATE_ID_DESC =
            Comparator.comparing(Fattura::getDataEmissione,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                      .thenComparing(Comparator.comparingInt(Fattura::getIdFattura).reversed());

    protected final Map<Integer, Fattura> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Il flag persistent indica se il provider è persistente (DB/FS) o in-memory
    // In caso di provider persistente, i raw* devono essere implementati e usati come fallback I/O

    private final boolean persistent;
    
    // Costruttore di default: IN_MEMORY
    public BaseFatturaDAO() { this(false); }
    // Costruttore per provider persistente (DB/FS)
    protected BaseFatturaDAO(boolean persistent) { this.persistent = persistent; }

    // -----------------------
    // RAW HOOKS (I/O) - override in DBMS/FS
    // -----------------------
    @SuppressWarnings("java:S1172")
    protected Fattura rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(Fattura entity) {
        // no-op: base in-memory implementation
    }

    protected void rawDelete(Integer id) {
        // no-op: base in-memory implementation
    }

    /**
     * Per persistent provider deve restituire l'ultima fattura dell'utente (DB/FS).
     * Default IN_MEMORY: null.
     */
    @SuppressWarnings("java:S1172")
    protected Fattura rawFindLastByUtente(int idUtente) {
        return null;
    }

    // -----------------------
    // DAO API - cache-first
    // -----------------------
    @Override
    public Fattura load(Integer id) {
        if (id == null || id <= 0) return null;

        // 1) cache-first
        lock.readLock().lock();
        try {
            Fattura cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        // 2) fallback raw se persistent
        if (!persistent) return null;

        Fattura f = rawLoad(id);
        if (f != null && f.getIdFattura() > 0) {
            lock.writeLock().lock();
            try {
                cache.put(f.getIdFattura(), f);
            } finally {
                lock.writeLock().unlock();
            }
        }
        return f;
    }
    // store/delete/exist/create/findLastByUtente implementati in cache-first, raw* usati solo se persistent
    @Override
    public void store(Fattura entity) {
        if (entity == null) return;

        // Caso nuovo (id==0)
        if (entity.getIdFattura() == 0) {
            if (persistent) {
                // il provider (DB/FS) deve assegnare id
                rawStore(entity);

                int id = entity.getIdFattura();
                if (id <= 0) {
                    // fallback difensivo (idealmente non dovrebbe accadere per DBMS con generated keys)
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdFattura(next);
                        cache.put(next, entity);
                    } finally {
                        lock.writeLock().unlock();
                    }
                    return;
                }

                lock.writeLock().lock();
                try {
                    cache.put(id, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;

            } else {
                // IN_MEMORY: genera id e salva in cache
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdFattura(next);
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;
            }
        }

        // Caso esistente (id!=0): cache-put e poi persistenza se necessario
        int id = entity.getIdFattura();
        lock.writeLock().lock();
        try {
            cache.put(id, entity);
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) rawStore(entity);
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;

        lock.writeLock().lock();
        try {
            cache.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) rawDelete(id);
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;

        lock.readLock().lock();
        try {
            if (cache.containsKey(id)) return true;
        } finally {
            lock.readLock().unlock();
        }

        if (!persistent) return false;

        // fallback rawLoad (costoso ma compatibile)
        Fattura f = rawLoad(id);
        if (f != null && f.getIdFattura() > 0) {
            lock.writeLock().lock();
            try {
                cache.put(f.getIdFattura(), f);
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        }
        return false;
    }

    @Override
    public Fattura create(Integer id) {
        Fattura f = new Fattura();
        f.setIdFattura(id != null ? id : 0);
        return f;
    }

    /**
     * Recupera l'ultima fattura emessa per un utente.
     *
     * - Provider persistente: query raw (cache parziale non garantisce "ultima" corretta).
     * - IN_MEMORY: calcolo su cache.
     */
    @Override
    public Fattura findLastByUtente(int idUtente) {
        if (idUtente <= 0) return null;

        if (persistent) {
            // Correttezza: vai su raw per avere veramente l'ultima nel provider
            Fattura f = rawFindLastByUtente(idUtente);
            if (f != null && f.getIdFattura() > 0) {
                lock.writeLock().lock();
                try {
                    cache.put(f.getIdFattura(), f);
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return f;
        }

        // IN_MEMORY: trova l'ultima dalla cache
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(f -> f != null && f.getIdUtente() == idUtente)
                    .sorted(ORDER_LAST_BY_DATE_ID_DESC)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Utility per test/cleanup
     *  serve a svuotare la cache (non cancella dati persistenti, ma forzerà reload da raw al prossimo accesso)
     *  potrei usare mock per testare solo la cache, ma questa è una soluzione semplice per testare anche il fallback raw
     *  senza dover creare un provider specifico di test.
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