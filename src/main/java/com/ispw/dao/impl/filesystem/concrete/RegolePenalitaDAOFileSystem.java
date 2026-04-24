package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseRegolePenalitaDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.RegolePenalita;

/**
 * Provider FileSystem JSON per RegolePenalita.
 * Gestisce UNA SOLA configurazione persistita su file.
 */
public class RegolePenalitaDAOFileSystem extends BaseRegolePenalitaDAO {

    private final JsonListFileStore<RegolePenalita> store;

    public RegolePenalitaDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("regole_penalita.json"),
                new TypeReference<java.util.List<RegolePenalita>>() {}
        );
    }

    @Override
    protected RegolePenalita rawLoad() {
        // esiste UNA sola configurazione → prendiamo la prima (se esiste)
        return store.readAll().stream().findFirst().orElse(null);
    }

    @Override
    protected void rawSave(RegolePenalita regole) {
        // sovrascriviamo completamente (singleton)
        store.writeAll(java.util.List.of(regole));
    }
}
