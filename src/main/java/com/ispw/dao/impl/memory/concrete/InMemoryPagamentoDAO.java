package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.InMemoryDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

public final class InMemoryPagamentoDAO extends InMemoryDAO<Integer, Pagamento> implements PagamentoDAO {
    public InMemoryPagamentoDAO() { super(true); }
    @Override protected Integer getId(Pagamento e) { return e != null ? e.getIdPagamento() : 0; }
    @Override public Pagamento findByPrenotazione(int idPrenotazione) {
        return snapshotValues().stream()
            .filter(p -> p != null && p.getIdPrenotazione() == idPrenotazione)
            .findFirst().orElse(null);
    }
}