package com.ispw.dao.factory;

import com.ispw.dao.impl.base.BaseCampoDAO;
import com.ispw.dao.impl.base.BaseFatturaDAO;
import com.ispw.dao.impl.base.BaseGeneralUserDAO;
import com.ispw.dao.impl.base.BaseLogDAO;
import com.ispw.dao.impl.base.BasePagamentoDAO;
import com.ispw.dao.impl.base.BasePenalitaDAO;
import com.ispw.dao.impl.base.BasePrenotazioneDAO;
import com.ispw.dao.impl.base.BaseRegolePenalitaDAO;
import com.ispw.dao.impl.base.BaseRegoleTempisticheDAO;
import com.ispw.dao.impl.base.BaseUtenteFinaleDAO;
import com.ispw.dao.impl.memory.concrete.InMemoryGestoreDAO;
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

public final class MemoryDAOFactory extends DAOFactory {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: factory concreta per DAO in memoria.
    // A2) Stato: istanze DAO memoizzate.

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
    // L1) get*DAO: lazy init delle implementazioni in-memory.

    @Override
    public synchronized CampoDAO getCampoDAO() {
        if (campoDAO == null) campoDAO = new BaseCampoDAO();
        return campoDAO;
    }

    @Override
    public synchronized FatturaDAO getFatturaDAO() {
        if (fatturaDAO == null) fatturaDAO = new BaseFatturaDAO();
        return fatturaDAO;
    }

    @Override
    public synchronized GeneralUserDAO getGeneralUserDAO() {
        if (generalUserDAO == null) generalUserDAO = new BaseGeneralUserDAO();
        return generalUserDAO;
    }

    @Override
    public synchronized GestoreDAO getGestoreDAO() {
        if (gestoreDAO == null) gestoreDAO = new InMemoryGestoreDAO();
        return gestoreDAO;
    }

    @Override
    public synchronized UtenteFinaleDAO getUtenteFinaleDAO() {
        if (utenteFinaleDAO == null) utenteFinaleDAO = new BaseUtenteFinaleDAO();
        return utenteFinaleDAO;
    }

    @Override
    public synchronized LogDAO getLogDAO() {
        if (logDAO == null) logDAO = new BaseLogDAO();
        return logDAO;
    }

    @Override
    public synchronized PagamentoDAO getPagamentoDAO() {
        if (pagamentoDAO == null) pagamentoDAO = new BasePagamentoDAO();
        return pagamentoDAO;
    }

    @Override
    public synchronized PenalitaDAO getPenalitaDAO() {
        if (penalitaDAO == null) penalitaDAO = new BasePenalitaDAO();
        return penalitaDAO;
    }

    @Override
    public synchronized PrenotazioneDAO getPrenotazioneDAO() {
        if (prenotazioneDAO == null) prenotazioneDAO = new BasePrenotazioneDAO();
        return prenotazioneDAO;
    }

    @Override
    public synchronized RegolePenalitaDAO getRegolePenalitaDAO() {
        if (regolePenalitaDAO == null) regolePenalitaDAO = new BaseRegolePenalitaDAO();
        return regolePenalitaDAO;
    }

    @Override
    public synchronized RegoleTempisticheDAO getRegoleTempisticheDAO() {
        if (regoleTempisticheDAO == null) regoleTempisticheDAO = new BaseRegoleTempisticheDAO();
        return regoleTempisticheDAO;
    }
}
