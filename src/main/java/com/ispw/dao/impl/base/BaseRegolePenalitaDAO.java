package com.ispw.dao.impl.base;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

/**
 * Base concreta del DAO RegolePenalita.
 *
 * Semantica:
 * - ESISTE UNA SOLA configurazione nel sistema (singleton logico)
 * - cache-first
 *
 * Tri-state persistence flag:
 * - persistent == TRUE  : DBMS/FS (rawLoad/rawSave)
 * - persistent == FALSE : IN_MEMORY puro (no seed)
 * - persistent == NULL  : IN_MEMORY seeded (load once from seed/regole_penalita.json; never persist back)
 */
public class BaseRegolePenalitaDAO implements RegolePenalitaDAO {

    protected final AtomicReference<RegolePenalita> cache = new AtomicReference<>();

    private final Boolean persistent;

    private volatile boolean seeded = false;

    /** Default: IN_MEMORY seeded (persistent == null) */
    public BaseRegolePenalitaDAO() {
        this(null);
    }

    protected BaseRegolePenalitaDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS (I/O)
    // -----------------------
    protected RegolePenalita rawLoad() { return null; }
    protected void rawSave(RegolePenalita regole) { /* no-op per base */ }

    // -----------------------
    // Seed logic (ONLY when persistent == null)
    // -----------------------
    private void ensureSeeded() {
        if (persistent != null) return;
        if (seeded) return;

        synchronized (this) {
            if (seeded) return;

            // ✅ prima controlla cache
            if (cache.get() != null) {
                seeded = true;
                return;
            }

            RegolePenalita seed = readSeedRegolePenalita();
            if (seed != null) {
                seed.setIdConfig(1);
                cache.set(seed);
            }

            seeded = true;
        }
    }

    private RegolePenalita readSeedRegolePenalita() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("regole_penalita.json");
            if (!Files.exists(file)) return null;

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            // 1) prova come oggetto singolo
            try {
                return om.readValue(file.toFile(), RegolePenalita.class);
            } catch (Exception ignore) {
                // 2) fallback: prova come lista e prendi il primo
                CollectionType listType = om.getTypeFactory()
                        .constructCollectionType(List.class, RegolePenalita.class);
                List<RegolePenalita> list = om.readValue(file.toFile(), listType);
                return (list == null || list.isEmpty()) ? null : list.get(0);
            }
        } catch (Exception ex) {
            return null; // best-effort
        }
    }

    // -----------------------
    // API RegolePenalitaDAO
    // -----------------------
    @Override
    public RegolePenalita get() {
        ensureSeeded();

        RegolePenalita cached = cache.get();
        if (cached != null) {
            return cached;
        }

        if (Boolean.TRUE.equals(persistent)) {
            RegolePenalita loaded = rawLoad();
            if (loaded != null) {
                loaded.setIdConfig(1);
                cache.set(loaded);
            }
            return loaded;
        }

        return null;
    }

    @Override
    public void save(RegolePenalita regole) {
        if (regole == null) {
            throw new IllegalArgumentException("regole non possono essere null");
        }

        ensureSeeded();

        regole.setIdConfig(1);
        cache.set(regole);

        if (Boolean.TRUE.equals(persistent)) {
            rawSave(regole);
        }
    }

    public void clear() {
        cache.set(null);
        seeded = false;
    }
}