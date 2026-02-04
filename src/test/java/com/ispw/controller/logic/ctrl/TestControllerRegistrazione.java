package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.DatiRegistrazioneBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaRegistrazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;
import com.ispw.model.enums.TipoOperazione;

/**
 * Test JUnit 5 per il caso d'uso "Registrazione".
 * Assunzione: a runtime i DAO creati sono In-Memory.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerRegistrazione extends BaseDAOTest {

    private LogicControllerRegistrazione controller;
    private GeneralUserDAO userDAO;
    private LogDAO logDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerRegistrazione();
        userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        logDAO  = DAOFactory.getInstance().getLogDAO();

        // Pulizia store in-memory (se i concreti espongono clear())
        tryClear(userDAO);
        tryClear(logDAO);
    }

    @Test
    @DisplayName("1) Registrazione: happy path → DA_CONFERMARE, log REGISTRAZIONE_ACCOUNT, notifica inviata")
    void testRegistraNuovoUtente_HappyPath() {
        // Arrange
        DatiRegistrazioneBean dati = new DatiRegistrazioneBean();
        dati.setNome("Mario");
        dati.setCognome("Rossi");
        dati.setEmail("mario.rossi@example.org");
        dati.setPassword("Secret!23");

        FakeNotificaRegistrazione fake = new FakeNotificaRegistrazione();

        // Act
        EsitoOperazioneBean esito = controller.registraNuovoUtente(dati, fake);

        // Assert esito
        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), "La registrazione dovrebbe andare a buon fine");
        assertNotNull(esito.getMessaggio());

        // Assert utente creato in stato DA_CONFERMARE
        GeneralUser u = userDAO.findByEmail(dati.getEmail());
        assertNotNull(u, "Utente deve esistere dopo registrazione");
        assertEquals(StatoAccount.DA_CONFERMARE, u.getStatoAccount());

        // Assert notifica inviata
        assertEquals(1, fake.getInvocations(), "La notifica deve essere inviata una sola volta");
        assertNotNull(fake.getLastUtente());
        assertEquals(dati.getEmail(), fake.getLastUtente().getEmail());

        // Assert log REGISTRAZIONE_ACCOUNT
        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve esistere almeno un log");
        SystemLog primo = logs.get(0); // ordinati DESC (ultimo in testa)
        assertEquals(TipoOperazione.REGISTRAZIONE_ACCOUNT, primo.getTipoOperazione());
        assertTrue(primo.getDescrizione() != null && !primo.getDescrizione().isBlank());
    }

    @Test
    @DisplayName("2) Registrazione: email duplicata → esito KO, nessuna notifica, nessun log")
    void testRegistraNuovoUtente_EmailDuplicata() {
        // Arrange: utente pre-esistente
        UtenteFinale esistente = new UtenteFinale();
        esistente.setNome("Mario");
        esistente.setCognome("Bianchi");
        esistente.setEmail("dup@example.org");
        esistente.setPassword("pwd");
        esistente.setStatoAccount(StatoAccount.DA_CONFERMARE);
        esistente.setRuolo(Ruolo.UTENTE);
        userDAO.store(esistente);

        DatiRegistrazioneBean dati = new DatiRegistrazioneBean();
        dati.setNome("Any");
        dati.setCognome("Person");
        dati.setEmail("dup@example.org");
        dati.setPassword("x");

        FakeNotificaRegistrazione fake = new FakeNotificaRegistrazione();

        // Act
        EsitoOperazioneBean esito = controller.registraNuovoUtente(dati, fake);

        // Assert
        assertNotNull(esito);
        assertFalse(esito.isSuccesso(), "La registrazione deve fallire per email duplicata");
        assertEquals(0, fake.getInvocations(), "Non deve essere inviata alcuna notifica");

        // Nessun nuovo log per l'utente pre-esistente
        List<SystemLog> logs = logDAO.findByUtente(esistente.getIdUtente());
        assertTrue(logs.isEmpty(), "Non dovrebbero essere stati aggiunti log");
    }

    @Test
    @DisplayName("3) Conferma account (via email) → ATTIVO, log ACCOUNT_ATTIVATO")
    void testConfermaNuovoAccount_HappyPath() {
        // Arrange: utente registrato
        UtenteFinale u = new UtenteFinale();
        u.setNome("Anna");
        u.setCognome("Verdi");
        u.setEmail("anna@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.DA_CONFERMARE);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        UtenteBean ub = new UtenteBean(u.getNome(), u.getCognome(), u.getEmail(), u.getRuolo());

        // Act
        controller.confermaNuovoAccount(ub);

        // Assert stato
        GeneralUser r = userDAO.findByEmail(u.getEmail());
        assertNotNull(r);
        assertEquals(StatoAccount.ATTIVO, r.getStatoAccount());

        // Assert log
        List<SystemLog> logs = logDAO.findByUtente(r.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve esistere almeno un log");
        assertEquals(TipoOperazione.ACCOUNT_ATTIVATO, logs.get(0).getTipoOperazione());
    }

    @Test
    @DisplayName("4) Finalizza attivazione (via id) → ATTIVO, log ACCOUNT_ATTIVATO")
    void testFinalizzaAttivazioneAccount_HappyPath() {
        // Arrange
        UtenteFinale u = new UtenteFinale();
        u.setNome("Luca");
        u.setCognome("Neri");
        u.setEmail("luca@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.DA_CONFERMARE);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        // Act
        controller.finalizzaAttivazioneAccount(u.getIdUtente());

        // Assert
        GeneralUser r = userDAO.findById(u.getIdUtente());
        assertNotNull(r);
        assertEquals(StatoAccount.ATTIVO, r.getStatoAccount());

        List<SystemLog> logs = logDAO.findByUtente(r.getIdUtente());
        assertFalse(logs.isEmpty());
        assertEquals(TipoOperazione.ACCOUNT_ATTIVATO, logs.get(0).getTipoOperazione());
    }

    @Test
    @DisplayName("5) Finalizza attivazione con id non valido → no-op, nessun log")
    void testFinalizzaAttivazioneAccount_BadId() {
        // Act
        controller.finalizzaAttivazioneAccount(0);

        // Assert: nessun log presente
        List<SystemLog> logs = logDAO.findLast(1);
        assertTrue(logs.isEmpty(), "Non devono essere presenti log");
    }

    // =========================
    // Fake per GestioneNotificaRegistrazione
    // =========================

    private static final class FakeNotificaRegistrazione implements GestioneNotificaRegistrazione {
        private int invocations;
        private UtenteBean lastUtente;

        @Override
        public void inviaConfermaRegistrazione(UtenteBean utente) {
            this.invocations++;
            this.lastUtente = utente;
        }

        int getInvocations() { return invocations; }
        UtenteBean getLastUtente() { return lastUtente; }
    }
    /** Invoca clear() se esiste (evita import dei concreti in-memory). */
    private static void tryClear(Object dao) {
        if (dao == null) return;
        try {
            Method m = dao.getClass().getMethod("clear");
            m.setAccessible(true);
            m.invoke(dao);
        } catch (ReflectiveOperationException ignored) {
            // il DAO non espone clear(): nessuna azione
        }
    }
}