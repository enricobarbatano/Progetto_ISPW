
package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

public class CampoDAOFileSystem extends BaseCampoDAO {

    private final JsonListFileStore<Campo> campoStore;
    private final JsonListFileStore<Prenotazione> prenotazioneStore;

    public CampoDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.campoStore = new JsonListFileStore<>(
                storageDir.resolve("campi.json"),
                new TypeReference<List<Campo>>() {}
        );
        this.prenotazioneStore = new JsonListFileStore<>(
                storageDir.resolve("prenotazioni.json"),
                new TypeReference<List<Prenotazione>>() {}
        );
    }

    @Override
    protected Campo rawLoad(Integer id) {
        return rawFindAll().stream()
                .filter(c -> c.getIdCampo() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<Campo> rawFindAll() {
        List<Campo> campi = campoStore.readAll();
        List<Prenotazione> prenotazioni = prenotazioneStore.readAll();

        for (Campo c : campi) {
            prenotazioni.stream()
                    .filter(p -> p.getIdCampo() == c.getIdCampo()
                              && p.getStato() != StatoPrenotazione.ANNULLATA)
                    .forEach(c::aggiungiPrenotazione);
        }
        return campi;
    }

    @Override
    protected void rawStore(Campo entity) {
        List<Campo> all = campoStore.readAll();
        all.removeIf(c -> c.getIdCampo() == entity.getIdCampo());
        all.add(entity);
        campoStore.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        List<Campo> all = campoStore.readAll();
        all.removeIf(c -> c.getIdCampo() == id);
        campoStore.writeAll(all);
    }
}
