package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;

import com.ispw.dao.impl.base.BasePenalitaDAO;
import com.ispw.model.entity.Penalita;

public class PenalitaDAOFileSystem extends BasePenalitaDAO {

    private final FileSystemPenalitaDAO delegate;

    public PenalitaDAOFileSystem(Path storageDir) {
        super(true);
        this.delegate = new FileSystemPenalitaDAO(storageDir);
    }

    @Override
    protected Penalita rawLoad(Integer id) { return delegate.load(id); }

    @Override
    protected void rawStore(Penalita entity) { delegate.store(entity); }

    @Override
    protected void rawDelete(Integer id) { delegate.delete(id); }

    @Override
    protected List<Penalita> rawFindByUtente(int idUtente) { return delegate.recuperaPenalitaUtente(idUtente); }
}
