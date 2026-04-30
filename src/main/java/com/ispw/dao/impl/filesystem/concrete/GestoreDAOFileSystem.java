package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseGestoreDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

public class GestoreDAOFileSystem extends BaseGestoreDAO {

    private final JsonListFileStore<Gestore> store;

    public GestoreDAOFileSystem(Path storageDir) {
        super(true);
        try { Files.createDirectories(storageDir); }
        catch (IOException e) { throw new DaoException("Impossibile creare directory storage", e); }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("gestori.json"),
                new TypeReference<List<Gestore>>() {}
        );
    }

    // ================= RAW IMPLEMENTATION =================
    @Override
    protected List<Gestore> rawFindAll() {
        return new ArrayList<>(store.readAll());
    }

    @Override
    protected Gestore rawLoad(Integer id) {
        return store.readAll().stream()
                .filter(g -> g.getIdUtente() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected Gestore rawFindByEmail(String email) {
        final String norm = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        if (norm == null) return null;

        return store.readAll().stream()
                .filter(g -> g.getEmail() != null &&
                             g.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void rawStore(Gestore entity) {
        List<Gestore> all = new ArrayList<>(store.readAll());

        if (entity.getIdUtente() == 0) {
            int next = all.stream().mapToInt(Gestore::getIdUtente).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }

        all.removeIf(g -> g.getIdUtente() == entity.getIdUtente());
        all.add(entity);
        store.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        List<Gestore> all = new ArrayList<>(store.readAll());
        all.removeIf(g -> g.getIdUtente() == id);
        store.writeAll(all);
    }

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        Gestore g = load(idGestore);
        return (g == null) ? Set.of() : Set.copyOf(g.getPermessi());
    }
}
