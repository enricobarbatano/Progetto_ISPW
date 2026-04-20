package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;

import com.ispw.dao.impl.base.BaseFatturaDAO;
import com.ispw.model.entity.Fattura;

/**
 * FileSystem wrapper that reuses BaseFatturaDAO cache and delegates raw I/O
 * to the existing FileSystemFatturaDAO.
 */
public class FatturaDAOFileSystem extends BaseFatturaDAO {

    private final FileSystemFatturaDAO delegate;

    public FatturaDAOFileSystem(Path storageDir) {
        super(true);
        this.delegate = new FileSystemFatturaDAO(storageDir);
    }

    @Override
    protected Fattura rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(Fattura entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected Fattura rawFindLastByUtente(int idUtente) { return delegate.findLastByUtente(idUtente); }
}
