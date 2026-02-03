package com.ispw.dao.impl.memory.concrete;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

public class InMemoryPagamentoDAO extends In_MemoryDAO<Integer, Pagamento> implements PagamentoDAO {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public InMemoryPagamentoDAO() { super(true); }

    @Override
    protected Integer getId(Pagamento entity) {
        return entity.getIdPagamento(); // entity: int → autoboxing
    }

    @Override
    public void store(Pagamento entity) {
        if (entity.getIdPagamento() <= 0) {
            entity.setIdPagamento(SEQ.incrementAndGet());
        }
        super.store(entity);
    }

    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        List<Pagamento> all = snapshotValues();
        return all.stream()
                  .filter(p -> p.getIdPrenotazione() == idPrenotazione)
                  .findFirst()
                  .orElse(null);
    }

}