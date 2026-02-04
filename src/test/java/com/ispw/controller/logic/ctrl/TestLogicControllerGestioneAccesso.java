package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestLogicControllerGestioneAccesso extends BaseDAOTest {

    private LogicControllerGestioneAccesso controller;
    private GeneralUserDAO userDAO;
    private LogDAO logDAO;

   
    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        logDAO  = DAOFactory.getInstance().getLogDAO();
        // LogicController ora accede ai DAO on-demand via DAOFactory
        controller = new LogicControllerGestioneAccesso();

        // pulizia in-memory se disponibile
        tryClear(userDAO);
        tryClear(logDAO);

        // seed: utente attivo
        final UtenteFinale u = new UtenteFinale();
        u.setNome("Mario");
        u.setEmail("login@example.org");
        u.setPassword("Secret!23");
        u.setStatoAccount(StatoAccount.ATTIVO);
        userDAO.store(u);
    }

    @Test
    @DisplayName("1) Login happy path → sessione non nulla + log ACCESSO_ESEGUITO")
    void testLoginHappyPath() {
        final DatiLoginBean dl = new DatiLoginBean("login@example.org", "Secret!23");

        final SessioneUtenteBean sessione = controller.verificaCredenziali(dl);
        assertNotNull(sessione, "La sessione deve essere creata");
        assertNotNull(sessione.getIdSessione());
        assertEquals("login@example.org", sessione.getUtente().getEmail());

        controller.saveLog(sessione);

        final int id = userDAO.findByEmail("login@example.org").getIdUtente();
        final List<SystemLog> logs = logDAO.findByUtente(id);
        assertFalse(logs.isEmpty(), "Deve esistere almeno un log");
        assertEquals(TipoOperazione.ACCESSO_ESEGUITO, logs.get(0).getTipoOperazione());
    }

    @Test
    @DisplayName("2) Password errata → sessione null, nessun log")
    void testPasswordErrata() {
        final DatiLoginBean dl = new DatiLoginBean("login@example.org", "wrong");
        assertNull(controller.verificaCredenziali(dl));
        assertTrue(logDAO.findLast(1).isEmpty());
    }

    @Test
    @DisplayName("3) Account non attivo → sessione null")
    void testAccountNonAttivo() {
        final UtenteFinale u = new UtenteFinale();
        u.setNome("Anna");
        u.setEmail("anna@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.DA_CONFERMARE);
        userDAO.store(u);

        assertNull(controller.verificaCredenziali(new DatiLoginBean("anna@example.org", "pwd")));
    }

    @Test
    @DisplayName("4) saveLog con sessione null / utente inesistente → no-op")
    void testSaveLogNoOp() {
        controller.saveLog(null);

        final SessioneUtenteBean s = new SessioneUtenteBean("id", 
                new com.ispw.bean.UtenteBean("X","", "missing@example.org", null),
                new java.util.Date());
        controller.saveLog(s);

        assertTrue(logDAO.findLast(1).isEmpty());
    }

    /** Invoca clear() se presente, senza importare i concreti in-memory */
    private static void tryClear(Object dao) {
        if (dao == null) return;
        try {
            Method m = dao.getClass().getMethod("clear");
            m.setAccessible(true);
            m.invoke(dao);
        } catch (ReflectiveOperationException ignored) { /* ignored: clear may not exist on all DAOs */ }
    }
}