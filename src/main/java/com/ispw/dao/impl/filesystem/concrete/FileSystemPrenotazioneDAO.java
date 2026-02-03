package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * DAO FileSystem per Prenotazione.
 * - Serializza una mappa <Integer, Prenotazione> su disco (prenotazione.ser).
 * - Operazioni di ricerca basate sulla cache in memoria fornita dalla base.
 */
public class FileSystemPrenotazioneDAO extends FileSystemDAO<Integer, Prenotazione> implements PrenotazioneDAO {

    public FileSystemPrenotazioneDAO(Path storageDir) {
        super(storageDir, "prenotazione.ser", new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Prenotazione entity) {
        return entity.getIdPrenotazione();
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        // Filtra la cache (copiandola per sicurezza)
        List<Prenotazione> all = new ArrayList<>(cache.values());
        all.removeIf(p -> p.getIdUtente() != idUtente);
        return all;
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        List<Prenotazione> all = new ArrayList<>(cache.values());
        all.removeIf(p -> p.getIdUtente() != idUtente || p.getStato() != stato);
        return all;
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        Prenotazione p = load(idPrenotazione);
        if (p != null) {
            p.setStato(nuovoStato);
            store(p); // persiste modifiche su file
        }
    }
}