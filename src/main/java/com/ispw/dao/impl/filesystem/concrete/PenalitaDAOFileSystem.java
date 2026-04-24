
package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePenalitaDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.Penalita;

public class PenalitaDAOFileSystem extends BasePenalitaDAO {

    private final JsonListFileStore<Penalita> store;

    public PenalitaDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("penalita.json"),
                new TypeReference<List<Penalita>>() {}
        );
    }

    private Map<Integer, Penalita> readAllAsMap() {
        Map<Integer, Penalita> map = new ConcurrentHashMap<>();
        for (Penalita p : store.readAll()) {
            if (p != null && p.getIdPenalita() > 0) map.put(p.getIdPenalita(), p);
        }
        return map;
    }

    @Override
    protected Penalita rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        return readAllAsMap().get(id);
    }

    @Override
    protected List<Penalita> rawFindByUtente(int idUtente) {
        List<Penalita> out = new ArrayList<>();
        for (Penalita p : store.readAll()) {
            if (p != null && p.getIdUtente() == idUtente) out.add(p);
        }
        return out;
    }

    @Override
    protected void rawStore(Penalita entity) {
        if (entity == null) return;

        List<Penalita> all = store.readAll();
        if (entity.getIdPenalita() == 0) {
            int next = all.stream().mapToInt(Penalita::getIdPenalita).max().orElse(0) + 1;
            entity.setIdPenalita(next);
        }

        all.removeIf(p -> p.getIdPenalita() == entity.getIdPenalita());
        all.add(entity);
        store.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) return;
        List<Penalita> all = store.readAll();
        all.removeIf(p -> p.getIdPenalita() == id);
        store.writeAll(all);
    }
}
