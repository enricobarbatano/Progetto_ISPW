package com.ispw.dao.interfaces;

import com.ispw.model.entity.Pagamento;
public interface PagamentoDAO extends DAO<Integer, Pagamento> {
    Pagamento findByPrenotazione(int idPrenotazione);
}