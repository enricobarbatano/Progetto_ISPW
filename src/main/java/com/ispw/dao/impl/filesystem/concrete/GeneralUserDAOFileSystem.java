package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;

import com.ispw.dao.impl.base.BaseGeneralUserDAO;
import com.ispw.model.entity.GeneralUser;

/**
 * FileSystem GeneralUser DAO implemented as a wrapper that delegates
 * to the existing FileSystemGeneralUserDAO while reusing the
 * BaseGeneralUserDAO cache/template.
 */
public class GeneralUserDAOFileSystem extends BaseGeneralUserDAO {

    private final FileSystemGeneralUserDAO delegate;

    public GeneralUserDAOFileSystem(Path storageDir) {
        super(true); // persistent
        this.delegate = new FileSystemGeneralUserDAO(storageDir);
    }

    @Override
    protected GeneralUser rawLoad(Integer id) {
        return delegate.load(id);
    }

    @Override
    protected void rawStore(GeneralUser entity) {
        delegate.store(entity);
    }

    @Override
    protected void rawDelete(Integer id) {
        delegate.delete(id);
    }

    @Override
    protected List<GeneralUser> rawFindAll() {
        return delegate.findAll();
    }

    @Override
    protected GeneralUser rawFindByEmail(String email) {
        return delegate.findByEmail(email);
    }
}
