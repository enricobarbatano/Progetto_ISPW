package com.ispw.dao.interfaces;

import com.ispw.model.entity.Fattura;

public interface FatturaDAO extends DAO<Integer, Fattura> {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita Fattura.
    // A2) IO: query ultima fattura utente.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findLastByUtente: recupero ultima fattura.
    // ========================
    Fattura findLastByUtente(int idUtente);
}