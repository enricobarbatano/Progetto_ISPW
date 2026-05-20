package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePrenotazioneDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * FileSystem Prenotazione DAO.
 *
 * Responsabilità:
 * - implementare i raw hook definiti da BasePrenotazioneDAO;
 * - leggere e scrivere prenotazioni.json;
 * - lasciare a BasePrenotazioneDAO cache-first e composizione A2.
 *
 * NON:
 * - non compone Campo/Pagamento/Fattura;
 * - non chiama DAO figli;
 * - non contiene logica DBMS.
 */
public class PrenotazioneDAOFileSystem extends BasePrenotazioneDAO {

    private static final Comparator<Prenotazione> ORDER_BY_DATA_ORA_ID =
            Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Prenotazione::getIdPrenotazione);

    // Ordine stabile nel file JSON.
    private static final Comparator<Prenotazione> ORDER_BY_ID =
            Comparator.comparingInt(Prenotazione::getIdPrenotazione);

    private final Path filePath;
    private final JsonListFileStore<Prenotazione> jsonStore;

    public PrenotazioneDAOFileSystem(Path storageDir) {
        super(true);

        try {
            // Crea la directory di storage se non esiste.
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage: " + storageDir, e);
        }

        // File JSON dedicato alle prenotazioni.
        this.filePath = storageDir.resolve("prenotazioni.json");

        // Store JSON generico per lista di Prenotazione.
        this.jsonStore = new JsonListFileStore<>(
                filePath,
                new TypeReference<List<Prenotazione>>() {},
                ORDER_BY_ID
        );
    }

    /**
     * Legge tutte le prenotazioni dal file e le indicizza per id.
     */
    private Map<Integer, Prenotazione> readAllAsMap() {
        List<Prenotazione> list = jsonStore.readAll();
        Map<Integer, Prenotazione> map = new ConcurrentHashMap<>();

        for (Prenotazione prenotazione : list) {
            if (prenotazione != null && prenotazione.getIdPrenotazione() > 0) {
                map.put(prenotazione.getIdPrenotazione(), prenotazione);
            }
        }

        return map;
    }

    /**
     * Scrive su file tutte le prenotazioni presenti nella mappa.
     */
    private void writeAllFromMap(Map<Integer, Prenotazione> data) {
        jsonStore.writeAll(new ArrayList<>(data.values()));
    }

    @Override
    protected Prenotazione rawLoad(Integer id) {
        if (id == null) {
            return null;
        }

        return readAllAsMap().get(id);
    }

    @Override
    protected void rawStore(Prenotazione entity) {
        if (entity == null) {
            return;
        }

        Map<Integer, Prenotazione> data = readAllAsMap();

        if (entity.getIdPrenotazione() == 0) {
            int next = data.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0) + 1;

            entity.setIdPrenotazione(next);
        }

        data.put(entity.getIdPrenotazione(), entity);
        writeAllFromMap(data);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null) {
            return;
        }

        Map<Integer, Prenotazione> data = readAllAsMap();
        data.remove(id);
        writeAllFromMap(data);
    }

    @Override
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        Map<Integer, Prenotazione> data = readAllAsMap();
        List<Prenotazione> out = new ArrayList<>();

        for (Prenotazione prenotazione : data.values()) {
            if (prenotazione != null && prenotazione.getIdUtente() == idUtente) {
                out.add(prenotazione);
            }
        }

        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        Map<Integer, Prenotazione> data = readAllAsMap();
        List<Prenotazione> out = new ArrayList<>();

        for (Prenotazione prenotazione : data.values()) {
            if (prenotazione != null
                    && prenotazione.getIdUtente() == idUtente
                    && prenotazione.getStato() == stato) {
                out.add(prenotazione);
            }
        }

        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByCampo(int idCampo) {
        Map<Integer, Prenotazione> data = readAllAsMap();
        List<Prenotazione> out = new ArrayList<>();

        for (Prenotazione prenotazione : data.values()) {
            if (prenotazione != null && prenotazione.getIdCampo() == idCampo) {
                out.add(prenotazione);
            }
        }

        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        Map<Integer, Prenotazione> data = readAllAsMap();

        /*
         * Mantiene la logica precedente:
         * - scrive su file solo se qualcosa è stato trovato.
         */
        if (data.containsKey(idPrenotazione)) {
            data.computeIfPresent(idPrenotazione, (id, prenotazione) -> {
                prenotazione.setStato(nuovoStato);
                return prenotazione;
            });

            writeAllFromMap(data);
        }
    }
}