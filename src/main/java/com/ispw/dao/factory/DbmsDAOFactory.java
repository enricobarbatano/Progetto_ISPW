package com.ispw.dao.factory;

import com.ispw.dao.impl.dbms.concrete.CampoDAODbms;
import com.ispw.dao.impl.dbms.concrete.DbmsGestoreDAO;
import com.ispw.dao.impl.dbms.concrete.DbmsRegoleTempisticheDAO;
import com.ispw.dao.impl.dbms.concrete.FatturaDAODbms;
import com.ispw.dao.impl.dbms.concrete.GeneralUserDAODbms;
import com.ispw.dao.impl.dbms.concrete.LogDAODbms;
import com.ispw.dao.impl.dbms.concrete.PagamentoDAODbms;
import com.ispw.dao.impl.dbms.concrete.PenalitaDAODbms;
import com.ispw.dao.impl.dbms.concrete.PrenotazioneDAODbms;
import com.ispw.dao.impl.dbms.concrete.RegolePenalitaDAODbms;
import com.ispw.dao.impl.dbms.concrete.UtenteFinaleDAODbms;
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

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: factory concreta per DAO DBMS.
    // A2) Stato: connection factory e istanze DAO memoizzate.

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

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) get*DAO: lazy init con connection factory.

    @Override
    public synchronized CampoDAO getCampoDAO() {
        if (campoDAO == null) campoDAO = new CampoDAODbms(cf);
        return campoDAO;
    }

    @Override
    public synchronized FatturaDAO getFatturaDAO() {
        if (fatturaDAO == null) fatturaDAO = new FatturaDAODbms(cf);
        return fatturaDAO;
    }

    @Override
    public synchronized GeneralUserDAO getGeneralUserDAO() {
        if (generalUserDAO == null) generalUserDAO = new GeneralUserDAODbms(cf);
        return generalUserDAO;
    }

    @Override
    public synchronized GestoreDAO getGestoreDAO() {
        if (gestoreDAO == null) gestoreDAO = new DbmsGestoreDAO(cf);
        return gestoreDAO;
    }

    @Override
    public synchronized UtenteFinaleDAO getUtenteFinaleDAO() {
        if (utenteFinaleDAO == null) utenteFinaleDAO = new UtenteFinaleDAODbms(cf);
        return utenteFinaleDAO;
    }

    @Override
    public synchronized LogDAO getLogDAO() {
        if (logDAO == null) logDAO = new LogDAODbms(cf);
        return logDAO;
    }

    @Override
    public synchronized PagamentoDAO getPagamentoDAO() {
        if (pagamentoDAO == null) pagamentoDAO = new PagamentoDAODbms(cf);
        return pagamentoDAO;
    }

    @Override
    public synchronized PenalitaDAO getPenalitaDAO() {
        if (penalitaDAO == null) penalitaDAO = new PenalitaDAODbms(cf);
        return penalitaDAO;
    }

    @Override
    public synchronized PrenotazioneDAO getPrenotazioneDAO() {
        if (prenotazioneDAO == null) prenotazioneDAO = new PrenotazioneDAODbms(cf);
        return prenotazioneDAO;
    }

    @Override
    public synchronized RegoleTempisticheDAO getRegoleTempisticheDAO() {
        if (regoleTempisticheDAO == null) regoleTempisticheDAO = new DbmsRegoleTempisticheDAO(cf);
        return regoleTempisticheDAO;
    }

    @Override
    public synchronized RegolePenalitaDAO getRegolePenalitaDAO() {
        if (regolePenalitaDAO == null) regolePenalitaDAO = new RegolePenalitaDAODbms(cf);
        return regolePenalitaDAO;
    }

}
