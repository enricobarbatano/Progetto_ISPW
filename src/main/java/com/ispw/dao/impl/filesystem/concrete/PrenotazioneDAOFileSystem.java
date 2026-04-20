package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePrenotazioneDAO;
import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * FileSystem Prenotazione DAO implemented as subclass of BasePrenotazioneDAO.
 * Implements raw I/O reading/writing a map serialized on disk.
 */
public class PrenotazioneDAOFileSystem extends BasePrenotazioneDAO {

    private final Path filePath;
    private final FileSystemDAO.JavaBinaryMapCodec<Integer, Prenotazione> codec = new FileSystemDAO.JavaBinaryMapCodec<>();

    public PrenotazioneDAOFileSystem(Path storageDir) {
        super(true); // persistent
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage: " + storageDir, e);
        }
        this.filePath = storageDir.resolve("prenotazione.ser");
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Prenotazione> readAll() {
        try {
            Optional<Map<Integer, Prenotazione>> maybe = codec.read(filePath);
            return maybe.orElseGet(ConcurrentHashMap::new);
        } catch (DaoException e) {
            throw e;
        }
    }

    @Override
    protected Prenotazione rawLoad(Integer id) {
        if (id == null) return null;
        Map<Integer, Prenotazione> data = readAll();
        return data.get(id);
    }

    @Override
    protected void rawStore(Prenotazione entity) {
        if (entity == null) return;
        Map<Integer, Prenotazione> data = readAll();

        if (entity.getIdPrenotazione() == 0) {
            int next = data.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPrenotazione(next);
        }
        data.put(entity.getIdPrenotazione(), entity);

        // atomic write: tmp -> move
        try {
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            codec.write(tmp, data);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DaoException("Errore scrittura su file: " + filePath, e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null) return;
        Map<Integer, Prenotazione> data = readAll();
        data.remove(id);
        try {
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            codec.write(tmp, data);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DaoException("Errore scrittura su file: " + filePath, e);
        }
    }

    @Override
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        Map<Integer, Prenotazione> data = readAll();
        List<Prenotazione> out = new ArrayList<>();
        for (Prenotazione p : data.values()) {
            if (p != null && p.getIdUtente() == idUtente) out.add(p);
        }
        out.sort(Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                         .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                         .thenComparingInt(Prenotazione::getIdPrenotazione));
        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        Map<Integer, Prenotazione> data = readAll();
        List<Prenotazione> out = new ArrayList<>();
        for (Prenotazione p : data.values()) {
            if (p != null && p.getIdUtente() == idUtente && p.getStato() == stato) out.add(p);
        }
        out.sort(Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                         .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                         .thenComparingInt(Prenotazione::getIdPrenotazione));
        return out;
    }

    @Override
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        Map<Integer, Prenotazione> data = readAll();
        Prenotazione p = data.get(idPrenotazione);
        if (p != null) {
            p.setStato(nuovoStato);
            try {
                Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
                codec.write(tmp, data);
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new DaoException("Errore scrittura su file: " + filePath, e);
            }
        }
    }
}
