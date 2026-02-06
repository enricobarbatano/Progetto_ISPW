package com.ispw.dao.interfaces;

import com.ispw.model.entity.RegoleTempistiche;

public interface RegoleTempisticheDAO {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per regole tempistiche.
    // A2) IO: lettura/salvataggio configurazione.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) get/save: accesso configurazione tempistiche.
    // ========================
    RegoleTempistiche get();
    void save(RegoleTempistiche regole);
}