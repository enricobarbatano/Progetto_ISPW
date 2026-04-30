package com.ispw.dao.impl.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

/**
 * Base concrete UtenteFinale DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 */
public class BaseUtenteFinaleDAO implements UtenteFinaleDAO {

    protected final Map<Integer, UtenteFinale> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BaseUtenteFinaleDAO() { this(false); }

    protected BaseUtenteFinaleDAO(boolean persistent) { this.persistent = persistent; }

    //i metodi raw sono quelli che devono essere implementati dai dao persistenti per accedere al database o al filesystem,
    //  mentre i metodi load/store/delete/findByEmail/findAll implementano la logica di caching e delegano ai raw quando necessario
    protected UtenteFinale rawLoad(Integer id) { return null; }
    protected void rawStore(UtenteFinale entity) { }
    protected void rawDelete(Integer id) { }
    protected UtenteFinale rawFindByEmail(String email) { return null; }
    protected List<UtenteFinale> rawFindAll() { return null; }

    @Override
    public UtenteFinale load(Integer id) {
        if (id == null || id <= 0) return null;
        lock.readLock().lock();
        try {
            UtenteFinale cached = cache.get(id);
            if (cached != null) return cached;
        } finally { lock.readLock().unlock(); }

        if (persistent) {
            UtenteFinale u = rawLoad(id);
            if (u != null && u.getIdUtente() > 0) {
                lock.writeLock().lock();
                try { cache.put(u.getIdUtente(), u); } finally { lock.writeLock().unlock(); }
            }
            return u;
        }
        return null;
    }


    // findAll è metodo che serve per esportare tutti gli utenti finali
    @Override
    public List<UtenteFinale> findAll() {
    if (persistent) {
        List<UtenteFinale> res = rawFindAll();
        if (res == null) return new ArrayList<>();

        lock.writeLock().lock();
        try {
            for (UtenteFinale u : res) {
                if (u != null && u.getIdUtente() > 0) {
                    cache.put(u.getIdUtente(), u);
                }
            }
        } finally { lock.writeLock().unlock(); }

        return res;
    }

    lock.readLock().lock();
    try {
        return new ArrayList<>(cache.values());
    } finally { lock.readLock().unlock(); }
    }


    @Override
    public void store(UtenteFinale entity) {
        if (entity == null) return;
        if (entity.getIdUtente() == 0) {
            if (persistent) {
                rawStore(entity);
                int id = entity.getIdUtente();
                if (id <= 0) {
                    lock.writeLock().lock();
                    try {
                        int next = cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                        entity.setIdUtente(next);
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
                    entity.setIdUtente(next);
                    cache.put(next, entity);
                } finally { lock.writeLock().unlock(); }
                return;
            }
        }

        int id = entity.getIdUtente();
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
            UtenteFinale u = rawLoad(id);
            if (u != null) {
                if (u.getIdUtente() > 0) {
                    lock.writeLock().lock();
                    try { cache.put(u.getIdUtente(), u); } finally { lock.writeLock().unlock(); }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public UtenteFinale create(Integer id) {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    @Override
    public UtenteFinale findById(int idUtente) { return load(idUtente); }

    @Override
    public UtenteFinale findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        lock.readLock().lock();
        try {
            Optional<UtenteFinale> fromCache = cache.values().stream()
                .filter(u -> {
                    if (u == null) return false;
                    String e = u.getEmail(); if (e == null) return false;
                    String en = normalizeEmail(e); return en != null && en.equals(norm);
                }).findFirst();
            if (fromCache.isPresent()) return fromCache.get();
        } finally { lock.readLock().unlock(); }

        if (persistent) {
            UtenteFinale u = rawFindByEmail(norm);
            if (u != null && u.getIdUtente() > 0) {
                lock.writeLock().lock();
                try { cache.put(u.getIdUtente(), u); } finally { lock.writeLock().unlock(); }
            }
            return u;
        }
        return null;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void clear() {
        lock.writeLock().lock();
        try { cache.clear(); } finally { lock.writeLock().unlock(); }
    }
}
