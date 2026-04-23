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
import com.ispw.dao.impl.base.BaseFatturaDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore; // adatta al tuo package reale
import com.ispw.model.entity.Fattura;

/**
 * Provider FileSystem JSON per Fattura: raw-only.
 * Persiste una lista su 'fatture.json' (1 file per entità).
 */
public class FatturaDAOFileSystem extends BaseFatturaDAO {

    private final Path filePath;
    private final JsonListFileStore<Fattura> jsonStore;

    public FatturaDAOFileSystem(Path storageDir) {
        super(true);

        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage: " + storageDir, e);
        }

        this.filePath = storageDir.resolve("fatture.json");

        // ordine stabile sul file (opzionale): per id crescente
        this.jsonStore = new JsonListFileStore<>(
                filePath,
                new TypeReference<List<Fattura>>() {},
                java.util.Comparator.comparingInt(Fattura::getIdFattura)
        );
    }

    private Map<Integer, Fattura> readAllAsMap() {
        List<Fattura> list = jsonStore.readAll();
        Map<Integer, Fattura> map = new ConcurrentHashMap<>();
        for (Fattura f : list) {
            if (f != null && f.getIdFattura() > 0) {
                map.put(f.getIdFattura(), f);
            }
        }
        return map;
    }

    private void writeAllFromMap(Map<Integer, Fattura> data) {
        jsonStore.writeAll(new ArrayList<>(data.values()));
    }

    @Override
    protected Fattura rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        return readAllAsMap().get(id);
    }

    @Override
    protected void rawStore(Fattura entity) {
        if (entity == null) return;

        Map<Integer, Fattura> data = readAllAsMap();

        // FS: se id==0 assegna max+1
        if (entity.getIdFattura() == 0) {
            int next = data.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdFattura(next);
        }

        data.put(entity.getIdFattura(), entity);
        writeAllFromMap(data);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) return;

        Map<Integer, Fattura> data = readAllAsMap();
        data.remove(id);
        writeAllFromMap(data);
    }

    @Override
    protected Fattura rawFindLastByUtente(int idUtente) {
        if (idUtente <= 0) return null;

        Map<Integer, Fattura> data = readAllAsMap();

        // “ultima fattura” = max per data desc + id desc
        return data.values().stream()
                .filter(f -> f != null && f.getIdUtente() == idUtente)
                .sorted(ORDER_LAST_BY_DATE_ID_DESC)
                .findFirst()
                .orElse(null);
    }
}