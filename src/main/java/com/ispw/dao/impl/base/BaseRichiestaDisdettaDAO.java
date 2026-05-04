package com.ispw.dao.impl.base;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
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
import com.ispw.dao.interfaces.RichiestaDisdettaDAO;
import com.ispw.model.entity.RichiestaDisdettaRimborso;
import com.ispw.model.enums.StatoRichiestaDisdetta;

/**
 * Base DAO cache-first per RichiestaDisdettaRimborso.
 *
 * Vincolo architetturale:
 * - cache-first
 * - seed solo se (persistent == null) e cache vuota (modalità demo IN_MEMORY)
 * - raw* chiamati solo se (persistent == true) (DBMS/FS concreti)
 *
 * Tri-state:
 * - TRUE  -> DB/FS (raw*)
 * - FALSE -> IN_MEMORY puro (no seed)
 * - NULL  -> IN_MEMORY seeded (seed se cache vuota)
 */
public class BaseRichiestaDisdettaDAO implements RichiestaDisdettaDAO {

    protected final Map<Integer, RichiestaDisdettaRimborso> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Boolean persistent;

    private volatile boolean seeded = false;
    private int nextId = 1; // protetto da writeLock

    public BaseRichiestaDisdettaDAO() { this(null); }
    protected BaseRichiestaDisdettaDAO(Boolean persistent) { this.persistent = persistent; }

    // -------- RAW HOOKS (DB/FS) --------
    // Concreti DBMS/FS override
    protected RichiestaDisdettaRimborso rawLoad(Integer id) { return null; }
    protected void rawStore(RichiestaDisdettaRimborso r) { }
    protected void rawDelete(Integer id) { }
    protected List<RichiestaDisdettaRimborso> rawFindAll() { return null; }
    protected void rawUpdateStato(int id, StatoRichiestaDisdetta stato, Integer idGestore, String notaGestore) { }

