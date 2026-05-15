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
 * FileSystem Prenotazione DAO (JSON) implemented as subclass of BasePrenotazioneDAO.
 * Implements raw I/O reading/writing a list of Prenotazione on disk (prenotazioni.json).
 *
 * NOTE:
 * Prenotazione entity should @JsonIgnore composed fields (campo/pagamento/fattura)
 * so that we persist RAW data only (FK + slot + stato).
 */
public class PrenotazioneDAOFileSystem extends BasePrenotazioneDAO {

    private static final Comparator<Prenotazione> ORDER_BY_DATA_ORA_ID =
            Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Prenotazione::getIdPrenotazione);

    // Ordine stabile nel file JSON
    private static final Comparator<Prenotazione> ORDER_BY_ID =
            Comparator.comparingInt(Prenotazione::getIdPrenotazione);

    private final Path filePath;
    private final JsonListFileStore<Prenotazione> jsonStore;

    //stroage directory è il path della cartella FS dove ho i file json delle diverse istanze e anche questo viene passato dalla dilesystemdaofactory a run-time
    public PrenotazioneDAOFileSystem(Path storageDir) {
        super(true);
        try {
            //il costruttore verifica che la directory di storga esista davvero sennò la crea
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage: " + storageDir, e);
        }

        this.filePath = storageDir.resolve("prenotazioni.json");
        // gestisco il file prenotazioni.son come una lista di prenotazione: rappresentazione fs<--> O.O
        this.jsonStore = new JsonListFileStore<>(
                filePath,
                // type reference è importantissimo perchè essendo la classe jsonlistfilestore generica e utilizzata da tutti i fs dao, non sa a priori
                // che tipo convertire, perciò definiamo una new type reference e passiamo quella che è la rappresentazione run-time del file.json
                new TypeReference<List<Prenotazione>>() {},
                ORDER_BY_ID
        );
    }

    // trsaformiamo la lista in una mappa per motivi di semplicità run time, 
    // siccome la useremo per fare ad esempio ricerche id prenotazione-> istanza prenotazione, come le quuery per sql
    private Map<Integer, Prenotazione> readAllAsMap() {
        List<Prenotazione> list = jsonStore.readAll();
        Map<Integer, Prenotazione> map = new ConcurrentHashMap<>();

        for (Prenotazione p : list) {
            if (p != null && p.getIdPrenotazione() > 0) {
                map.put(p.getIdPrenotazione(), p);
            }
        }

        return map;
    }

    private void writeAllFromMap(Map<Integer, Prenotazione> data) {
        jsonStore.writeAll(new ArrayList<>(data.values()));
    }

    @Override
    protected Prenotazione rawLoad(Integer id) {
        if (id == null) return null;
        return readAllAsMap().get(id);
    }

    @Override
    protected void rawStore(Prenotazione entity) {
        if (entity == null) return;

        Map<Integer, Prenotazione> data = readAllAsMap();

        if (entity.getIdPrenotazione() == 0) {
            int next = data.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPrenotazione(next);
        }

        data.put(entity.getIdPrenotazione(), entity);
        writeAllFromMap(data);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null) return;

        Map<Integer, Prenotazione> data = readAllAsMap();
        data.remove(id);
        writeAllFromMap(data);
    }

    @Override
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        Map<Integer, Prenotazione> data = readAllAsMap();
        List<Prenotazione> out = new ArrayList<>();

        for (Prenotazione p : data.values()) {
            if (p != null && p.getIdUtente() == idUtente) {
                out.add(p);
            }
        }

        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        Map<Integer, Prenotazione> data = readAllAsMap();
        List<Prenotazione> out = new ArrayList<>();

        for (Prenotazione p : data.values()) {
            if (p != null && p.getIdUtente() == idUtente && p.getStato() == stato) {
                out.add(p);
            }
        }

        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    protected List<Prenotazione> rawFindByCampo(int idCampo) {
        Map<Integer, Prenotazione> data = readAllAsMap();
        List<Prenotazione> out = new ArrayList<>();

        for (Prenotazione p : data.values()) {
            if (p != null && p.getIdCampo() == idCampo) {
                out.add(p);
            }
        }

        out.sort(ORDER_BY_DATA_ORA_ID);
        return out;
    }

    @Override
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        Map<Integer, Prenotazione> data = readAllAsMap();

        Prenotazione p = data.get(idPrenotazione);
        if (p != null) {
            p.setStato(nuovoStato);
            data.put(idPrenotazione, p);
            writeAllFromMap(data);
        }
    }
}