package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseGeneralUserDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.UtenteFinale;

public class GeneralUserDAOFileSystem extends BaseGeneralUserDAO {

    private final JsonListFileStore<GeneralUser> store;

    public GeneralUserDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("general_users.json"),
                new TypeReference<List<GeneralUser>>() {}
        );
    }

    @Override
    protected GeneralUser rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        return store.readAll().stream()
                .filter(u -> u.getIdUtente() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<GeneralUser> rawFindAll() {
        return new ArrayList<>(store.readAll());
    }

    @Override
    protected GeneralUser rawFindByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null) return null;

        return store.readAll().stream()
                .filter(u -> u.getEmail() != null &&
                             u.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void rawStore(GeneralUser entity) {
        if (entity == null) return;

        List<GeneralUser> all = new ArrayList<>(store.readAll());

        if (entity.getIdUtente() == 0) {
            int next = all.stream().mapToInt(GeneralUser::getIdUtente).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }

        all.removeIf(u -> u.getIdUtente() == entity.getIdUtente());
        all.add(entity);
        store.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        List<GeneralUser> all = new ArrayList<>(store.readAll());
        all.removeIf(u -> u.getIdUtente() == id);
        store.writeAll(all);
    }

    @Override
    public GeneralUser create(Integer id) {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}