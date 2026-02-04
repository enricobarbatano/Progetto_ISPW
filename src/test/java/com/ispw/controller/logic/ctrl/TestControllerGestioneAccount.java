package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.util.Date;
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
import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaGestioneAccount;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Test JUnit 5 per il caso d'uso "Gestione Account".
 * Assunzione: a runtime i DAO creati sono In-Memory.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerGestioneAccount extends BaseDAOTest {

    private LogicControllerGestioneAccount controller;
    private GeneralUserDAO userDAO;
    private LogDAO logDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerGestioneAccount();
        userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        logDAO  = DAOFactory.getInstance().getLogDAO();

        assertNotNull(userDAO, "DAOFactory.getGeneralUserDAO() ha restituito null");
        assertNotNull(logDAO,  "DAOFactory.getLogDAO() ha restituito null");

        // Pulizia store in-memory (se i concreti espongono clear())
        tryClear(userDAO);
        tryClear(logDAO);
    }

    // =====================================================================================
    // 1) RECUPERO INFORMAZIONI ACCOUNT
    // =====================================================================================

    @Test
    @DisplayName("1) Recupero: sessione valida → ritorna DatiAccountBean (telefono/indirizzo null)")
    void testRecuperaInformazioniAccount_HappyPath() {
        // Arrange: utente presente in DAO
        String email = "mario.rossi@example.org";
        UtenteFinale u = new UtenteFinale();
        u.setNome("Mario");
        u.setCognome("Rossi");
        u.setEmail(email);
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        SessioneUtenteBean sessione = new SessioneUtenteBean(
                "SID-1", new UtenteBean(u.getNome(), u.getCognome(), u.getEmail(), u.getRuolo()), new Date());

        // Act
        DatiAccountBean out = controller.recuperaInformazioniAccount(sessione);

        // Assert
        assertNotNull(out, "DatiAccountBean non nullo");
        assertEquals(u.getIdUtente(), out.getIdUtente());
        assertEquals(u.getNome(), out.getNome());
        assertEquals(u.getCognome(), out.getCognome());
        assertEquals(email, out.getEmail());
        assertNull(out.getTelefono(), "GeneralUser non espone telefono → null");
        assertNull(out.getIndirizzo(), "GeneralUser non espone indirizzo → null");
    }

    @Test
    @DisplayName("2) Recupero: sessione/utente/email non validi → null")
    void testRecuperaInformazioniAccount_SessioneKO() {
        assertNull(controller.recuperaInformazioniAccount(null));

        SessioneUtenteBean s1 = new SessioneUtenteBean("SID", null, new Date());
        assertNull(controller.recuperaInformazioniAccount(s1));

        SessioneUtenteBean s2 = new SessioneUtenteBean("SID",
                new UtenteBean("X","Y","   ", Ruolo.UTENTE), new Date());
        assertNull(controller.recuperaInformazioniAccount(s2));
    }

    @Test
    @DisplayName("3) Recupero: utente assente in DAO → null")
    void testRecuperaInformazioniAccount_UtenteAssente() {
        SessioneUtenteBean sessione = new SessioneUtenteBean("SID",
                new UtenteBean("Ghost","User","ghost@example.org", Ruolo.UTENTE), new Date());
        assertNull(controller.recuperaInformazioniAccount(sessione));
    }

    // =====================================================================================
    // 2) AGGIORNA DATI ACCOUNT
    // =====================================================================================

    @Test
    @DisplayName("4) Aggiorna: nome + email (overload) → OK, notifica inviata e log presente")
    void testAggiornaDatiAccount_HappyPath_Notifica() {
        // Arrange
        String email = "anna@example.org";
        UtenteFinale u = new UtenteFinale();
        u.setNome("Anna");
        u.setCognome("Bianchi");
        u.setEmail(email);
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        DatiAccountBean dati = new DatiAccountBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setNome("Anna Maria");
        dati.setEmail("anna.updated@example.org");

        FakeNotificaGestioneAccount fake = new FakeNotificaGestioneAccount();

        // Act
        EsitoOperazioneBean esito = controller.aggiornaDatiAccount(dati, fake);

        // Assert esito
        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), "L'aggiornamento deve andare a buon fine");
        assertEquals(1, fake.getInvocations(), "Notifica deve essere inviata");
        assertNotNull(fake.getLastUtente());
        assertEquals(dati.getEmail(), fake.getLastUtente().getEmail());

        // Assert persist
        GeneralUser after = userDAO.findById(u.getIdUtente());
        assertNotNull(after);
        assertEquals("Anna Maria", after.getNome());
        assertEquals("anna.updated@example.org", after.getEmail());

        // Assert log presente
        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve esistere almeno un log");
        assertTrue(logs.get(0).getDescrizione() != null && !logs.get(0).getDescrizione().isBlank());
    }

    @Test
    @DisplayName("5) Aggiorna: email duplicata → esito KO (nessun log)")
    void testAggiornaDatiAccount_EmailDuplicata() {
        // Arrange: due utenti
        UtenteFinale a = new UtenteFinale();
        a.setNome("Luca");
        a.setCognome("Verdi");
        a.setEmail("luca@example.org");
        a.setPassword("pwd");
        a.setStatoAccount(StatoAccount.ATTIVO);
        a.setRuolo(Ruolo.UTENTE);
        userDAO.store(a);

        UtenteFinale b = new UtenteFinale();
        b.setNome("Marco");
        b.setCognome("Neri");
        b.setEmail("dup@example.org");
        b.setPassword("pwd");
        b.setStatoAccount(StatoAccount.ATTIVO);
        b.setRuolo(Ruolo.UTENTE);
        userDAO.store(b);

        DatiAccountBean dati = new DatiAccountBean();
        dati.setIdUtente(a.getIdUtente());
        dati.setEmail("dup@example.org"); // tenta duplicazione

        // Act
        EsitoOperazioneBean esito = controller.aggiornaDatiAccount(dati);

        // Assert KO
        assertNotNull(esito);
        assertFalse(esito.isSuccesso(), "Deve fallire per email duplicata");

        // Nessun nuovo log per l'utente A
        List<SystemLog> logs = logDAO.findByUtente(a.getIdUtente());
        assertTrue(logs.isEmpty(), "Non dovrebbero essere stati aggiunti log");
    }

    @Test
    @DisplayName("6) Aggiorna: utente non trovato/id non valido → KO")
    void testAggiornaDatiAccount_UtenteAssente() {
        DatiAccountBean dati = new DatiAccountBean();
        dati.setIdUtente(0);
        EsitoOperazioneBean esito = controller.aggiornaDatiAccount(dati);
        assertNotNull(esito);
        assertFalse(esito.isSuccesso());
    }

    // =====================================================================================
    // 3) CAMBIA PASSWORD
    // =====================================================================================

    @Test
    @DisplayName("7) Password: vecchia OK (overload) → aggiornata, notifica inviata, log presente")
    void testCambiaPassword_HappyPath() {
        // Arrange
        String email = "sara@example.org";
        UtenteFinale u = new UtenteFinale();
        u.setNome("Sara");
        u.setCognome("Bianchi");
        u.setEmail(email);
        u.setPassword("oldPwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        SessioneUtenteBean sessione = new SessioneUtenteBean(
                "SID", new UtenteBean(u.getNome(), u.getCognome(), u.getEmail(), u.getRuolo()), new Date());

        FakeNotificaGestioneAccount fake = new FakeNotificaGestioneAccount();

        // Act
        EsitoOperazioneBean esito = controller.cambiaPassword("oldPwd", "newPass1", sessione, fake);

        // Assert
        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), "Il cambio password deve andare a buon fine");
        assertEquals(1, fake.getInvocations(), "Notifica deve essere inviata");

        GeneralUser after = userDAO.findByEmail(email);
        assertNotNull(after);
        assertEquals("newPass1", after.getPassword());

        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve esistere almeno un log");
    }

    @Test
    @DisplayName("8) Password: vecchia errata → esito KO")
    void testCambiaPassword_VecchiaErrata() {
        String email = "paolo@example.org";
        UtenteFinale u = new UtenteFinale();
        u.setNome("Paolo");
        u.setCognome("R");
        u.setEmail(email);
        u.setPassword("pwdOK");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        SessioneUtenteBean sessione = new SessioneUtenteBean(
                "SID", new UtenteBean(u.getNome(), u.getCognome(), u.getEmail(), u.getRuolo()), new Date());

        EsitoOperazioneBean esito = controller.cambiaPassword("wrong", "newPass1", sessione);

        assertNotNull(esito);
        assertFalse(esito.isSuccesso(), "Deve fallire per vecchia password errata");
    }

    @Test
    @DisplayName("9) Password: sessione non valida / password nuova troppo corta → KO")
    void testCambiaPassword_SessioneOPwdKO() {
        // Sessione null
        EsitoOperazioneBean e1 = controller.cambiaPassword("x", "newPass1", null);
        assertNotNull(e1); assertFalse(e1.isSuccesso());

        // Email in sessione blank
        SessioneUtenteBean s2 = new SessioneUtenteBean(
                "SID", new UtenteBean("N","C","   ", Ruolo.UTENTE), new Date());
        EsitoOperazioneBean e2 = controller.cambiaPassword("x", "newPass1", s2);
        assertNotNull(e2); assertFalse(e2.isSuccesso());

        // Nuova password troppo corta
        SessioneUtenteBean s3 = new SessioneUtenteBean(
                "SID", new UtenteBean("N","C","z@example.org", Ruolo.UTENTE), new Date());
        EsitoOperazioneBean e3 = controller.cambiaPassword("old", "123", s3);
        assertNotNull(e3); assertFalse(e3.isSuccesso());
    }

    // =====================================================================================
    // 4) CONFERMA MODIFICA ACCOUNT
    // =====================================================================================

    @Test
    @DisplayName("10) Conferma: porta stato a ATTIVO e scrive log")
    void testConfermaModificaAccount() {
        String email = "giulia@example.org";
        UtenteFinale u = new UtenteFinale();
        u.setNome("Giulia");
        u.setCognome("Neri");
        u.setEmail(email);
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.DA_CONFERMARE);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        UtenteBean ub = new UtenteBean(u.getNome(), u.getCognome(), u.getEmail(), u.getRuolo());

        // Act
        controller.confermaModificaAccount(ub);

        // Assert
        GeneralUser after = userDAO.findByEmail(email);
        assertNotNull(after);
        assertEquals(StatoAccount.ATTIVO, after.getStatoAccount());

        List<SystemLog> logs = logDAO.findByUtente(after.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve esistere almeno un log");
        assertTrue(logs.get(0).getDescrizione() != null && !logs.get(0).getDescrizione().isBlank());
    }

    // =========================
    // Fake per GestioneNotificaGestioneAccount
    // =========================

    private static final class FakeNotificaGestioneAccount implements GestioneNotificaGestioneAccount {
        private int invocations;
        private UtenteBean lastUtente;

        @Override
        public void inviaConfermaAggiornamentoAccount(UtenteBean utente) {
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