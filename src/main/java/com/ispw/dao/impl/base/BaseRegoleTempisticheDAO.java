package com.ispw.dao.impl.base;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

/**
 * Base concreta del DAO RegoleTempistiche.
 *
 * Semantica:
 * - esiste UNA SOLA configurazione nel sistema (singleton logico)
 * - cache-first
 *
 * Tri-state persistence flag:
 * - persistent == TRUE  : DBMS/FS (rawLoad/rawSave)
 * - persistent == FALSE : IN_MEMORY puro (no seed)
 * - persistent == NULL  : IN_MEMORY seeded (load once from seed/regole_tempistiche.json; never persist back)
 */
public class BaseRegoleTempisticheDAO implements RegoleTempisticheDAO {

    /** Cache della configurazione unica */
    protected final AtomicReference<RegoleTempistiche> cache = new AtomicReference<>();

    /** Tri-state persistency */
    private final Boolean persistent;

    // seed-on-first-use (solo quando persistent == null)
    private volatile boolean seeded = false;

    /** Default: IN_MEMORY seeded (persistent == null) */
    public BaseRegoleTempisticheDAO() {
        this(null);
    }

    protected BaseRegoleTempisticheDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS (I/O)
    // -----------------------
    protected RegoleTempistiche rawLoad() { return null; }
    protected void rawSave(RegoleTempistiche regole) { /* no-op per base */ }

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

            RegoleTempistiche seed = readSeedRegoleTempistiche();
            if (seed != null) {
                seed.setIdConfig(1);
                cache.set(seed);
            }

            seeded = true;
        }
    }

    private RegoleTempistiche readSeedRegoleTempistiche() {
        try {
            Path root = DAOFactory.getSeedRootOrDefault();
            Path file = root.resolve("regole_tempistiche.json");
            if (!Files.exists(file)) return null;

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            // 1) prova come oggetto singolo
            try {
                return om.readValue(file.toFile(), RegoleTempistiche.class);
            } catch (Exception ignore) {
                // 2) fallback: prova come lista e prendi il primo
                CollectionType listType = om.getTypeFactory()
                        .constructCollectionType(List.class, RegoleTempistiche.class);
                List<RegoleTempistiche> list = om.readValue(file.toFile(), listType);
                return (list == null || list.isEmpty()) ? null : list.get(0);
            }
        } catch (Exception ex) {
            return null; // best-effort
        }
    }

    // -----------------------
    // API RegoleTempisticheDAO
    // -----------------------
    @Override
    public RegoleTempistiche get() {
        ensureSeeded();

        // 1) cache-first
        RegoleTempistiche cached = cache.get();
        if (cached != null) {
            return cached;
        }

        // 2) fallback raw (solo se persistente)
        if (Boolean.TRUE.equals(persistent)) {
            RegoleTempistiche loaded = rawLoad();
            if (loaded != null) {
                loaded.setIdConfig(1);
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

        ensureSeeded();

        regole.setIdConfig(1);
        cache.set(regole);

        if (Boolean.TRUE.equals(persistent)) {
            rawSave(regole);
        }
    }

    /** Utility test */
    public void clear() {
        cache.set(null);
        seeded = false;
    }
}