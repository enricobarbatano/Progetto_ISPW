package com.ispw.dao.impl.filesystem.concrete;


import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

/**
 * DAO FileSystem per SystemLog (append-only).
 * SonarCloud-friendly: comparator riutilizzabile, nessun System.out, controlli input.
 */
public final class FileSystemLogDAO extends FileSystemDAO<Integer, SystemLog> implements LogDAO {

    private static final String FILE_NAME = "system_log.ser";
    private static final int MIN_LIMIT = 1;

    /**
     * Ordina per timestamp DESC (null last) e, a parità, per id DESC.
     * Evita generics espliciti non necessari che causano errori d'inferenza.
     */
    private static final Comparator<SystemLog> ORDER_BY_TS_DESC_ID_DESC =
            Comparator.comparing(SystemLog::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(SystemLog::getIdLog)
                      .reversed();

    public FileSystemLogDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(SystemLog entity) {
        return entity.getIdLog();
    }

    @Override
    public void append(SystemLog log) {
        Objects.requireNonNull(log, "log non può essere null");
        // Timestamp di default (evita null downstream)
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        // ID autoincrement se non impostato
        if (log.getIdLog() == 0) {
            final int next = this.cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            log.setIdLog(next);
        }
        super.store(log); // persist su file (append-only)
    }

    /** Append-only: store delega ad append (no update). */
    @Override
    public void store(SystemLog entity) {
        append(entity);
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        return this.cache.values().stream()
                .filter(l -> l.getIdUtenteCoinvolto() == idUtente)
                .sorted(ORDER_BY_TS_DESC_ID_DESC)
                .toList();
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        final int safeLimit = Math.max(MIN_LIMIT, limit);
        return this.cache.values().stream()
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