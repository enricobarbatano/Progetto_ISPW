package com.ispw.dao.interfaces;

public interface DAO<I, E> {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: contratto base per DAO.
    // A2) IO: operazioni CRUD su entita' E con chiave I.

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) load/store/delete/exists/create: operazioni CRUD base.
    E load(I id);
    void store(E entity);
    void delete(I id);
    boolean exists(I id);
    E create(I id);
}
