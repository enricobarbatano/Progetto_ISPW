package com.ispw.dao.factory;

import com.ispw.dao.interfaces.*;

import com.ispw.dao.impl.memory.concrete.*;

public final class MemoryDAOFactory extends DAOFactory {

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
        if (campoDAO == null) campoDAO = new InMemoryCampoDAO();
        return campoDAO;
    }

    @Override
    public synchronized FatturaDAO getFatturaDAO() {
        if (fatturaDAO == null) fatturaDAO = new InMemoryFatturaDAO();
        return fatturaDAO;
    }

    @Override
    public synchronized GeneralUserDAO getGeneralUserDAO() {
        if (generalUserDAO == null) generalUserDAO = new InMemoryGeneralUserDAO();
        return generalUserDAO;
    }

    @Override
    public synchronized GestoreDAO getGestoreDAO() {
        if (gestoreDAO == null) gestoreDAO = new InMemoryGestoreDAO();
        return gestoreDAO;
    }

    @Override
    public synchronized UtenteFinaleDAO getUtenteFinaleDAO() {
        if (utenteFinaleDAO == null) utenteFinaleDAO = new InMemoryUtenteFinaleDAO();
        return utenteFinaleDAO;
    }

    @Override
    public synchronized LogDAO getLogDAO() {
        if (logDAO == null) logDAO = new InMemoryLogDAO();
        return logDAO;
    }

    @Override
    public synchronized PagamentoDAO getPagamentoDAO() {
        if (pagamentoDAO == null) pagamentoDAO = new InMemoryPagamentoDAO();
        return pagamentoDAO;
    }

    @Override
    public synchronized PenalitaDAO getPenalitaDAO() {
        if (penalitaDAO == null) penalitaDAO = new InMemoryPenalitaDAO();
        return penalitaDAO;
    }

    @Override
    public synchronized PrenotazioneDAO getPrenotazioneDAO() {
        if (prenotazioneDAO == null) prenotazioneDAO = new InMemoryPrenotazioneDAO();
        return prenotazioneDAO;
    }

    @Override
    public synchronized RegolePenalitaDAO getRegolePenalitaDAO() {
        if (regolePenalitaDAO == null) regolePenalitaDAO = new InMemoryRegolePenalitaDAO();
        return regolePenalitaDAO;
    }

    @Override
    public synchronized RegoleTempisticheDAO getRegoleTempisticheDAO() {
        if (regoleTempisticheDAO == null) regoleTempisticheDAO = new InMemoryRegoleTempisticheDAO();
        return regoleTempisticheDAO;
    }
}
