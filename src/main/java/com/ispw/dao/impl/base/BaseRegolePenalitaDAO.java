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
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

/**
 * Base concreta del DAO RegolePenalita.
 *
 * Semantica:
 * - esiste una sola configurazione nel sistema;
 * - cache-first;
 * - seed solo in modalità IN_MEMORY seeded.
 */
public class BaseRegolePenalitaDAO implements RegolePenalitaDAO {

    protected final AtomicReference<RegolePenalita> cache = new AtomicReference<>();

    private final Boolean persistent;

    private volatile boolean seeded = false;

    public BaseRegolePenalitaDAO() {
        this(null);
    }

    protected BaseRegolePenalitaDAO(Boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // RAW HOOKS
    // -----------------------

    protected RegolePenalita rawLoad() {
        return null;
    }

    protected void rawSave(RegolePenalita regole) {
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

            if (!Files.exists(file)) {
                return null;
            }

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            RegolePenalita single = readSingleRegolePenalita(om, file);

            if (single != null) {
                return single;
            }

            return readFirstRegolePenalitaFromList(om, file);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Primo tentativo: il seed è salvato come oggetto singolo.
     */
    private RegolePenalita readSingleRegolePenalita(ObjectMapper om, Path file) {
        try {
            return om.readValue(file.toFile(), RegolePenalita.class);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Secondo tentativo: il seed è salvato come lista e viene preso il primo elemento.
     */
    private RegolePenalita readFirstRegolePenalitaFromList(ObjectMapper om, Path file)
            throws IOException {
        CollectionType listType = om.getTypeFactory()
                .constructCollectionType(List.class, RegolePenalita.class);

        List<RegolePenalita> list = om.readValue(file.toFile(), listType);
        return (list == null || list.isEmpty()) ? null : list.get(0);
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