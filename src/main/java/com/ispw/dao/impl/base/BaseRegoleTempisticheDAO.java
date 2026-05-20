package com.ispw.dao.impl.base;

import java.io.IOException;
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
 * - esiste una sola configurazione nel sistema;
 * - cache-first;
 * - seed solo in modalità IN_MEMORY seeded.
 */
public class BaseRegoleTempisticheDAO implements RegoleTempisticheDAO {

    protected final AtomicReference<RegoleTempistiche> cache = new AtomicReference<>();

    private final Boolean persistent;

    private volatile boolean seeded = false;

    public BaseRegoleTempisticheDAO() {
        this(null);
    }

    protected BaseRegoleTempisticheDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS
    // -----------------------

    protected RegoleTempistiche rawLoad() {
        return null;
    }

    protected void rawSave(RegoleTempistiche regole) {
        // no-op per base in-memory
    }

    // -----------------------
    // Seed logic
    // -----------------------

    private void ensureSeeded() {
        if (persistent != null) return;
        if (seeded) return;

        synchronized (this) {
            if (seeded) return;

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

            if (!Files.exists(file)) {
                return null;
            }

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            RegoleTempistiche single = readSingleRegoleTempistiche(om, file);

            if (single != null) {
                return single;
            }

            return readFirstRegoleTempisticheFromList(om, file);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Primo tentativo: il seed è salvato come oggetto singolo.
     */
    private RegoleTempistiche readSingleRegoleTempistiche(ObjectMapper om, Path file) {
        try {
            return om.readValue(file.toFile(), RegoleTempistiche.class);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Secondo tentativo: il seed è salvato come lista e viene preso il primo elemento.
     */
    private RegoleTempistiche readFirstRegoleTempisticheFromList(ObjectMapper om, Path file)
            throws IOException {
        CollectionType listType = om.getTypeFactory()
                .constructCollectionType(List.class, RegoleTempistiche.class);

        List<RegoleTempistiche> list = om.readValue(file.toFile(), listType);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    // -----------------------
    // API RegoleTempisticheDAO
    // -----------------------

    @Override
    public RegoleTempistiche get() {
        ensureSeeded();

        RegoleTempistiche cached = cache.get();

        if (cached != null) {
            return cached;
        }

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

    public void clear() {
        cache.set(null);
        seeded = false;
    }
}