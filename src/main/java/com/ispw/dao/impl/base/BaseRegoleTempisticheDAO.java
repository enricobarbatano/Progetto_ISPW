package com.ispw.dao.impl.base;

import java.util.concurrent.atomic.AtomicReference;

import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

/**
 * Base concreta del DAO RegoleTempistiche.
 *
 * Semantica:
 * - esiste UNA SOLA configurazione nel sistema (singleton logico)
 * - cache-first
 * - IN_MEMORY se persistent=false
 * - DBMS/FS se persistent=true
 */
public class BaseRegoleTempisticheDAO implements RegoleTempisticheDAO {

    /** Cache della configurazione unica */
    protected final AtomicReference<RegoleTempistiche> cache = new AtomicReference<>();

    /** Indica se il provider è persistente (DB/FS) */
    private final boolean persistent;

    public BaseRegoleTempisticheDAO() {
        this(false);
    }

    protected BaseRegoleTempisticheDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS (I/O)
    // -----------------------

    /**
     * Carica la configurazione dal provider persistente.
     * @return RegoleTempistiche oppure null se non configurata
     */
    protected RegoleTempistiche rawLoad() {
        return null;
    }

    /**
     * Salva la configurazione nel provider persistente.
     * Deve sovrascrivere l'eventuale configurazione esistente.
     */
    protected void rawSave(RegoleTempistiche regole) {
        // no-op per IN_MEMORY
    }

    // -----------------------
    // API RegoleTempisticheDAO
    // -----------------------

    @Override
    public RegoleTempistiche get() {
        // 1) cache-first
        RegoleTempistiche cached = cache.get();
        if (cached != null) {
            return cached;
        }

        // 2) fallback raw
        if (persistent) {
            RegoleTempistiche loaded = rawLoad();
            if (loaded != null) {
                loaded.setIdConfig(1); // forziamo id logico
                cache.set(loaded);
            }
            return loaded;
        }

        return null;
    }

    @Override
    public void save(RegoleTempistiche regole) {
        if (regole == null) {
            throw new IllegalArgumentException("regole non possono essere null");
        }

        // id unico forzato
        regole.setIdConfig(1);

        // 1) cache
        cache.set(regole);

        // 2) persistenza
        if (persistent) {
            rawSave(regole);
        }
    }

    /** Utility test */
    public void clear() {
        cache.set(null);
    }
}