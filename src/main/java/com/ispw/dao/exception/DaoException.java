package com.ispw.dao.exception;

/**
 * Unchecked exception wrapper per errori DAO/Storage.
 * Usata per propagare errori I/O e di persistenza senza esporre eccezioni checked
 * nelle interfacce DAO. Mantiene la causa originale.
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
