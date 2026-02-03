package com.ispw.dao.impl.filesystem.concrete;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

import java.nio.file.Path;

/**
 * DAO FileSystem per Fattura.
 * - Usa JavaBinaryMapCodec (serializzazione binaria).
 * - Auto-assegna id se mancante (id=0) con "max+1".
 * - findLastByUtente: non supportato (manca idUtente nell'entity).
 */
public class FileSystemFatturaDAO extends FileSystemDAO<Integer, Fattura> implements FatturaDAO {

    public FileSystemFatturaDAO(Path storageDir) {
        super(storageDir, "fattura.ser", new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Fattura entity) {
        return entity.getIdFattura();
    }

    @Override
    public void store(Fattura entity) {
        // Auto-assegna id se 0 (accademico): max+1 sulla cache corrente
        if (entity.getIdFattura() == 0) {
            int next = this.cache.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max().orElse(0) + 1;
            entity.setIdFattura(next);
        }
        super.store(entity);
    }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        // Non supportabile senza idUtente nell'entity o senza join su Prenotazione.
        throw new UnsupportedOperationException(
                "findLastByUtente non supportato in FileSystemFatturaDAO: Fattura non contiene idUtente.");
    }
}
