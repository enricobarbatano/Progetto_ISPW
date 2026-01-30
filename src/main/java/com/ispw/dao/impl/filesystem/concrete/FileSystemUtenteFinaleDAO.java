package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

public class FileSystemUtenteFinaleDAO extends FileSystemDAO<Integer, UtenteFinale> implements UtenteFinaleDAO {

    public FileSystemUtenteFinaleDAO(Path storageDir) {
        super(storageDir, "utente_finale.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(UtenteFinale entity) {
        // TODO: return entity.getIdUtente();
        throw new UnsupportedOperationException("TODO: UtenteFinale.getIdUtente()");
    }

    @Override
    public UtenteFinale findById(int idUtente) {
        return load(idUtente);
    }

    @Override
    public UtenteFinale findByEmail(String email) {
        throw new UnsupportedOperationException("TODO: findByEmail()");
    }
}
