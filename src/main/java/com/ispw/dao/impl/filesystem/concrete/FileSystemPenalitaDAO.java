package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public class FileSystemPenalitaDAO extends FileSystemDAO<Integer, Penalita> implements PenalitaDAO {
    private static final Comparator<Penalita> ORDER_BY_DATA_DESC_ID_DESC =
            Comparator.comparing(Penalita::getDataEmissione, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Penalita::getIdPenalita)
                      .reversed();


    private static final String FILE_NAME = "penalita.ser";

    public FileSystemPenalitaDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Penalita entity) {
        return entity != null ? entity.getIdPenalita() : 0;
    }

    @Override
    public void store(Penalita entity) {
        Objects.requireNonNull(entity, "entity non può essere null");
        if (entity.getIdPenalita() == 0) {
            final int next = this.cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPenalita(next);
        }
        super.store(entity);
    }

    @Override
    public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        return this.cache.values().stream()
                .filter(p -> p != null && p.getIdUtente() == idUtente)
                .sorted(ORDER_BY_DATA_DESC_ID_DESC)
                .toList();
    }
}
