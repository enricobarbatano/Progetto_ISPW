package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseRegoleTempisticheDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.RegoleTempistiche;

/**
 * Provider FileSystem JSON per RegoleTempistiche.
 * Gestisce UNA SOLA configurazione.
 */
public class RegoleTempisticheDAOFileSystem extends BaseRegoleTempisticheDAO {

    private final JsonListFileStore<RegoleTempistiche> store;

    public RegoleTempisticheDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("regole_tempistiche.json"),
                new TypeReference<java.util.List<RegoleTempistiche>>() {}
        );
    }

    @Override
    protected RegoleTempistiche rawLoad() {
        // singleton: prima (e unica) entry
        return store.readAll().stream().findFirst().orElse(null);
    }

    @Override
    protected void rawSave(RegoleTempistiche regole) {
        // sovrascrive completamente
        store.writeAll(java.util.List.of(regole));
    }
}