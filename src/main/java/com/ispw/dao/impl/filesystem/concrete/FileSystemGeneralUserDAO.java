package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;

public class FileSystemGeneralUserDAO extends FileSystemDAO<Integer, GeneralUser> implements GeneralUserDAO {

    public FileSystemGeneralUserDAO(Path storageDir) {
        super(storageDir, "general_user.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(GeneralUser entity) {
        // TODO: return entity.getIdUtente();
        throw new UnsupportedOperationException("TODO: GeneralUser.getIdUtente()");
    }

    @Override
    public GeneralUser findByEmail(String email) {
        throw new UnsupportedOperationException("TODO: findByEmail()");
    }

    @Override
    public GeneralUser findById(int idUtente) {
        return load(idUtente);
    }
}
