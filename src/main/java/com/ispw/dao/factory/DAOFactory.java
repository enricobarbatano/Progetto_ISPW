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

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: factory astratta per DAO, seleziona provider.
    // A2) Stato: provider, instance e root FS.

    private static PersistencyProvider provider;
    private static DAOFactory instance;

    private static Path fileSystemRoot;

    protected static Path getFileSystemRootOrThrow() {
        if (fileSystemRoot == null) {
            throw new IllegalStateException("FileSystem root non impostata (chiama initialize(PersistencyProvider, Path) nel bootstrap).");
        }
        return fileSystemRoot;
    }

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) initialize/getInstance: selezione provider e singleton.
    // L2) resetForTests: reset stato per test.
    // L3) get*DAO: factory methods.
    public static synchronized void initialize(PersistencyProvider p, Path root) {
        if (provider != null) {
            throw new IllegalStateException("DAOFactory gia  inizializzata.");
        }
        if (p == null) {
            throw new IllegalArgumentException("PersistencyProvider non puo essere null");
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

    @SuppressWarnings("unused") // invoked via reflection in tests
    static synchronized void resetForTests() {
        provider = null;
        fileSystemRoot = null;
        instance = null;
    }

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
