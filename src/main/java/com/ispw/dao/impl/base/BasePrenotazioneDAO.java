package com.ispw.dao.impl.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Fattura;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * Base concrete Prenotazione DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 *
 * A2 strategy: compose ALWAYS. The DAO "father" composes Prenotazione by calling
 * child DAOs (CampoDAO, PagamentoDAO, FatturaDAO) in cache-first mode, both for
 * load(id) and list finders.
 *
 * NOTE: To avoid circular composition, CampoDAO must NOT compose Prenotazioni.
 */
public class BasePrenotazioneDAO implements PrenotazioneDAO {

    /** Ordine cronologico crescente (data ASC, oraInizio ASC, id ASC) */
    private static final Comparator<Prenotazione> ORDER_BY_DATA_ORA_ID =
            Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Prenotazione::getIdPrenotazione);

    protected final Map<Integer, Prenotazione> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BasePrenotazioneDAO() {
        this(false);
    }

    protected BasePrenotazioneDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // Protected raw operations
    // (Subclasses override these to provide DB/FS I/O)
    // -----------------------
    protected Prenotazione rawLoad(Integer id) { return null; }
    protected void rawStore(Prenotazione entity) { /* no-op in memory */ }
    protected void rawDelete(Integer id) { /* no-op in memory */ }
    protected List<Prenotazione> rawFindByUtente(int idUtente) { return null; }
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) { return null; }
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) { /* no-op in memory */ }

    // -----------------------
    // Public DAO API (cache-first template + ALWAYS COMPOSE)
    // -----------------------
    @Override
    public Prenotazione load(Integer id) {
        if (id == null || id <= 0) return null;

        // STEP 1: cache
        Prenotazione cached;
        lock.readLock().lock();
        try {
            cached = cache.get(id);
        } finally {
            lock.readLock().unlock();
        }
        if (cached != null) {
            // A2: ensure composed (if someone stored raw objects in cache)
            composeIfNeeded(cached);
            return cached;
        }

        // STEP 2: fallback raw (only if persistent)
        Prenotazione p = null;
        if (persistent) {
            p = rawLoad(id);
        } else {
            // IN_MEMORY and not present
            return null;
        }
        if (p == null) return null;

        // STEP 3: compose ALWAYS (A2)
        composeIfNeeded(p);

        // STEP 4: cache-put
        lock.writeLock().lock();
        try {
            cache.put(p.getIdPrenotazione(), p);
        } finally {
            lock.writeLock().unlock();
        }

        return p;
    }

    @Override
    public void store(Prenotazione entity) {
        if (entity == null) return;

        // new entity (id == 0)
        if (entity.getIdPrenotazione() == 0) {
            if (persistent) {
                // rawStore should assign id (DB auto/gen or FS max+1)
                rawStore(entity);

                int id = entity.getIdPrenotazione();
                if (id <= 0) {
                    // Defensive fallback: generate id from cache space
                    // (preferably never happens for real DBMS providers)
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

                // compose before caching (A2)
                composeIfNeeded(entity);

                lock.writeLock().lock();
                try {
                    cache.put(id, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;

            } else {
                // IN_MEMORY: generate id locally
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    entity.setIdPrenotazione(next);
                    composeIfNeeded(entity); // A2
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }
                return;
            }
        }

        // existing entity
        composeIfNeeded(entity); // A2

        lock.writeLock().lock();
        try {
            cache.put(entity.getIdPrenotazione(), entity);
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
            if (cache.containsKey(id)) return true;
        } finally {
            lock.readLock().unlock();
        }

        if (persistent) {
            // fallback rawLoad (costly but compatible)
            Prenotazione p = rawLoad(id);
            if (p != null) {
                // Do NOT force compose in exists() (keep it lightweight)
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
        final List<Prenotazione> res;

        if (persistent) {
           
            res = rawFindByUtente(idUtente);
        } else {
            // IN_MEMORY from cache
            res = new ArrayList<>();
            lock.readLock().lock();
            try {
                for (Prenotazione p : cache.values()) {
                    if (p != null && p.getIdUtente() == idUtente) res.add(p);
                }
            } finally {
                lock.readLock().unlock();
            }
        }

        if (res == null) return new ArrayList<>();

        // Sort
        res.sort(ORDER_BY_DATA_ORA_ID);

        // A2: compose ALL
        prewarmCampoCache(res);
        for (Prenotazione p : res) {
            composeIfNeeded(p);
        }

        // cache-put results
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

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        final List<Prenotazione> res;

        if (persistent) {
            res = rawFindByUtenteAndStato(idUtente, stato);
        } else {
            res = new ArrayList<>();
            lock.readLock().lock();
            try {
                for (Prenotazione p : cache.values()) {
                    if (p != null && p.getIdUtente() == idUtente && p.getStato() == stato) res.add(p);
                }
            } finally {
                lock.readLock().unlock();
            }
        }

        if (res == null) return new ArrayList<>();

        res.sort(ORDER_BY_DATA_ORA_ID);

        // A2: compose ALL
        prewarmCampoCache(res);
        for (Prenotazione p : res) {
            composeIfNeeded(p);
        }

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

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        // Update cache if present
        lock.writeLock().lock();
        try {
            Prenotazione p = cache.get(idPrenotazione);
            if (p != null) {
                p.setStato(nuovoStato);
                cache.put(idPrenotazione, p);
            }
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) {
            rawUpdateStato(idPrenotazione, nuovoStato);
        }
    }

    /** Compatibilità: pulisce la cache (test) */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -----------------------
    // A2 Composition logic (DAO father calls DAO children, cache-first)
    // -----------------------

    /**
     * Composizione A2: per ogni prenotazione,
     * compone i campi Campo, 
     * Pagamento e Fattura se non già presenti.
     * IMPORTANT (anti-loop): CampoDAO non deve comporre Prenotazioni.
     */
    protected void composeIfNeeded(Prenotazione p) {
        if (p == null) return;

        // Campo
        if (p.getCampo() == null && p.getIdCampo() > 0) {
            
            Object campoDao = getDaoFromFactory("getCampoDAO");
            Campo campo = (Campo) invokeFirstNonNull(
                    campoDao,
                    new String[] {"load", "findById"},
                    new Class<?>[] { Integer.class, int.class },
                    new Object[] { p.getIdCampo(), p.getIdCampo() }
            );
            if (campo != null) p.setCampo(campo);
        }

        // Pagamento (by prenotazione)
        if (p.getPagamento() == null && p.getIdPrenotazione() > 0) {
            Object pagDao = getDaoFromFactory("getPagamentoDAO");
            Pagamento pag = (Pagamento) invokeFirstNonNull(
                    pagDao,
                    new String[] {"findByPrenotazione", "findByIdPrenotazione"},
                    new Class<?>[] { int.class, Integer.class },
                    new Object[] { p.getIdPrenotazione(), p.getIdPrenotazione() }
            );
            if (pag != null) p.setPagamento(pag);
        }

        // Fattura (try common signatures; adapt if your FatturaDAO differs)
        if (p.getFattura() == null) {
            Object fatDao = getDaoFromFactory("getFatturaDAO");
            // Prefer a "by prenotazione" lookup if present
            Fattura fatt = (Fattura) invokeFirstNonNull(
                    fatDao,
                    new String[] {"findByPrenotazione", "findByIdPrenotazione", "findLastByUtente"},
                    new Class<?>[] { int.class, int.class, int.class },
                    new Object[] { p.getIdPrenotazione(), p.getIdPrenotazione(), p.getIdUtente() }
            );
            if (fatt != null) p.setFattura(fatt);
        }
    }

    //Prewarm helper: dato un elenco di prenotazioni, pre-carica in cache i campi Campo associati (A2).
    // Questo è un'ottimizzazione per evitare N chiamate individuali a CampoDAO quando si carica una lista di prenotazioni.

    private void prewarmCampoCache(List<Prenotazione> list) {
        Object campoDao = getDaoFromFactory("getCampoDAO");
        if (campoDao == null || list == null || list.isEmpty()) return;

        Set<Integer> ids = new HashSet<>();
        for (Prenotazione p : list) {
            if (p != null && p.getIdCampo() > 0) ids.add(p.getIdCampo());
        }
        for (Integer idCampo : ids) {
            invokeFirstNonNull(
                    campoDao,
                    new String[] {"load", "findById"},
                    new Class<?>[] { Integer.class, int.class },
                    new Object[] { idCampo, idCampo }
            );
        }
    }
    // -----------------------
    // Reflection helpers (to avoid hard-binding to specific DAO interfaces/method names)
    // -----------------------

    // Reflection helper: dato il nome di un metodo factory (es. "getCampoDAO"), 
    // prova a invocarlo su DAOFactory per ottenere l'istanza del DAO figlio.
    private Object getDaoFromFactory(String factoryMethodName) {
        try {
            Object factory = DAOFactory.getInstance();
            Method m = factory.getClass().getMethod(factoryMethodName);
            return m.invoke(factory);
        } catch (Exception ignore) {
            return null;
        }
    }
    // Reflection helper: dato un target, una lista di nomi di metodi, tipi di parametri e argomenti,
    // prova a invocare ciascuna combinazione fino a trovare una che restituisce un risultato non null.
    // Questo è utile per adattarsi a diversi DAO che potrebbero avere metodi di lookup con nomi o firme leggermente diverse.
    private Object invokeFirstNonNull(Object target, String[] methodNames, Class<?>[] paramTypes, Object[] args) {
        if (target == null || methodNames == null) return null;

        for (String name : methodNames) {
            try {
                // Try each provided signature pairing (we pass parallel arrays; same index)
                for (int i = 0; i < paramTypes.length && i < args.length; i++) {
                    Method m = findMethod(target.getClass(), name, paramTypes[i]);
                    if (m == null) continue;
                    Object out = m.invoke(target, args[i]);
                    if (out != null) return out;
                }
            } catch (Exception ignore) {
                // try next
            }
        }
        return null;
    }
    // Helper per trovare un metodo con nome e tipo di parametro specificati, restituendo null se non trovato.
    private Method findMethod(Class<?> clazz, String name, Class<?> paramType) {
        try {
            return clazz.getMethod(name, paramType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}