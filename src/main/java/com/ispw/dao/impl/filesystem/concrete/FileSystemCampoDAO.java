package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
        seedIfEmpty();
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

    private void seedIfEmpty() {
        if (!cache.isEmpty()) {
            return;
        }
        store(buildCampo(1, "Campo A", "Calcio", 25f, true, false));
        store(buildCampo(2, "Campo B", "Tennis", 18f, true, false));
        store(buildCampo(3, "Campo C", "Padel", 20f, true, false));
    }

    private Campo buildCampo(int id, String nome, String sport, float costoOrario,
                             boolean attivo, boolean manutenzione) {
        Objects.requireNonNull(nome, "nome non può essere null");
        Objects.requireNonNull(sport, "sport non può essere null");
        Campo c = new Campo();
        c.setIdCampo(id);
        c.setNome(nome);
        c.setTipoSport(sport);
        c.setCostoOrario(costoOrario);
        c.setAttivo(attivo);
        c.setFlagManutenzione(manutenzione);
        return c;
    }
}
