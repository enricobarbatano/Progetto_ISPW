package com.ispw.dao.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import com.ispw.dao.interfaces.DAO;

public abstract class InMemoryDAO<I, E> implements DAO<I, E> {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: base DAO in memoria.
    // A2) IO: CRUD thread-safe su Map.
    // ========================

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected final Map<I, E> store;

    protected InMemoryDAO() {
        this(false);
    }

    protected InMemoryDAO(boolean sharedStore) {
        if (sharedStore) {
            this.store = SharedStoreRegistry.getStoreFor(getClass());
        } else {
            this.store = new ConcurrentHashMap<>();
        }
    }

    protected abstract I getId(E entity);

    protected E newEntity() {
        return null;
    }

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) CRUD base: load/store/delete/exists/create.
    // L2) snapshotValues/filter/clear: utility per DAO concreti.
    // L3) SharedStoreRegistry: store condiviso opzionale.
    // ========================

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
    public E create(I id) {
        lock.writeLock().lock();
        try {
            if (store.containsKey(id)) {
                return store.get(id);
            }
            E e = newEntity();
            if (e != null) {
                store.put(id, e);
            }
            return e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected List<E> snapshotValues() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(store.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    protected List<E> filter(Predicate<E> predicate) {
        List<E> all = snapshotValues();
        all.removeIf(predicate.negate());
        return all;
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            store.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static final class SharedStoreRegistry {
        private static final Map<Class<?>, Map<?, ?>> REGISTRY = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        static <K, V> Map<K, V> getStoreFor(Class<?> daoClass) {
            // una Map per ogni DAO concreto
            return (Map<K, V>) REGISTRY.computeIfAbsent(daoClass, k -> new ConcurrentHashMap<>());
        }
    }
}
