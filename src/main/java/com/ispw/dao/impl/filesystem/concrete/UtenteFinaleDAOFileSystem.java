package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;

import com.ispw.dao.impl.base.BaseUtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

/**
 * FileSystem wrapper that reuses BaseUtenteFinaleDAO cache and delegates raw I/O
 * to the existing FileSystemUtenteFinaleDAO.
 */
public class UtenteFinaleDAOFileSystem extends BaseUtenteFinaleDAO {

    private final FileSystemUtenteFinaleDAO delegate;

    public UtenteFinaleDAOFileSystem(Path storageDir) {
        super(true);
        this.delegate = new FileSystemUtenteFinaleDAO(storageDir);
    }

    @Override
    protected UtenteFinale rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(UtenteFinale entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected UtenteFinale rawFindByEmail(String email) { return delegate.findByEmail(email); }
}
