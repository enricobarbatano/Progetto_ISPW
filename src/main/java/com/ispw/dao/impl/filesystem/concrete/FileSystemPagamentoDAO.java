package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.Comparator;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO FileSystem per Pagamento.
 * Responsabilita': persistere una mappa serializzata su file locale.
 *
 * SEZIONE LOGICA
 * Usa FileSystemDAO per (de)serializzazione e accesso alla cache.
 */
public class FileSystemPagamentoDAO extends FileSystemDAO<Integer, Pagamento> implements PagamentoDAO {

    private static final Comparator<Pagamento> ORDER_BY_DATA_DESC_ID_DESC =
            Comparator.comparing(Pagamento::getDataPagamento, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparingInt(Pagamento::getIdPagamento)
                      .reversed();

    public FileSystemPagamentoDAO(Path storageDir) {
        super(storageDir, "pagamento.ser", new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Pagamento entity) {
        return entity.getIdPagamento();
    }

    @Override
    public void store(Pagamento entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity non puÃ² essere null");
        }
        if (entity.getIdPagamento() == 0) {
            final int next = this.cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPagamento(next);
        }
        super.store(entity);
    }

    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        return this.cache.values().stream()
                .filter(p -> p != null && p.getIdPrenotazione() == idPrenotazione)
                .sorted(ORDER_BY_DATA_DESC_ID_DESC)
                .findFirst()
                .orElse(null);
    }
}
