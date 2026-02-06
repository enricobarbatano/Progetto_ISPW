package com.ispw.dao.interfaces;



import java.util.List;

import com.ispw.model.entity.GeneralUser;

public interface GeneralUserDAO extends DAO<Integer, GeneralUser> {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita GeneralUser.
    // A2) IO: ricerche utente base.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findAll/findByEmail/findById: query utente.
    // ========================
    List<GeneralUser> findAll();
    GeneralUser findByEmail(String email);
    GeneralUser findById(int idUtente);
}
