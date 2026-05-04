package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BaseRichiestaDisdettaDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.RichiestaDisdettaRimborso;
import com.ispw.model.enums.StatoRichiestaDisdetta;

public class RichiestaDisdettaDAOFileSystem extends BaseRichiestaDisdettaDAO {

    private final JsonListFileStore<RichiestaDisdettaRimborso> store;

    // Ordine stabile nel JSON (opzionale ma utile per diff)
    private static final Comparator<RichiestaDisdettaRimborso> ORDER_BY_ID =
            Comparator.comparingInt(RichiestaDisdettaRimborso::getIdRichiesta);

    public RichiestaDisdettaDAOFileSystem(Path storageDir) {
        // Se la tua base usa Boolean tri-state: super(Boolean.TRUE);
        super(true);

        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("richieste_disdetta.json"),
                new TypeReference<List<RichiestaDisdettaRimborso>>() {},
                ORDER_BY_ID
        );
    }

    @Override
    protected RichiestaDisdettaRimborso rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        return rawFindAll().stream()
                .filter(r -> r != null && r.getIdRichiesta() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<RichiestaDisdettaRimborso> rawFindAll() {
        return store.readAll(); // già ordinato su writeAll (stableOrder); se vuoi, puoi ordinare anche qui
    }

    @Override
    protected void rawStore(RichiestaDisdettaRimborso entity) {
        if (entity == null) return;

        List<RichiestaDisdettaRimborso> all = store.readAll();

        // assegna ID se manca (stile FS: max+1)
        if (entity.getIdRichiesta() <= 0) {
            int next = all.stream()
                    .filter(r -> r != null)
                    .mapToInt(RichiestaDisdettaRimborso::getIdRichiesta)
                    .max()
                    .orElse(0) + 1;
            entity.setIdRichiesta(next);
        }

        // default di dominio
        if (entity.getTimestampRichiesta() == null) entity.setTimestampRichiesta(LocalDateTime.now());
        if (entity.getStato() == null) entity.setStato(StatoRichiestaDisdetta.PENDING);

        // upsert (remove + add)
        all.removeIf(r -> r != null && r.getIdRichiesta() == entity.getIdRichiesta());
        all.add(entity);

        store.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) return;

        List<RichiestaDisdettaRimborso> all = store.readAll();
        all.removeIf(r -> r != null && r.getIdRichiesta() == id);
        store.writeAll(all);
    }

    @Override
    protected void rawUpdateStato(int idRichiesta, StatoRichiestaDisdetta stato, Integer idGestore, String notaGestore) {
        if (idRichiesta <= 0 || stato == null) return;

        List<RichiestaDisdettaRimborso> all = store.readAll();
        for (RichiestaDisdettaRimborso r : all) {
            if (r != null && r.getIdRichiesta() == idRichiesta) {
                r.setStato(stato);
                r.setTimestampDecisione(LocalDateTime.now());
                r.setIdGestoreDecisione(idGestore);
                r.setNotaGestore(notaGestore);
                break;
            }
        }
        store.writeAll(all);
    }
}