    // -------- Seed (ONLY if persistent == null) --------
    private void ensureSeeded() {
        if (persistent != null) return; // seed solo per modalità demo (null)
        if (seeded) return;

        lock.writeLock().lock();
        try {
            if (seeded) return;

            // ✅ prima cache: se già popolata, non seedare
            if (!cache.isEmpty()) {
                recomputeNextIdUnsafe();
                seeded = true;
                return;
            }

            List<RichiestaDisdettaRimborso> initial = readSeed();
            for (RichiestaDisdettaRimborso r : initial) {
                if (r == null) continue;
                if (r.getIdRichiesta() <= 0) continue;
                cache.put(r.getIdRichiesta(), r);
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

    private List<RichiestaDisdettaRimborso> readSeed() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("richieste_disdetta.json"); // opzionale
            if (!Files.exists(file)) return List.of();

            ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
            CollectionType listType = om.getTypeFactory()
                    .constructCollectionType(List.class, RichiestaDisdettaRimborso.class);

            List<RichiestaDisdettaRimborso> res = om.readValue(file.toFile(), listType);
            return (res != null) ? res : List.of();
        } catch (Exception ex) {
            // best-effort: se seed fallisce, parti vuoto
            return List.of();
        }
    }

    // ===================== DAO<Integer, Entity> =====================

    @Override
    public RichiestaDisdettaRimborso load(Integer id) {
        if (id == null || id <= 0) return null;
        ensureSeeded();

        // cache-first
        lock.readLock().lock();
        try {
            RichiestaDisdettaRimborso cached = cache.get(id);
            if (cached != null) return cached;
        } finally {
            lock.readLock().unlock();
        }

        // fallback su raw solo se persistente
        if (Boolean.TRUE.equals(persistent)) {
            RichiestaDisdettaRimborso r = rawLoad(id);
            if (r != null && r.getIdRichiesta() > 0) {
                lock.writeLock().lock();
                try {
                    cache.put(r.getIdRichiesta(), r);
                    // opzionale: aggiorna nextId se serve
                    if (r.getIdRichiesta() >= nextId) nextId = r.getIdRichiesta() + 1;
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return r;
        }

        return null;
    }

    @Override
    public void store(RichiestaDisdettaRimborso richiesta) {
        if (richiesta == null) return;
        ensureSeeded();

        // default di dominio
        if (richiesta.getTimestampRichiesta() == null) {
            richiesta.setTimestampRichiesta(LocalDateTime.now());
        }
        if (richiesta.getStato() == null) {
            richiesta.setStato(StatoRichiestaDisdetta.PENDING);
        }

        if (Boolean.TRUE.equals(persistent)) {
            // ✅ Persistente: chiamata raw UNA SOLA VOLTA
            rawStore(richiesta);

            // fallback se il raw non ha assegnato id
            if (richiesta.getIdRichiesta() <= 0) {
                lock.writeLock().lock();
                try {
                    int id = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    richiesta.setIdRichiesta(id);
                    if (id >= nextId) nextId = id + 1;
                } finally {
                    lock.writeLock().unlock();
                }
            }

            lock.writeLock().lock();
            try {
                cache.put(richiesta.getIdRichiesta(), richiesta);
                if (richiesta.getIdRichiesta() >= nextId) nextId = richiesta.getIdRichiesta() + 1;
            } finally {
                lock.writeLock().unlock();
            }
            return;
        }

        // IN_MEMORY (persistent == null o false): assegna id se necessario (thread-safe)
        lock.writeLock().lock();
        try {
            if (richiesta.getIdRichiesta() <= 0) {
                richiesta.setIdRichiesta(nextId++);
            }
            cache.put(richiesta.getIdRichiesta(), richiesta);
        } finally {
            lock.writeLock().unlock();
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

        if (Boolean.TRUE.equals(persistent)) rawDelete(id);
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
            RichiestaDisdettaRimborso r = rawLoad(id);
            if (r != null && r.getIdRichiesta() > 0) {
                lock.writeLock().lock();
                try {
                    cache.put(r.getIdRichiesta(), r);
                    if (r.getIdRichiesta() >= nextId) nextId = r.getIdRichiesta() + 1;
                } finally {
                    lock.writeLock().unlock();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public RichiestaDisdettaRimborso create(Integer id) {
        RichiestaDisdettaRimborso r = new RichiestaDisdettaRimborso();
        if (id != null && id > 0) r.setIdRichiesta(id);
        return r;
    }

    // ===================== Metodi specifici =====================

    @Override
    public List<RichiestaDisdettaRimborso> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            List<RichiestaDisdettaRimborso> res = rawFindAll();
            if (res == null) return new ArrayList<>();

            lock.writeLock().lock();
            try {
                for (RichiestaDisdettaRimborso r : res) {
                    if (r != null && r.getIdRichiesta() > 0) {
                        cache.put(r.getIdRichiesta(), r);
                        if (r.getIdRichiesta() >= nextId) nextId = r.getIdRichiesta() + 1;
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }

            res.sort(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta));
            return res;
        }

        lock.readLock().lock();
        try {
            List<RichiestaDisdettaRimborso> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta));
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<RichiestaDisdettaRimborso> findByStato(StatoRichiestaDisdetta stato) {
        ensureSeeded();
        if (stato == null) return List.of();

        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r != null && stato.equals(r.getStato()))
                    .sorted(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public RichiestaDisdettaRimborso findByPrenotazione(int idPrenotazione) {
        ensureSeeded();
        if (idPrenotazione <= 0) return null;

        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r != null && r.getIdPrenotazione() == idPrenotazione)
                    .findFirst().orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updateStato(int idRichiesta, StatoRichiestaDisdetta stato, Integer idGestore, String notaGestore) {
        ensureSeeded();
        if (idRichiesta <= 0 || stato == null) return;

        RichiestaDisdettaRimborso r = load(idRichiesta);
        if (r == null) return;

        // aggiorna entity
        r.setStato(stato);
        r.setTimestampDecisione(LocalDateTime.now());
        r.setIdGestoreDecisione(idGestore);
        r.setNotaGestore(notaGestore);

        // cache write-through
        lock.writeLock().lock();
        try {
            cache.put(r.getIdRichiesta(), r);
        } finally {
            lock.writeLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            rawUpdateStato(idRichiesta, stato, idGestore, notaGestore);
        }
    }

    /** Utility: pulizia per test/in-memory */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            seeded = false;
            nextId = 1;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
