package com.ispw.dao.exception;


/**
 * Eccezione unchecked (Runtime) per la persistenza/DAO.
 *
 * Scopo: incapsulare errori di I/O e DB (es. IOException/SQLException) senza
 * propagare eccezioni checked nelle interfacce DAO, mantenendo la causa originale.
 * Permette di uniformare la gestione degli errori di persistenza e distinguere
 * gli errori "di storage" dagli errori di logica applicativa.
 */

public class DaoException extends RuntimeException {
    public DaoException(String message) {
        super(message);
    }
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
    public DaoException(Throwable cause) {
        super(cause);
    }
}
