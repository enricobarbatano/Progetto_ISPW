package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseUtenteFinaleDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.UtenteFinale;

public class UtenteFinaleDAOFileSystem extends BaseUtenteFinaleDAO {

    private final JsonListFileStore<UtenteFinale> store;

    public UtenteFinaleDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("utenti_finali.json"),
                new TypeReference<List<UtenteFinale>>() {}
        );
    }

    
    @Override
    protected List<UtenteFinale> rawFindAll() {
        return new ArrayList<>(store.readAll());
    }


    @Override
    protected UtenteFinale rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        return store.readAll().stream()
                .filter(u -> u.getIdUtente() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected UtenteFinale rawFindByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null) return null;

        return store.readAll().stream()
                .filter(u -> u.getEmail() != null &&
                             u.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void rawStore(UtenteFinale entity) {
        if (entity == null) return;

        List<UtenteFinale> all = new ArrayList<>(store.readAll());

        if (entity.getIdUtente() == 0) {
            int next = all.stream().mapToInt(UtenteFinale::getIdUtente).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }

        all.removeIf(u -> u.getIdUtente() == entity.getIdUtente());
        all.add(entity);
        store.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        List<UtenteFinale> all = new ArrayList<>(store.readAll());
        all.removeIf(u -> u.getIdUtente() == id);
        store.writeAll(all);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}