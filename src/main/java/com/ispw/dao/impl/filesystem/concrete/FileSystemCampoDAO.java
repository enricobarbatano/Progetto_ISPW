package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

/**
 * Implementazione FileSystem di CampoDAO.
 * - Salva/legge una mappa serializzata su file (campo.ser)
 * - Niente SQL, solo (de)serializzazione binaria tramite la base FileSystemDAO.
 */
public class FileSystemCampoDAO extends FileSystemDAO<Integer, Campo> implements CampoDAO {

    public FileSystemCampoDAO(Path storageDir) {
        // "campo.ser" = file unico della "tabella"
        super(storageDir, "campo.ser", new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Campo entity) {
        // *** Necessario per la chiave nella mappa (cache) ***
        return entity.getIdCampo();
    }

    @Override
    public List<Campo> findAll() {
        // Copia difensiva della cache (fornita dalla base)
        return new ArrayList<>(cache.values());
    }

    @Override
    public Campo findById(int idCampo) {
        // Semplicemente carica dalla mappa/archivio
        return load(idCampo);
    }
}
