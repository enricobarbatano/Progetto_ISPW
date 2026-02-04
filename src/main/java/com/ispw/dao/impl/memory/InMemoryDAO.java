package com.ispw.dao.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import com.ispw.dao.interfaces.DAO;

/**
 * Base astratta per DAO in memoria.
 * Supporta:
 *  - store per istanza (default)
 *  - store condiviso per classe DAO (utile se non hai ancora la factory/caching)
 */
public abstract class InMemoryDAO<I, E> implements DAO<I, E> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Storage effettivo. Può essere:
     *  - una Map "per istanza"
     *  - una Map "condivisa per classe DAO"
     */
    protected final Map<I, E> store;

    /**
     * Costruttore default: store per istanza.
     */
    protected InMemoryDAO() {
        this(false);
    }

    /**
     * @param sharedStore se true, tutte le istanze della stessa classe DAO concreta
     *                    (es. InMemoryCampoDAO) condividono lo stesso store.
     */
    protected InMemoryDAO(boolean sharedStore) {
        if (sharedStore) {
            this.store = SharedStoreRegistry.getStoreFor(getClass());
        } else {
            this.store = new ConcurrentHashMap<>();
        }
    }

    /** Ogni DAO concreto deve definire come ricavare la chiave (ID) dall'entità */
    protected abstract I getId(E entity);

    /**
     * (Opzionale) Factory method per create(id).
     * Default: non crea nulla. Override nei concreti se ti serve.
     */
    protected E newEntity(I id) {
        return null;
    }

    // --------------------
    // CRUD base (DAO<I,E>)
    // --------------------

    @Override
    public E load(I id) {
        lock.readLock().lock();
        try {
            return store.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void store(E entity) {
        I id = getId(entity);
        lock.writeLock().lock();
        try {
            store.put(id, entity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(I id) {
        lock.writeLock().lock();
        try {
            store.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(I id) {
        lock.readLock().lock();
        try {
            return store.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    /**
     * Crea una nuova entità con l'ID specificato, se non esiste già nello store.
     * @param id l'identificatore dell'entità da creare
     * @return l'entità creata, o null se newEntity ritorna null
     */
    public E create(I id) {
        lock.writeLock().lock();
        try {
            if (store.containsKey(id)) {
                return store.get(id);
            }
            E e = newEntity(id);
            if (e != null) {
                store.put(id, e);
            }
            return e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // --------------------
    // Utility utili per DAO specifici (findAll / filtri)
    // --------------------

    /**
     * Snapshot consistente dei valori (evita ConcurrentModification e ti dà una lista stabile).
     */
    protected List<E> snapshotValues() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(store.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Filtro in memoria (utile per metodi tipo findByUtente, findByStato, ecc.)
     */
    protected List<E> filter(Predicate<E> predicate) {
        List<E> all = snapshotValues();
        all.removeIf(predicate.negate());
        return all;
    }

    /**
     * Pulisce lo store (utile in test o reset applicativo).
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            store.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // --------------------
    // Shared store registry (per evitare perdita stato senza factory)
    // --------------------
    private static final class SharedStoreRegistry {
        private static final Map<Class<?>, Map<?, ?>> REGISTRY = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        static <K, V> Map<K, V> getStoreFor(Class<?> daoClass) {
            // una Map per ogni DAO concreto
            return (Map<K, V>) REGISTRY.computeIfAbsent(daoClass, k -> new ConcurrentHashMap<>());
        }
    }
}
