package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.Set;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

public class FileSystemGestoreDAO extends FileSystemDAO<Integer, Gestore> implements GestoreDAO {

    public FileSystemGestoreDAO(Path storageDir) {
        super(storageDir, "gestore.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Gestore entity) {
        // TODO: return entity.getIdGestore() o getIdUtente()
        throw new UnsupportedOperationException("TODO: Gestore.getId...");
    }

    @Override
    public Gestore findById(int idGestore) {
        return load(idGestore);
    }

    @Override
    public Gestore findByEmail(String email) {
        throw new UnsupportedOperationException("TODO: findByEmail()");
    }

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        throw new UnsupportedOperationException("TODO: getPermessi()");
    }

    @Override
    public boolean hasPermesso(int idGestore, Permesso permesso) {
        throw new UnsupportedOperationException("TODO: hasPermesso()");
    }

    @Override
    public void assegnaPermesso(int idGestore, Permesso permesso) {
        throw new UnsupportedOperationException("TODO: assegnaPermesso()");
    }

    @Override
    public void rimuoviPermesso(int idGestore, Permesso permesso) {
        throw new UnsupportedOperationException("TODO: rimuoviPermesso()");
    }
}
