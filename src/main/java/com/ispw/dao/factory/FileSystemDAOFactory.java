package com.ispw.dao.factory;

import java.nio.file.Path;

import com.ispw.dao.impl.filesystem.concrete.FileSystemCampoDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemFatturaDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemGeneralUserDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemGestoreDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemLogDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemPagamentoDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemPenalitaDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemPrenotazioneDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemRegolePenalitaDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemRegoleTempisticheDAO;
import com.ispw.dao.impl.filesystem.concrete.FileSystemUtenteFinaleDAO;
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

    @Override
    public synchronized CampoDAO getCampoDAO() {
        if (campoDAO == null) campoDAO = new FileSystemCampoDAO(root);
        return campoDAO;
    }

    @Override
    public synchronized FatturaDAO getFatturaDAO() {
        if (fatturaDAO == null) fatturaDAO = new FileSystemFatturaDAO(root);
        return fatturaDAO;
    }

    @Override
    public synchronized GeneralUserDAO getGeneralUserDAO() {
        if (generalUserDAO == null) generalUserDAO = new FileSystemGeneralUserDAO(root);
        return generalUserDAO;
    }

    @Override
    public synchronized GestoreDAO getGestoreDAO() {
        if (gestoreDAO == null) gestoreDAO = new FileSystemGestoreDAO(root);
        return gestoreDAO;
    }

    @Override
    public synchronized UtenteFinaleDAO getUtenteFinaleDAO() {
        if (utenteFinaleDAO == null) utenteFinaleDAO = new FileSystemUtenteFinaleDAO(root);
        return utenteFinaleDAO;
    }

    @Override
    public synchronized LogDAO getLogDAO() {
        if (logDAO == null) logDAO = new FileSystemLogDAO(root);
        return logDAO;
    }

    @Override
    public synchronized PagamentoDAO getPagamentoDAO() {
        if (pagamentoDAO == null) pagamentoDAO = new FileSystemPagamentoDAO(root);
        return pagamentoDAO;
    }

    @Override
    public synchronized PenalitaDAO getPenalitaDAO() {
        if (penalitaDAO == null) penalitaDAO = new FileSystemPenalitaDAO(root);
        return penalitaDAO;
    }

    @Override
    public synchronized PrenotazioneDAO getPrenotazioneDAO() {
        if (prenotazioneDAO == null) prenotazioneDAO = new FileSystemPrenotazioneDAO(root);
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