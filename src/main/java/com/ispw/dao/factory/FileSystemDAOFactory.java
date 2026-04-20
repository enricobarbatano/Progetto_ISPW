package com.ispw.dao.factory;

import java.nio.file.Path;

import com.ispw.dao.impl.filesystem.concrete.CampoDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.FatturaDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.FileSystemGestoreDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemRegolePenalitaDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemRegoleTempisticheDAO;
import com.ispw.dao.impl.filesystem.concrete.GeneralUserDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.LogDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.PagamentoDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.PenalitaDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.PrenotazioneDAOFileSystem;
import com.ispw.dao.impl.filesystem.concrete.UtenteFinaleDAOFileSystem;
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

public final class FileSystemDAOFactory extends DAOFactory {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: factory concreta per DAO su file system.
    // A2) Stato: root FS e istanze DAO memoizzate.

    private final Path root = DAOFactory.getFileSystemRootOrThrow();

    private CampoDAO campoDAO;
    private FatturaDAO fatturaDAO;

    private GeneralUserDAO generalUserDAO;
    private GestoreDAO gestoreDAO;
    private UtenteFinaleDAO utenteFinaleDAO;

    private LogDAO logDAO;
    private PagamentoDAO pagamentoDAO;
    private PenalitaDAO penalitaDAO;
    private PrenotazioneDAO prenotazioneDAO;

    private RegolePenalitaDAO regolePenalitaDAO;
    private RegoleTempisticheDAO regoleTempisticheDAO;

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) get*DAO: lazy init con root FS.

    @Override
    public synchronized CampoDAO getCampoDAO() {
        if (campoDAO == null) campoDAO = new CampoDAOFileSystem(root);
        return campoDAO;
    }

    @Override
    public synchronized FatturaDAO getFatturaDAO() {
        if (fatturaDAO == null) fatturaDAO = new FatturaDAOFileSystem(root);
        return fatturaDAO;
    }

    @Override
    public synchronized GeneralUserDAO getGeneralUserDAO() {
        if (generalUserDAO == null) generalUserDAO = new GeneralUserDAOFileSystem(root);
        return generalUserDAO;
    }

    @Override
    public synchronized GestoreDAO getGestoreDAO() {
        if (gestoreDAO == null) gestoreDAO = new FileSystemGestoreDAO(root);
        return gestoreDAO;
    }

    @Override
    public synchronized UtenteFinaleDAO getUtenteFinaleDAO() {
        if (utenteFinaleDAO == null) utenteFinaleDAO = new UtenteFinaleDAOFileSystem(root);
        return utenteFinaleDAO;
    }

    @Override
    public synchronized LogDAO getLogDAO() {
        if (logDAO == null) logDAO = new LogDAOFileSystem(root);
        return logDAO;
    }

    @Override
    public synchronized PagamentoDAO getPagamentoDAO() {
        if (pagamentoDAO == null) pagamentoDAO = new PagamentoDAOFileSystem(root);
        return pagamentoDAO;
    }

    @Override
    public synchronized PenalitaDAO getPenalitaDAO() {
        if (penalitaDAO == null) penalitaDAO = new PenalitaDAOFileSystem(root);
        return penalitaDAO;
    }

    @Override
    public synchronized PrenotazioneDAO getPrenotazioneDAO() {
        if (prenotazioneDAO == null) prenotazioneDAO = new PrenotazioneDAOFileSystem(root);
        return prenotazioneDAO;
    }

    @Override
    public synchronized RegolePenalitaDAO getRegolePenalitaDAO() {
        if (regolePenalitaDAO == null) regolePenalitaDAO = new FileSystemRegolePenalitaDAO(root);
        return regolePenalitaDAO;
    }

    @Override
    public synchronized RegoleTempisticheDAO getRegoleTempisticheDAO() {
        if (regoleTempisticheDAO == null) regoleTempisticheDAO = new FileSystemRegoleTempisticheDAO(root);
        return regoleTempisticheDAO;
    }
}
