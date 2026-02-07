package com.ispw.dao.impl.memory.concrete;

import java.util.Comparator;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO InMemory per Pagamento.
 * Responsabilita': gestire persistenza volatile per test/uso locale.
 *
 * SEZIONE LOGICA
 * Delega a InMemoryDAO e applica ricerche per prenotazione/penalita'.
 */
public final class InMemoryPagamentoDAO extends InMemoryDAO<Integer, Pagamento> implements PagamentoDAO {
    public InMemoryPagamentoDAO() { super(true); }
    @Override protected Integer getId(Pagamento e) { return e != null ? e.getIdPagamento() : 0; }
    @Override
    public void store(Pagamento entity) {
        if (entity.getIdPagamento() == 0) {
            int next = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPagamento(next);
        }
        super.store(entity);
    }
    @Override public Pagamento findByPrenotazione(int idPrenotazione) {
        return snapshotValues().stream()
            .filter(p -> p != null && p.getIdPrenotazione() == idPrenotazione)
            .max(Comparator.comparing(Pagamento::getDataPagamento,
                     Comparator.nullsLast(Comparator.naturalOrder()))
                 .thenComparingInt(Pagamento::getIdPagamento))
            .orElse(null);
    }
}
