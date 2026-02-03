package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

public class FileSystemFatturaDAO extends FileSystemDAO<Integer, Fattura> implements FatturaDAO {

    private static final String FILE_NAME = "fattura.ser";

    // Ordine: dataEmissione DESC (null last), poi idFattura DESC
    private static final Comparator<Fattura> ORDER_BY_DATA_DESC_ID_DESC =
            Comparator.<Fattura, java.time.LocalDate>comparing(Fattura::getDataEmissione,
                    Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Fattura::getIdFattura)
                      .reversed();

    public FileSystemFatturaDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Fattura entity) {
        return entity != null ? entity.getIdFattura() : 0;
    }

    @Override
    public void store(Fattura entity) {
        Objects.requireNonNull(entity, "entity non può essere null");
        if (entity.getIdFattura() == 0) {
            final int next = this.cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdFattura(next);
        }
        super.store(entity);
    }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        return this.cache.values().stream()
                .filter(f -> f != null && f.getIdUtente() == idUtente) // <--- richiede Fattura#idUtente
                .sorted(ORDER_BY_DATA_DESC_ID_DESC)
                .findFirst()
                .orElse(null);
    }
}