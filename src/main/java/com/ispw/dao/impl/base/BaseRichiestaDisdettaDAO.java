package com.ispw.dao.impl.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
 * - cache-first;
 * - seed solo se persistent == null e cache vuota;
 * - raw* chiamati solo se persistent == true, cioè DBMS/FS concreti.
 *
 * Tri-state:
 * - TRUE  -> DB/FS, quindi usa raw*;
 * - FALSE -> IN_MEMORY puro, senza seed;
 * - NULL  -> IN_MEMORY seeded, con seed se cache vuota.
 */
public class BaseRichiestaDisdettaDAO implements RichiestaDisdettaDAO {

    protected final Map<Integer, RichiestaDisdettaRimborso> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Boolean persistent;

    private volatile boolean seeded = false;

    /*
     * AtomicInteger evita smell sulle operazioni atomiche.
     * L'accesso resta comunque protetto dai lock nei punti critici,
     * quindi il comportamento del DAO non cambia.
     */
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * Default: IN_MEMORY seeded, quindi persistent == null.
     */
    public BaseRichiestaDisdettaDAO() {
        this(null);
    }

    /**
     * Costruttore usato da DBMS/FS e varianti in-memory.
     *
     * @param persistent true per DBMS/FS, false per in-memory puro, null per in-memory seeded
     */
    protected BaseRichiestaDisdettaDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // =========================================================
    // RAW HOOKS
    // =========================================================
    // Le subclass DBMS/FS sovrascrivono questi metodi per fare I/O reale.
    // La base in-memory lavora solo sulla cache.

    /**
     * Hook per caricamento da persistenza.
     *
     * La base in-memory non carica da DB/FS, quindi ritorna null.
     */
    @SuppressWarnings("java:S1172")
    protected RichiestaDisdettaRimborso rawLoad(Integer id) {
        return null;
    }

    /**
     * Hook per salvataggio su persistenza.
     *
     * La base in-memory salva direttamente in cache, quindi questo metodo è no-op.
     */
    @SuppressWarnings("java:S1172")
    protected void rawStore(RichiestaDisdettaRimborso richiesta) {
        // No-op intenzionale: le subclass persistenti fanno override.
    }

    /**
     * Hook per eliminazione da persistenza.
     *
     * La base in-memory elimina direttamente dalla cache, quindi questo metodo è no-op.
     */
    @SuppressWarnings("java:S1172")
    protected void rawDelete(Integer id) {
        // No-op intenzionale: le subclass persistenti fanno override.
    }

    /**
     * Hook per caricamento completo da persistenza.
     *
     * La base in-memory non legge da DB/FS, quindi ritorna lista vuota.
     */
    protected List<RichiestaDisdettaRimborso> rawFindAll() {
        return List.of();
    }

    /**
     * Hook per aggiornamento dello stato su persistenza.
     *
     * La base in-memory aggiorna direttamente la cache, quindi questo metodo è no-op.
     */
    @SuppressWarnings("java:S1172")
    protected void rawUpdateStato(int id,
                                  StatoRichiestaDisdetta stato,
                                  Integer idGestore,
                                  String notaGestore) {
        // No-op intenzionale: le subclass persistenti fanno override.
    }

    // =========================================================
    // SEED LOGIC
    // =========================================================

    /**
     * Carica le richieste iniziali da seed/richieste_disdetta.json
     * solo in modalità in-memory seeded.
     */
    private void ensureSeeded() {
        if (persistent != null) {
            return;
        }

        if (seeded) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (seeded) {
                return;
            }

            if (!cache.isEmpty()) {
                recomputeNextIdUnsafe();
                seeded = true;
                return;
            }

            List<RichiestaDisdettaRimborso> initial = readSeed();

            for (RichiestaDisdettaRimborso richiesta : initial) {
                addSeedRichiestaIfValid(richiesta);
            }

            recomputeNextIdUnsafe();
            seeded = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Aggiunge alla cache una richiesta letta dal seed solo se valida.
     *
     * Questo metodo evita continue multipli nel ciclo di ensureSeeded().
     */
    private void addSeedRichiestaIfValid(RichiestaDisdettaRimborso richiesta) {
        if (richiesta != null && richiesta.getIdRichiesta() > 0) {
            cache.put(richiesta.getIdRichiesta(), richiesta);
        }
    }

    /**
     * Ricalcola il prossimo id partendo dalla cache.
     *
     * Questo metodo deve essere chiamato mentre il write lock è già acquisito.
     */
    private void recomputeNextIdUnsafe() {
        int max = cache.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        int computedNextId = max + 1;

        if (computedNextId <= 0) {
            computedNextId = 1;
        }

        nextId.set(computedNextId);
    }

    /**
     * Legge le richieste di seed da richieste_disdetta.json.
     *
     * Se il file non esiste o la lettura fallisce, ritorna una lista vuota.
     */
    private List<RichiestaDisdettaRimborso> readSeed() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("richieste_disdetta.json");

            if (!Files.exists(file)) {
                return List.of();
            }

            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, RichiestaDisdettaRimborso.class);

