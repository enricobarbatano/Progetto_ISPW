package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseLogDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.SystemLog;

public class LogDAOFileSystem extends BaseLogDAO {

    private final JsonListFileStore<SystemLog> store;

    public LogDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("system_log.json"),
                new TypeReference<List<SystemLog>>() {}
        );
    }

    @Override
    protected void rawAppend(SystemLog log) {
        List<SystemLog> all = new ArrayList<>(store.readAll());

        if (log.getIdLog() == 0) {
            int next = all.stream().mapToInt(SystemLog::getIdLog).max().orElse(0) + 1;
            log.setIdLog(next);
        }

        all.add(log);
        store.writeAll(all);
    }

    @Override
    protected SystemLog rawLoad(Integer id) {
        return store.readAll().stream()
                .filter(l -> l.getIdLog() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<SystemLog> rawFindByUtente(int idUtente) {
        List<SystemLog> out = new ArrayList<>();
        for (SystemLog l : store.readAll()) {
            if (l.getIdUtenteCoinvolto() == idUtente) out.add(l);
        }
        return out;
    }

    @Override
    protected List<SystemLog> rawFindLast(int limit) {
        return store.readAll().stream()
                .sorted(BaseLogDAO.ORDER_BY_TS_DESC_ID_DESC)
                .limit(Math.max(1, limit))
                .toList();
    }
}