package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

public class InMemoryPagamentoDAO extends In_MemoryDAO<Integer, Pagamento> implements PagamentoDAO {

    public InMemoryPagamentoDAO() {
        super(true);
    }

    @Override
    protected Integer getId(Pagamento entity) {
        // TODO: return entity.getIdPagamento();
        throw new UnsupportedOperationException("TODO: Pagamento.getIdPagamento()");
    }

    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        throw new UnsupportedOperationException("TODO: findByPrenotazione");
    }
}
