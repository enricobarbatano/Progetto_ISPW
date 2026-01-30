package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

public class FileSystemFatturaDAO extends FileSystemDAO<Integer, Fattura> implements FatturaDAO {

    public FileSystemFatturaDAO(Path storageDir) {
        super(storageDir, "fattura.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Fattura entity) {
        // TODO: return entity.getIdFattura();
        throw new UnsupportedOperationException("TODO: Fattura.getIdFattura()");
    }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        throw new UnsupportedOperationException("TODO: findLastByUtente()");
    }
}
