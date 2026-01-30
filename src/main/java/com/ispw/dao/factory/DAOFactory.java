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

    public static void setPersistencyProvider(PersistencyProvider p) {
        if (provider != null) {
            throw new IllegalStateException("PersistencyProvider già impostato. Non puoi cambiarlo a runtime.");
        }
        provider = p;
    }

    public static void setFileSystemRoot(Path root) {
        if (fileSystemRoot != null) {
            throw new IllegalStateException("FileSystem root già impostata.");
        }
        fileSystemRoot = root;
    }

    protected static Path getFileSystemRootOrThrow() {
        if (fileSystemRoot == null) {
            throw new IllegalStateException("FileSystem root non impostata (chiama setFileSystemRoot nel bootstrap).");
        }
        return fileSystemRoot;
    }

    // GUI-safe: synchronized
    public static synchronized DAOFactory getInstance() {
        if (provider == null) {
            throw new IllegalStateException("DAOFactory non configurata. Chiama setPersistencyProvider() prima.");
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
