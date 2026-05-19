package com.ispw.dao.impl.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    // Tri-state: null => seed-enabled in-memory
    private final Boolean persistent;

    // Seed-on-first-use state, used only when persistent == null
    private volatile boolean seeded = false;

    /*
     * AtomicInteger evita lo smell Sonar sulle operazioni atomiche.
     * L'accesso resta comunque protetto dai lock nei punti critici,
     * quindi il comportamento del DAO non cambia.
     */
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * Default: IN_MEMORY seeded, quindi persistent == null.
     */
    public BaseCampoDAO() {
        this(null);
    }

    /**
     * Costruttore usato dalle subclass DBMS/FS o dalle varianti in-memory.
     *
     * @param persistent true per DBMS/FS, false per in-memory puro, null per in-memory seeded
     */
    protected BaseCampoDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // =========================================================
    // RAW HOOKS
    // =========================================================
    // Le subclass DBMS/FS sovrascrivono questi metodi per fare I/O reale.
    // La base in-memory lavora solo sulla cache.

    /**
     * Hook per il caricamento da persistenza.
     *
     * La base in-memory non carica da DB/FS, quindi ritorna null.
     */
    @SuppressWarnings("java:S1172")
    protected Campo rawLoad(Integer id) {
        return null;
    }

    /**
     * Hook per il salvataggio su persistenza.
     *
     * La base in-memory salva direttamente in cache, quindi questo metodo è no-op.
     */
    @SuppressWarnings("java:S1172")
    protected void rawStore(Campo entity) {
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
    protected List<Campo> rawFindAll() {
        return List.of();
    }

    // =========================================================
    // SEED LOGIC
    // =========================================================

    /**
     * Carica i campi iniziali da seed/campi.json solo in modalità in-memory seeded.
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

            List<Campo> initial = readSeedCampi();

            for (Campo campo : initial) {
                addSeedCampoIfValid(campo);
            }

            recomputeNextIdUnsafe();
            seeded = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Aggiunge alla cache un campo letto dal seed solo se valido.
     */
    private void addSeedCampoIfValid(Campo campo) {
        if (campo != null && campo.getIdCampo() > 0) {
            cache.put(campo.getIdCampo(), campo);
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
     * Legge i campi di seed da campi.json.
     *
     * Se il file non esiste o la lettura fallisce, ritorna una lista vuota.
     */
    private List<Campo> readSeedCampi() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("campi.json");

            if (!Files.exists(file)) {
                return List.of();
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Campo.class);

            List<Campo> campi = mapper.readValue(file.toFile(), listType);

            return campi != null ? campi : List.of();

        } catch (IOException ex) {
            // Best-effort: se il seed fallisce, il DAO parte con cache vuota.
            return List.of();
        }
    }

    // =========================================================
    // DAO BASE
    // =========================================================

    @Override
    public Campo load(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        ensureSeeded();

        Campo cached = loadFromCache(id);

        if (cached != null) {
            return cached;
        }

        if (Boolean.TRUE.equals(persistent)) {
            return loadFromRawAndCache(id);
        }

        return null;
    }

    /**
     * Recupera un campo dalla cache.
     */
    private Campo loadFromCache(Integer id) {
        lock.readLock().lock();
        try {
            return cache.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Recupera un campo dalla persistenza e aggiorna la cache.
     */
    private Campo loadFromRawAndCache(Integer id) {
        Campo campo = rawLoad(id);

        if (campo != null) {
            putInCacheAndUpdateNextId(campo.getIdCampo(), campo);
        }

        return campo;
    }

    @Override
    public void store(Campo entity) {
        if (entity == null) {
            return;
        }

        ensureSeeded();

        if (isPersistentNewEntity(entity)) {
            storePersistentNewEntity(entity);
            return;
        }

        if (isNewEntity(entity)) {
            storeNewEntityInCache(entity);
            return;
        }

        storeExistingEntity(entity);
    }

    /**
     * Controlla se il campo è nuovo e il DAO lavora su persistenza.
     */
    private boolean isPersistentNewEntity(Campo entity) {
        return entity.getIdCampo() == 0 && Boolean.TRUE.equals(persistent);
    }

    /**
     * Controlla se il campo è nuovo.
     */
    private boolean isNewEntity(Campo entity) {
        return entity.getIdCampo() == 0;
    }

    /**
     * Salva un nuovo campo usando il provider persistente.
     *
     * Il rawStore dovrebbe assegnare l'id in caso di insert.
     * Se non lo assegna, viene usato un fallback coerente con la cache.
     */
    private void storePersistentNewEntity(Campo entity) {
        rawStore(entity);

        int id = entity.getIdCampo();

        if (id <= 0) {
            assignFallbackIdAndCache(entity);
            return;
        }

        putInCacheAndUpdateNextId(id, entity);
    }

    /**
     * Salva un nuovo campo solo in cache.
     *
     * Questo ramo viene usato in modalità in-memory.
     */
    private void storeNewEntityInCache(Campo entity) {
        lock.writeLock().lock();
        try {
            if (nextId.get() <= 0) {
                recomputeNextIdUnsafe();
            }

            int id = nextId.getAndIncrement();
            entity.setIdCampo(id);
            cache.put(id, entity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Salva un campo già esistente.
     *
     * Aggiorna sempre la cache e, se il DAO è persistente,
     * propaga anche la modifica al provider concreto.
     */
    private void storeExistingEntity(Campo entity) {
        int id = entity.getIdCampo();

        putInCacheAndUpdateNextId(id, entity);

        if (Boolean.TRUE.equals(persistent)) {
            rawStore(entity);
        }
    }

    /**
     * Assegna un id di fallback quando il provider persistente
     * non ha valorizzato l'id della nuova entità.
     */
    private void assignFallbackIdAndCache(Campo entity) {
        lock.writeLock().lock();
        try {
            int next = cache.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0) + 1;

            entity.setIdCampo(next);
            cache.put(next, entity);
            updateNextIdIfNeeded(next);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Inserisce un campo in cache e aggiorna nextId se necessario.
     */
    private void putInCacheAndUpdateNextId(int id, Campo entity) {
        lock.writeLock().lock();
        try {
            cache.put(id, entity);
            updateNextIdIfNeeded(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Aggiorna nextId solo se l'id indicato supera o raggiunge il valore corrente.
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
            Campo campo = rawLoad(id);

            if (campo != null) {
                putInCacheAndUpdateNextId(campo.getIdCampo(), campo);
                return true;
            }
        }

        return false;
    }

    /**
     * Controlla se un id è presente nella cache.
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
    public Campo create(Integer id) {
        Campo campo = new Campo();

        if (id != null && id > 0) {
            campo.setIdCampo(id);
        }

        return campo;
    }

    @Override
    public List<Campo> findAll() {
        ensureSeeded();

        if (Boolean.TRUE.equals(persistent)) {
            return findAllFromRaw();
        }

        return findAllFromCache();
    }

    /**
     * Recupera tutti i campi dalla persistenza e aggiorna la cache.
     */
    private List<Campo> findAllFromRaw() {
        List<Campo> result = rawFindAll();

        if (result == null) {
            return new ArrayList<>();
        }

        result.sort(Comparator.comparingInt(Campo::getIdCampo));
        cacheAll(result);

        return result;
    }

    /**
     * Recupera tutti i campi dalla cache.
     */
    private List<Campo> findAllFromCache() {
        lock.readLock().lock();
        try {
            List<Campo> out = new ArrayList<>(cache.values());
            out.sort(Comparator.comparingInt(Campo::getIdCampo));
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Inserisce in cache tutti i campi validi ricevuti dalla persistenza.
     */
    private void cacheAll(List<Campo> campi) {
        lock.writeLock().lock();
        try {
            for (Campo campo : campi) {
                if (campo != null) {
                    cache.put(campo.getIdCampo(), campo);
                    updateNextIdIfNeeded(campo.getIdCampo());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }

    /**
     * Compatibilità: pulisce la cache, usato dai test tramite reflection.
     * Resetta anche seeding e nextId.
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