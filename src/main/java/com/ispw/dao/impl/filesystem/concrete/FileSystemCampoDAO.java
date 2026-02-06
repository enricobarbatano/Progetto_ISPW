package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.model.entity.Campo;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO FileSystem per Campo.
 * Responsabilita': persistere una mappa serializzata su file locale.
 *
 * SEZIONE LOGICA
 * Usa FileSystemDAO per (de)serializzazione e accesso alla cache.
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
        List<Campo> all = new ArrayList<>(cache.values());
        all.sort(Comparator.comparingInt(Campo::getIdCampo));
        return all;
    }

    @Override
    public Campo findById(int idCampo) {
        // Semplicemente carica dalla mappa/archivio
        return load(idCampo);
    }
}
