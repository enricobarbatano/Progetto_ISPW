package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public class FileSystemPenalitaDAO extends FileSystemDAO<Integer, Penalita> implements PenalitaDAO {

    public FileSystemPenalitaDAO(Path storageDir) {
        super(storageDir, "penalita.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Penalita entity) {
        // TODO: return entity.getIdPenalita();
        throw new UnsupportedOperationException("TODO: Penalita.getIdPenalita()");
    }

    @Override
    public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        throw new UnsupportedOperationException("TODO: recuperaPenalitaUtente()");
    }
}
