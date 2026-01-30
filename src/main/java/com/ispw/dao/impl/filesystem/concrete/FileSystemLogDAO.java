package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;

public class FileSystemLogDAO extends FileSystemDAO<Integer, SystemLog> implements LogDAO {

    public FileSystemLogDAO(Path storageDir) {
        super(storageDir, "log.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(SystemLog entity) {
        // TODO: return entity.getIdLog();
        throw new UnsupportedOperationException("TODO: SystemLog.getIdLog()");
    }

    @Override
    public void append(SystemLog log) {
        store(log);
    }

    @Override
    public List<SystemLog> findByUtente(int idUtente) {
        throw new UnsupportedOperationException("TODO: findByUtente()");
    }

    @Override
    public List<SystemLog> findLast(int limit) {
        throw new UnsupportedOperationException("TODO: findLast()");
    }
}
