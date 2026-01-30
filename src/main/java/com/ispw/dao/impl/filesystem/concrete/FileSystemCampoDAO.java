package com.ispw.dao.impl.filesystem.concrete;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

public class FileSystemCampoDAO extends FileSystemDAO<Integer, Campo> implements CampoDAO {

    public FileSystemCampoDAO(Path storageDir) {
        super(storageDir, "campo.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Campo entity) {
        // TODO: return entity.getIdCampo();
        throw new UnsupportedOperationException("TODO: Campo.getIdCampo()");
    }

    @Override
    public List<Campo> findAll() {
        // ATTENZIONE: cache è protetta ma lock è privato nella base; per skeleton va bene.
        return new ArrayList<>(cache.values());
    }

    @Override
    public Campo findById(int idCampo) {
        return load(idCampo);
    }
}
