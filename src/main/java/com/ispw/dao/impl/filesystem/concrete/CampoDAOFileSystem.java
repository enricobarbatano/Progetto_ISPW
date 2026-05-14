package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.Campo;

public class CampoDAOFileSystem extends BaseCampoDAO {

    private final JsonListFileStore<Campo> campoStore;

    public CampoDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.campoStore = new JsonListFileStore<>(
                storageDir.resolve("campi.json"),
                new TypeReference<List<Campo>>() {}
        );
    }

    @Override
    protected Campo rawLoad(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        return campoStore.readAll().stream()
                .filter(c -> c != null && c.getIdCampo() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<Campo> rawFindAll() {
        return campoStore.readAll();
    }

    @Override
    protected void rawStore(Campo entity) {
        if (entity == null) {
            return;
        }

        List<Campo> all = campoStore.readAll();
        all.removeIf(c -> c != null && c.getIdCampo() == entity.getIdCampo());
        all.add(entity);
        campoStore.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) {
            return;
        }

        List<Campo> all = campoStore.readAll();
        all.removeIf(c -> c != null && c.getIdCampo() == id);
        campoStore.writeAll(all);
    }
}