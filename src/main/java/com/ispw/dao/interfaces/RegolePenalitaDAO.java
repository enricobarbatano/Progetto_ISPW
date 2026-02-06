package com.ispw.dao.interfaces;

import com.ispw.model.entity.RegolePenalita;

public interface RegolePenalitaDAO {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per regole penalita.
    // A2) IO: lettura/salvataggio configurazione.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) get/save: accesso configurazione penalita.
    // ========================
    RegolePenalita get();
    void save(RegolePenalita regole);
}