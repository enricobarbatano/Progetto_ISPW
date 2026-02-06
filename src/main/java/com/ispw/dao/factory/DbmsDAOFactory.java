package com.ispw.dao.factory;

import com.ispw.dao.impl.dbms.concrete.DbmsCampoDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsFatturaDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsGeneralUserDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsGestoreDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsLogDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsPagamentoDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsPenalitaDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsPrenotazioneDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsRegolePenalitaDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsRegoleTempisticheDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsUtenteFinaleDAO;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
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

public final class DbmsDAOFactory extends DAOFactory {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: factory concreta per DAO DBMS.
    // A2) Stato: connection factory e istanze DAO memoizzate.
    // ========================

    private final DbmsConnectionFactory cf = DbmsConnectionFactory.getInstance();

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

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) get*DAO: lazy init con connection factory.
    // ========================

    @Override
    public synchronized CampoDAO getCampoDAO() {
        if (campoDAO == null) campoDAO = new DbmsCampoDAO(cf);
        return campoDAO;
    }

    @Override
    public synchronized FatturaDAO getFatturaDAO() {
        if (fatturaDAO == null) fatturaDAO = new DbmsFatturaDAO(cf);
        return fatturaDAO;
    }

    @Override
    public synchronized GeneralUserDAO getGeneralUserDAO() {
        if (generalUserDAO == null) generalUserDAO = new DbmsGeneralUserDAO(cf);
        return generalUserDAO;
    }

    @Override
    public synchronized GestoreDAO getGestoreDAO() {
        if (gestoreDAO == null) gestoreDAO = new DbmsGestoreDAO(cf);
        return gestoreDAO;
    }

    @Override
    public synchronized UtenteFinaleDAO getUtenteFinaleDAO() {
        if (utenteFinaleDAO == null) utenteFinaleDAO = new DbmsUtenteFinaleDAO(cf);
        return utenteFinaleDAO;
    }

    @Override
    public synchronized LogDAO getLogDAO() {
        if (logDAO == null) logDAO = new DbmsLogDAO(cf);
        return logDAO;
    }

    @Override
    public synchronized PagamentoDAO getPagamentoDAO() {
        if (pagamentoDAO == null) pagamentoDAO = new DbmsPagamentoDAO(cf);
        return pagamentoDAO;
    }

    @Override
    public synchronized PenalitaDAO getPenalitaDAO() {
        if (penalitaDAO == null) penalitaDAO = new DbmsPenalitaDAO(cf);
        return penalitaDAO;
    }

    @Override
    public synchronized PrenotazioneDAO getPrenotazioneDAO() {
        if (prenotazioneDAO == null) prenotazioneDAO = new DbmsPrenotazioneDAO(cf);
        return prenotazioneDAO;
    }

    @Override
    public synchronized RegoleTempisticheDAO getRegoleTempisticheDAO() {
        if (regoleTempisticheDAO == null) regoleTempisticheDAO = new DbmsRegoleTempisticheDAO(cf);
        return regoleTempisticheDAO;
    }

    @Override
    public synchronized RegolePenalitaDAO getRegolePenalitaDAO() {
        if (regolePenalitaDAO == null) regolePenalitaDAO = new DbmsRegolePenalitaDAO(cf);
        return regolePenalitaDAO;
    }

}