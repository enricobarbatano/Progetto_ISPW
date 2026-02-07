package com.ispw.dao.interfaces;

import com.ispw.model.entity.Pagamento;

public interface PagamentoDAO extends DAO<Integer, Pagamento> {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita Pagamento.
    // A2) IO: lookup per prenotazione.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findByPrenotazione: query pagamento per prenotazione.
    Pagamento findByPrenotazione(int idPrenotazione);
}
