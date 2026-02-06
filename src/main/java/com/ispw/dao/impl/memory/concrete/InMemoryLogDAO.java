package com.ispw.dao.impl.memory.concrete;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO InMemory per SystemLog.
 * Responsabilita': gestire persistenza volatile per test/uso locale.
 *
 * SEZIONE LOGICA
 * Delega a InMemoryDAO e ordina i log per timestamp.
 */
public final class InMemoryLogDAO extends InMemoryDAO<Integer, SystemLog> implements LogDAO {

    private static final int MIN_LIMIT = 1;

    /**
     * Ordina per timestamp DESC (null last) e, a parità, per id DESC.
     */
    private static final Comparator<SystemLog> ORDER_BY_TS_DESC_ID_DESC =
            Comparator.comparing(SystemLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(SystemLog::getIdLog)
                      .reversed();

    public InMemoryLogDAO() {
        super(true); // store condiviso per classe concreta
    }

    @Override
    protected Integer getId(SystemLog entity) {
        return entity.getIdLog();
    }

    @Override
    public void append(SystemLog log) {
        Objects.requireNonNull(log, "log non può essere null");
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        if (log.getIdLog() == 0) {
            final int next = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            log.setIdLog(next);
        }
        super.store(log); // append
    }

    /** Append-only: store delega ad append (no update). */
    @Override
    public void store(SystemLog entity) {
        append(entity);
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        return snapshotValues().stream()
                .filter(l -> l.getIdUtenteCoinvolto() == idUtente)
                .sorted(ORDER_BY_TS_DESC_ID_DESC)
                .toList();
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        final int safeLimit = Math.max(MIN_LIMIT, limit);
        return snapshotValues().stream()
                .sorted(ORDER_BY_TS_DESC_ID_DESC)
                .limit(safeLimit)
                .toList();
    }

    /** Append-only: vietato cancellare log. */
    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("SystemLog è append-only: delete non consentita");
    }
}