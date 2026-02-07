package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.SystemLog;

public interface LogDAO {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per log di sistema.
    // A2) IO: operazioni append e query.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) append: aggiunta log.
    // L2) findByUtente/findLast: query log.
    void append(SystemLog log);

    List<SystemLog> findByUtente(int idUtente);
    List<SystemLog> findLast(int limit);
}
