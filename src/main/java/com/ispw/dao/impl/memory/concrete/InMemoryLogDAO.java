package com.ispw.dao.impl.memory.concrete;

import java.util.Comparator;
import java.util.List;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

public class InMemoryLogDAO extends In_MemoryDAO<Integer, SystemLog> implements LogDAO {

    public InMemoryLogDAO() {
        super(true);
    }

    @Override
    protected Integer getId(SystemLog entity) {
        // TODO: usa il tuo getter reale (es. entity.getIdLog())
        throw new UnsupportedOperationException("TODO: SystemLog.getIdLog()");
    }

    @Override
    public void append(SystemLog log) {
        // append-only: in pratica store (se vuoi davvero append-only, usa un id sempre nuovo)
        store(log);
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        // TODO: sostituisci con getter reale (es. log.getIdUtenteCoinvolto())
        return filter(l -> l.getIdUtenteCoinvolto() == idUtente);
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        // TODO: se hai timestamp, ordina per timestamp; altrimenti per id
        return snapshotValues().stream()
                .sorted(Comparator.comparing(SystemLog::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }
}
