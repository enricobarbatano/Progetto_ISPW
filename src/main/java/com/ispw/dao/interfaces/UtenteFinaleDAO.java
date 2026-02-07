package com.ispw.dao.interfaces;



import com.ispw.model.entity.UtenteFinale;

public interface UtenteFinaleDAO extends DAO<Integer, UtenteFinale> {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita UtenteFinale.
    // A2) IO: lookup per id/email.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findById/findByEmail: query utente finale.
    UtenteFinale findById(int idUtente);
    UtenteFinale findByEmail(String email);
}
