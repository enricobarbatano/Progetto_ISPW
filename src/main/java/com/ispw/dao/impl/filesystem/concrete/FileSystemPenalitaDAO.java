package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.model.entity.Penalita;

public class FileSystemPenalitaDAO extends FileSystemDAO<Integer, Penalita> implements PenalitaDAO {

    private static final String FILE_NAME = "penalita.ser";

    public FileSystemPenalitaDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Penalita entity) {
        return entity != null ? entity.getIdPenalita() : 0;
    }

    @Override
    public List<Penalita> recuperaPenalitaUtente(int idUtente) {
        return this.cache.values().stream()
                .filter(p -> p != null && p.getIdUtente() == idUtente)
                .collect(Collectors.toList());
    }
}
