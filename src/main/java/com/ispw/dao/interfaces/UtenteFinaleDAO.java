package com.ispw.dao.interfaces;



import com.ispw.model.entity.UtenteFinale;

public interface UtenteFinaleDAO extends DAO<Integer, UtenteFinale> {

    UtenteFinale findById(int idUtente);
    UtenteFinale findByEmail(String email);
}