            List<RichiestaDisdettaRimborso> richieste = mapper.readValue(file.toFile(), listType);

            return richieste != null ? richieste : List.of();

        } catch (IOException ex) {
            // Best-effort: se il seed fallisce, il DAO parte con cache vuota.
            return List.of();
        }
    }

    // =========================================================
    // DAO<Integer, RichiestaDisdettaRimborso>
    // =========================================================

    @Override
    public RichiestaDisdettaRimborso load(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        ensureSeeded();

        RichiestaDisdettaRimborso cached = loadFromCache(id);

        if (cached != null) {
            return cached;
        }

        if (Boolean.TRUE.equals(persistent)) {
            return loadFromRawAndCache(id);
        }

        return null;
    }

    /**
     * Recupera una richiesta dalla cache.
     */
    private RichiestaDisdettaRimborso loadFromCache(Integer id) {
        lock.readLock().lock();
        try {
            return cache.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Recupera una richiesta dalla persistenza e aggiorna la cache.
     */
    private RichiestaDisdettaRimborso loadFromRawAndCache(Integer id) {
        RichiestaDisdettaRimborso richiesta = rawLoad(id);

        if (richiesta != null && richiesta.getIdRichiesta() > 0) {
            putInCacheAndUpdateNextId(richiesta);
        }

        return richiesta;
    }

    @Override
    public void store(RichiestaDisdettaRimborso richiesta) {
        if (richiesta == null) {
            return;
        }

        ensureSeeded();
        applyDomainDefaults(richiesta);

        if (Boolean.TRUE.equals(persistent)) {
            storePersistent(richiesta);
            return;
        }

        storeInMemory(richiesta);
    }

    /**
     * Applica valori di default di dominio prima del salvataggio.
     */
    private void applyDomainDefaults(RichiestaDisdettaRimborso richiesta) {
        if (richiesta.getTimestampRichiesta() == null) {
            richiesta.setTimestampRichiesta(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
        }

        if (richiesta.getStato() == null) {
            richiesta.setStato(StatoRichiestaDisdetta.PENDING);
        }
    }

    /**
     * Salva una richiesta in modalità persistente.
     *
     * Il rawStore viene chiamato una sola volta.
     */
    private void storePersistent(RichiestaDisdettaRimborso richiesta) {
        rawStore(richiesta);

        if (richiesta.getIdRichiesta() <= 0) {
            assignFallbackId(richiesta);
        }

        putInCacheAndUpdateNextId(richiesta);
    }

    /**
     * Salva una richiesta in modalità in-memory.
     */
    private void storeInMemory(RichiestaDisdettaRimborso richiesta) {
        lock.writeLock().lock();
        try {
            if (richiesta.getIdRichiesta() <= 0) {
                richiesta.setIdRichiesta(nextId.getAndIncrement());
            }

            cache.put(richiesta.getIdRichiesta(), richiesta);
            updateNextIdIfNeeded(richiesta.getIdRichiesta());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Assegna un id di fallback se il provider persistente non lo valorizza.
     */
    private void assignFallbackId(RichiestaDisdettaRimborso richiesta) {
        lock.writeLock().lock();
        try {
            int id = cache.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0) + 1;

            richiesta.setIdRichiesta(id);
            updateNextIdIfNeeded(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Inserisce una richiesta in cache e aggiorna nextId.
     */
    private void putInCacheAndUpdateNextId(RichiestaDisdettaRimborso richiesta) {
        lock.writeLock().lock();
        try {
            cache.put(richiesta.getIdRichiesta(), richiesta);
            updateNextIdIfNeeded(richiesta.getIdRichiesta());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Aggiorna nextId solo se necessario.
     */
    private void updateNextIdIfNeeded(int id) {
        if (id >= nextId.get()) {
            nextId.set(id + 1);
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            return;
        }

        ensureSeeded();

        lock.writeLock().lock();
        try {
            cache.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

        if (Boolean.TRUE.equals(persistent)) {
            rawDelete(id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        ensureSeeded();

        if (existsInCache(id)) {
            return true;
        }

        if (Boolean.TRUE.equals(persistent)) {
            RichiestaDisdettaRimborso richiesta = rawLoad(id);

            if (richiesta != null && richiesta.getIdRichiesta() > 0) {
                putInCacheAndUpdateNextId(richiesta);
                return true;
            }
        }

        return false;
    }

    /**
     * Controlla se una richiesta è già presente in cache.
     */
    private boolean existsInCache(Integer id) {
        lock.readLock().lock();
        try {
            return cache.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public RichiestaDisdettaRimborso create(Integer id) {
        RichiestaDisdettaRimborso richiesta = new RichiestaDisdettaRimborso();

        if (id != null && id > 0) {
            richiesta.setIdRichiesta(id);
        }

        return richiesta;
    }

    // =========================================================
    // METODI SPECIFICI
    // =========================================================

    @Override
    public List<RichiestaDisdettaRimborso> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            return findAllFromRaw();
        }

        return findAllFromCache();
    }

    /**
     * Recupera tutte le richieste dalla persistenza e riallinea la cache.
     */
    private List<RichiestaDisdettaRimborso> findAllFromRaw() {
        List<RichiestaDisdettaRimborso> rawResult = rawFindAll();
        List<RichiestaDisdettaRimborso> result = new ArrayList<>(rawResult);

        result.sort(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta));
        cacheAll(result);

        return result;
    }

    /**
     * Recupera tutte le richieste dalla cache.
     */
    private List<RichiestaDisdettaRimborso> findAllFromCache() {
        lock.readLock().lock();
        try {
            List<RichiestaDisdettaRimborso> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta));
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Inserisce in cache tutte le richieste valide ricevute dalla persistenza.
     */
    private void cacheAll(List<RichiestaDisdettaRimborso> richieste) {
        lock.writeLock().lock();
        try {
            for (RichiestaDisdettaRimborso richiesta : richieste) {
                if (richiesta != null && richiesta.getIdRichiesta() > 0) {
                    cache.put(richiesta.getIdRichiesta(), richiesta);
                    updateNextIdIfNeeded(richiesta.getIdRichiesta());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<RichiestaDisdettaRimborso> findByStato(StatoRichiestaDisdetta stato) {
        ensureSeeded();

        if (stato == null) {
            return List.of();
        }

        /*
         * Se il DAO è persistente, la fonte autorevole è il DB/FS.
         * findAll() richiama rawFindAll() e riallinea la cache.
         */
        if (Boolean.TRUE.equals(persistent)) {
            return findAll().stream()
                    .filter(richiesta -> richiesta != null && stato.equals(richiesta.getStato()))
                    .sorted(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta))
                    .toList();
        }

        return findByStatoFromCache(stato);
    }

    /**
     * Filtra per stato usando la cache, usato in modalità in-memory.
     */
    private List<RichiestaDisdettaRimborso> findByStatoFromCache(StatoRichiestaDisdetta stato) {
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(richiesta -> richiesta != null && stato.equals(richiesta.getStato()))
                    .sorted(Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public RichiestaDisdettaRimborso findByPrenotazione(int idPrenotazione) {
        ensureSeeded();

        if (idPrenotazione <= 0) {
            return null;
        }

        /*
         * In modalità persistente bisogna leggere tramite findAll().
         * Se leggessimo solo dalla cache, dopo il riavvio la cache sarebbe vuota
         * anche se il database contiene già richieste.
         */
        if (Boolean.TRUE.equals(persistent)) {
            return findAll().stream()
                    .filter(richiesta -> richiesta != null
                            && richiesta.getIdPrenotazione() == idPrenotazione)
                    .findFirst()
                    .orElse(null);
        }

        return findByPrenotazioneFromCache(idPrenotazione);
    }

    /**
     * Cerca una richiesta per prenotazione usando la cache.
     */
    private RichiestaDisdettaRimborso findByPrenotazioneFromCache(int idPrenotazione) {
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(richiesta -> richiesta != null
                            && richiesta.getIdPrenotazione() == idPrenotazione)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updateStato(int idRichiesta,
                            StatoRichiestaDisdetta stato,
                            Integer idGestore,
                            String notaGestore) {
        ensureSeeded();

        if (idRichiesta <= 0 || stato == null) {
            return;
        }

        RichiestaDisdettaRimborso richiesta = load(idRichiesta);

        if (richiesta == null) {
            return;
        }

        updateEntityStato(richiesta, stato, idGestore, notaGestore);
        putInCacheAndUpdateNextId(richiesta);

        if (Boolean.TRUE.equals(persistent)) {
            rawUpdateStato(idRichiesta, stato, idGestore, notaGestore);
        }
    }

    /**
     * Aggiorna lo stato e i metadati decisionali della entity.
     */
    private void updateEntityStato(RichiestaDisdettaRimborso richiesta,
                                   StatoRichiestaDisdetta stato,
                                   Integer idGestore,
                                   String notaGestore) {
        richiesta.setStato(stato);
        richiesta.setTimestampDecisione(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
        richiesta.setIdGestoreDecisione(idGestore);
        richiesta.setNotaGestore(notaGestore);
    }

    /**
     * Utility: pulizia per test e modalità in-memory.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            seeded = false;
            nextId.set(1);
        } finally {
            lock.writeLock().unlock();
        }
    }
}