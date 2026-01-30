package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.SystemLog;

public interface LogDAO {

    /** Append-only: aggiunge un log (mai update/delete) */
    void append(SystemLog log);

    List<SystemLog> findByUtente(int idUtente);

    /** utile per debug */
    List<SystemLog> findLast(int limit);
}
