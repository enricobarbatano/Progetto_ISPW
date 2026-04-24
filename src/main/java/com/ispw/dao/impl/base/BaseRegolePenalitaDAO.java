package com.ispw.dao.impl.base;

import java.util.concurrent.atomic.AtomicReference;

import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

/**
 * Base concreta del DAO RegolePenalita.
 *
 * Semantica:
 * - ESISTE UNA SOLA configurazione nel sistema (singleton logico)
 * - cache-first
 * - IN_MEMORY se persistent=false
 * - DBMS/FS se persistent=true
 */
public class BaseRegolePenalitaDAO implements RegolePenalitaDAO {

    /** Cache della configurazione unica */
    protected final AtomicReference<RegolePenalita> cache = new AtomicReference<>();

    /**
     * Indica se il provider è persistente (DB/FS) oppure IN_MEMORY.
     */
    private final boolean persistent;

    public BaseRegolePenalitaDAO() {
        this(false);
    }

    protected BaseRegolePenalitaDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS (I/O)
    // Subclass DBMS/FS devono implementare questi metodi
    // -----------------------

    /**
     * Carica la configurazione dal provider persistente.
     * Deve restituire:
     * - l'istanza di RegolePenalita
     * - oppure null se non ancora configurata
     */
    protected RegolePenalita rawLoad() {
        return null;
    }

    /**
     * Salva la configurazione nel provider persistente.
     * Deve sovrascrivere l'eventuale configurazione esistente.
     */
    protected void rawSave(RegolePenalita regole) {
        // no-op per IN_MEMORY
    }

    // -----------------------
    // API RegolePenalitaDAO
    // -----------------------

    @Override
    public RegolePenalita get() {
        // 1) cache-first
        RegolePenalita cached = cache.get();
        if (cached != null) {
            return cached;
        }

        // 2) fallback al provider persistente
        if (persistent) {
            RegolePenalita loaded = rawLoad();
            if (loaded != null) {
                // forziamo idConfig=1 per coerenza architetturale
                loaded.setIdConfig(1);
                cache.set(loaded);
            }
            return loaded;
        }

        // IN_MEMORY e cache vuota
        return null;
    }

    @Override
    public void save(RegolePenalita regole) {
        if (regole == null) {
            throw new IllegalArgumentException("regole non possono essere null");
        }

        // Forziamo l'identificativo unico
        regole.setIdConfig(1);

        // 1) aggiorna cache
        cache.set(regole);

        // 2) persistenza se configurata
        if (persistent) {
            rawSave(regole);
        }
    }

    /**
     * Utility per test: pulisce la cache.
     * Non cancella la configurazione persistente.
     */
    public void clear() {
        cache.set(null);
    }
}
