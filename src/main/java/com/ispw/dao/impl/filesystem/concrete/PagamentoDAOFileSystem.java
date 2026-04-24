package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePagamentoDAO;
import com.ispw.dao.impl.filesystem.json.JsonListFileStore;
import com.ispw.model.entity.Pagamento;

public class PagamentoDAOFileSystem extends BasePagamentoDAO {

    private final JsonListFileStore<Pagamento> store;

    public PagamentoDAOFileSystem(Path storageDir) {
        super(true);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage", e);
        }

        this.store = new JsonListFileStore<>(
                storageDir.resolve("pagamenti.json"),
                new TypeReference<List<Pagamento>>() {}
        );
    }

    private Map<Integer, Pagamento> readAllAsMap() {
        Map<Integer, Pagamento> map = new ConcurrentHashMap<>();
        for (Pagamento p : store.readAll()) {
            if (p != null && p.getIdPagamento() > 0) {
                map.put(p.getIdPagamento(), p);
            }
        }
        return map;
    }

    @Override
    protected Pagamento rawLoad(Integer id) {
        if (id == null || id <= 0) return null;
        return readAllAsMap().get(id);
    }

    @Override
    protected void rawStore(Pagamento entity) {
        if (entity == null) return;

        List<Pagamento> all = new ArrayList<>(store.readAll());

        if (entity.getIdPagamento() == 0) {
            int next = all.stream().mapToInt(Pagamento::getIdPagamento).max().orElse(0) + 1;
            entity.setIdPagamento(next);
        }

        all.removeIf(p -> p.getIdPagamento() == entity.getIdPagamento());
        all.add(entity);
        store.writeAll(all);
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null || id <= 0) return;

        List<Pagamento> all = store.readAll();
        all.removeIf(p -> p.getIdPagamento() == id);
        store.writeAll(all);
    }

    @Override
    protected Pagamento rawFindByPrenotazione(int idPrenotazione) {
        return store.readAll().stream()
                .filter(p -> p != null && p.getIdPrenotazione() == idPrenotazione)
                .sorted((a, b) -> {
                    if (a.getDataPagamento() == null && b.getDataPagamento() == null) {
                        return Integer.compare(b.getIdPagamento(), a.getIdPagamento());
                    }
                    if (a.getDataPagamento() == null) return 1;
                    if (b.getDataPagamento() == null) return -1;
                    int cmp = b.getDataPagamento().compareTo(a.getDataPagamento());
                    if (cmp != 0) return cmp;
                    return Integer.compare(b.getIdPagamento(), a.getIdPagamento());
                })
                .findFirst()
                .orElse(null);
    }
}