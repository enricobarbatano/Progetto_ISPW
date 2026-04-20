package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;

import com.ispw.dao.impl.base.BaseLogDAO;
import com.ispw.model.entity.SystemLog;

public class LogDAOFileSystem extends BaseLogDAO {

    private final FileSystemLogDAO delegate;

    public LogDAOFileSystem(Path storageDir) {
        super(true);
        this.delegate = new FileSystemLogDAO(storageDir);
    }

    @Override
    protected SystemLog rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawAppend(SystemLog log) { delegate.append(log); }

    @Override
    protected List<SystemLog> rawFindByUtente(int idUtente) { return delegate.findByUtente(idUtente); }

    @Override
    protected List<SystemLog> rawFindLast(int limit) { return delegate.findLast(limit); }
}
