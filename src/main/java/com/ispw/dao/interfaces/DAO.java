package com.ispw.dao.interfaces;
/**
 * DAO standard generica.
 * I = tipo della chiave primaria (es. Integer, String, UUID)
 * E = tipo dell'entità (es. GeneralUser, Prenotazione)
 */
public interface DAO<I, E> {
    E load(I id);
    void store(E entity);
    void delete(I id);
    boolean exists(I id);
    E create(I id);
}
