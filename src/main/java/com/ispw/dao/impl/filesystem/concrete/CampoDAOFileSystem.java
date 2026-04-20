package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;

import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.model.entity.Campo;

/**
 * FileSystem wrapper that reuses BaseCampoDAO cache and delegates raw I/O
 * to the existing FileSystemCampoDAO.
 */
public class CampoDAOFileSystem extends BaseCampoDAO {

    private final FileSystemCampoDAO delegate;

    public CampoDAOFileSystem(Path storageDir) {
        super(true);
        this.delegate = new FileSystemCampoDAO(storageDir);
    }

    @Override
    protected Campo rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(Campo entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected List<Campo> rawFindAll() { return delegate.findAll(); }
}
