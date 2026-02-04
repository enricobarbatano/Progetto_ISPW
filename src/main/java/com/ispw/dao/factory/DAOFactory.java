package com.ispw.dao.factory;

import java.nio.file.Path;

import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.enums.PersistencyProvider;

public abstract class DAOFactory {

    private static PersistencyProvider provider;
    private static DAOFactory instance;

    // config FileSystem (opzione 1)
    private static Path fileSystemRoot;

    protected static Path getFileSystemRootOrThrow() {
        if (fileSystemRoot == null) {
            throw new IllegalStateException("FileSystem root non impostata (chiama initialize(PersistencyProvider, Path) nel bootstrap).");
        }
        return fileSystemRoot;
    }

    /**
     * Initialize the DAOFactory atomically. Call this once during bootstrap.
     * If the provider is FILE_SYSTEM, the root path must be supplied.
     */
    public static synchronized void initialize(PersistencyProvider p, Path root) {
        if (provider != null) {
            throw new IllegalStateException("DAOFactory già inizializzata.");
        }
        if (p == null) {
            throw new IllegalArgumentException("PersistencyProvider non può essere null");
        }
        if (p == PersistencyProvider.FILE_SYSTEM) {
            if (root == null) {
                throw new IllegalArgumentException("FileSystem root richiesta per FILE_SYSTEM");
            }
            fileSystemRoot = root;
        }
        provider = p;
        instance = switch (provider) {
            case IN_MEMORY   -> new MemoryDAOFactory();
            case FILE_SYSTEM -> new FileSystemDAOFactory();
            case DBMS        -> new DbmsDAOFactory();
        };
    }

    /**
     * Package-private: reset the factory state. Intended for test usage only.
     */
    static synchronized void resetForTests() {
        provider = null;
        fileSystemRoot = null;
        instance = null;
    }

    // GUI-safe: synchronized
    public static synchronized DAOFactory getInstance() {
        if (provider == null) {
            throw new IllegalStateException("DAOFactory non configurata. Chiama initialize(PersistencyProvider, Path) prima.");
        }
        if (instance == null) {
            instance = switch (provider) {
                case IN_MEMORY   -> new MemoryDAOFactory();
                case FILE_SYSTEM -> new FileSystemDAOFactory();
                case DBMS        -> new DbmsDAOFactory();
            };
        }
        return instance;
    }

    // ===== metodi astratti per tutti i DAO =====
    public abstract CampoDAO getCampoDAO();
    public abstract FatturaDAO getFatturaDAO();

    public abstract GeneralUserDAO getGeneralUserDAO();
    public abstract GestoreDAO getGestoreDAO();
    public abstract UtenteFinaleDAO getUtenteFinaleDAO();

    public abstract LogDAO getLogDAO();
    public abstract PagamentoDAO getPagamentoDAO();
    public abstract PenalitaDAO getPenalitaDAO();
    public abstract PrenotazioneDAO getPrenotazioneDAO();

    public abstract RegolePenalitaDAO getRegolePenalitaDAO();
    public abstract RegoleTempisticheDAO getRegoleTempisticheDAO();


}
