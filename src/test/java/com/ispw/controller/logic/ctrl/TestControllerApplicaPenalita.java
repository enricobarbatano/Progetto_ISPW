package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.Fattura;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Test JUnit 5 per "Applica Penalità".
 * Assunzione: In-Memory DAO attivi (vedi BaseDAOTest).
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerApplicaPenalita extends BaseDAOTest {

    private LogicControllerApplicaPenalita controller;

    private GeneralUserDAO    userDAO;
    private PenalitaDAO       penalitaDAO;
    private LogDAO            logDAO;
    private RegolePenalitaDAO rulesDAO;

    @BeforeEach
    void setUp() {
        userDAO     = DAOFactory.getInstance().getGeneralUserDAO();
        penalitaDAO = DAOFactory.getInstance().getPenalitaDAO();
        logDAO      = DAOFactory.getInstance().getLogDAO();
        rulesDAO    = DAOFactory.getInstance().getRegolePenalitaDAO();

        tryClear(userDAO);
        tryClear(penalitaDAO);
        tryClear(logDAO);
        tryClear(rulesDAO);

        controller = new LogicControllerApplicaPenalita(userDAO, penalitaDAO, rulesDAO);
    }

    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("1) Penalità base: happy path → OK, log scritto (no notifica/pagamento/fattura)")
    void testApplicaSanzione_Base_HappyPath() {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(1);
        u.setNome("Mario");
        u.setEmail("mario@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("No-show prenotazione");
        dati.setDataDecorrenza(LocalDate.of(2026, 1, 10));
        dati.setImporto(new BigDecimal("15.00"));

        EsitoOperazioneBean esito = controller.applicaSanzione(dati, null, null, null, null, null);

        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), "L'applicazione della penalità deve andare a buon fine");
        assertNotNull(esito.getMessaggio());

        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve essere presente un log");
        SystemLog last = logs.get(0);
        assertNotNull(last.getTimestamp());
        assertTrue(last.getDescrizione() != null && !last.getDescrizione().isBlank(),
                "La descrizione del log non deve essere vuota");
    }

    // -------------------------------------------------------
    @Test
    @DisplayName("2) Penalità: utente inesistente → esito KO, nessun log")
    void testApplicaSanzione_UtenteAssente() {
        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(9999);
        dati.setMotivazione("Test");
        dati.setDataDecorrenza(LocalDate.of(2026, 1, 10));
        dati.setImporto(new BigDecimal("10"));

        EsitoOperazioneBean esito = controller.applicaSanzione(dati, null, null, null, null, null);

        assertNotNull(esito);
        assertFalse(esito.isSuccesso(), "Deve fallire per utente non esistente");

        List<SystemLog> logs = logDAO.findLast(1);
        assertTrue(logs.isEmpty(), "Non devono essere presenti log");
    }

    // ---------------------------------------------------------------
    @Test
    @DisplayName("3) Penalità: dati non validi (id<=0 o motivazione vuota) → KO")
    void testApplicaSanzione_DatiNonValidi() {
        DatiPenalitaBean dati1 = new DatiPenalitaBean();
        dati1.setIdUtente(0);
        dati1.setMotivazione("x");
        EsitoOperazioneBean esito1 = controller.applicaSanzione(dati1, null, null, null, null, null);
        assertFalse(esito1.isSuccesso());

        DatiPenalitaBean dati2 = new DatiPenalitaBean();
        dati2.setIdUtente(1);
        dati2.setMotivazione("   ");
        EsitoOperazioneBean esito2 = controller.applicaSanzione(dati2, null, null, null, null, null);
        assertFalse(esito2.isSuccesso());
    }

    // -------------------------------------------------------------------
    @Test
    @DisplayName("4) Penalità: importo assente → usa RegolePenalitaDAO (happy path)")
    void testApplicaSanzione_ImportoDaRegole() {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(2);
        u.setNome("Anna");
        u.setEmail("anna@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        RegolePenalita rp = new RegolePenalita();
        rp.setValorePenalita(new BigDecimal("12.50"));
        trySetRegolaPenalita(rulesDAO, rp);

        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("Ritardo check-in");
        dati.setDataDecorrenza(LocalDate.of(2026, 1, 12));
        dati.setImporto(null);

        EsitoOperazioneBean esito = controller.applicaSanzione(dati, null, null, null, null, null);

        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), "La penalità deve andare a buon fine usando l'importo da regole");

        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("5) Orchestrazione → notifica/pagamento/fattura invocati; default importo/metodo")
    void testApplicaSanzione_Overload_Orchestrazione() {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(3);
        u.setNome("Luca");
        u.setEmail("luca@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("Danni al campo");
        dati.setDataDecorrenza(LocalDate.of(2026, 2, 1));
        BigDecimal importoPen = new BigDecimal("30.00");
        dati.setImporto(importoPen);

        DatiPagamentoBean pay = new DatiPagamentoBean();
        pay.setImporto(0f);
        pay.setMetodo("  "); // blank

        DatiFatturaBean fatt = new DatiFatturaBean();
        fatt.setDataOperazione(null);

        FakePagamentoPenalita fakePay = new FakePagamentoPenalita();
        FakeFatturaPenalita   fakeFatt = new FakeFatturaPenalita();
        FakeNotificaPenalita  fakeNoti = new FakeNotificaPenalita();

        EsitoOperazioneBean esito = controller.applicaSanzione(
                dati, pay, fatt, fakePay, fakeFatt, fakeNoti);

        assertNotNull(esito);
        assertTrue(esito.isSuccesso());

        assertEquals(1, fakeNoti.invocations, "Notifica penalità deve essere inviata una volta");
        assertEquals(String.valueOf(u.getIdUtente()), fakeNoti.lastIdUtente);

        assertEquals(1, fakePay.invocations, "Pagamento penalità deve essere richiesto una volta");
        assertTrue(fakePay.lastIdPenalita > 0, "L'ID penalità passato al pagamento deve essere > 0");
        assertEquals(importoPen.floatValue(), pay.getImporto(), 0.0001f);
        assertEquals("PAYPAL", pay.getMetodo());

        assertEquals(1, fakeFatt.invocations, "Fattura penalità deve essere generata una volta");
        assertTrue(fakeFatt.lastIdPenalita > 0);
        assertEquals(fakePay.lastIdPenalita, fakeFatt.lastIdPenalita,
                "ID penalità deve combaciare tra pagamento e fattura");

        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty());
    }

    // ---------------------------------------------------------
    @Test
    @DisplayName("6) Penalità: importo non valido (bean<=0 e regole assenti/<=0) → KO")
    void testApplicaSanzione_ImportoNonValido() {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(4);
        u.setNome("Sara");
        u.setEmail("sara@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        RegolePenalita rp = new RegolePenalita();
        rp.setValorePenalita(BigDecimal.ZERO); // <= 0 => non valida
        trySetRegolaPenalita(rulesDAO, rp);

        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("Test importo non valido");
        dati.setDataDecorrenza(LocalDate.of(2026, 2, 1));
        dati.setImporto(new BigDecimal("0"));

        EsitoOperazioneBean esito = controller.applicaSanzione(dati, null, null, null, null, null);

        assertNotNull(esito);
        assertFalse(esito.isSuccesso(), "Deve fallire per importo non valido (bean<=0 e regole<=0)");
    }

    // =========================================================
    // Fake per i collaboratori secondari (DIP by-parameter)
    // =========================================================
    private static final class FakePagamentoPenalita implements GestionePagamentoPenalita {
        int invocations;
        DatiPagamentoBean lastDati;
        int lastIdPenalita;

        @Override
        public StatoPagamentoBean richiediPagamentoPenalita(DatiPagamentoBean dati, int idPenalita) {
            this.invocations++;
            this.lastDati = dati;
            this.lastIdPenalita = idPenalita;
            return new StatoPagamentoBean();
        }
    }

    private static final class FakeFatturaPenalita implements GestioneFatturaPenalita {
        int invocations;
        DatiFatturaBean lastDati;
        int lastIdPenalita;

        @Override
        public Fattura generaFatturaPenalita(DatiFatturaBean dati, int idPenalita) {
            this.invocations++;
            this.lastDati = dati;
            this.lastIdPenalita = idPenalita;
            return new Fattura();
        }
    }

    private static final class FakeNotificaPenalita implements GestioneNotificaPenalita {
        int invocations;
        String lastIdUtente;

        @Override
        public void inviaNotificaPenalita(String idUtente) {
            this.invocations++;
            this.lastIdUtente = idUtente;
        }
    }

    // =========================================================
    // Helper
    // =========================================================

    /** Invoca clear() se esiste (evita import dei concreti In-Memory). */
    private static void tryClear(Object dao) {
        if (dao == null) return;
        for (String mName : new String[] { "clear", "tryClear", "reset" }) {
            try {
                Method m = dao.getClass().getMethod(mName);
                m.invoke(dao);
                return;
            } catch (ReflectiveOperationException ignored) {
                // prova nome successivo
            }
        }
    }

    /** Prova a salvare la regola penalità su concreti diversi usando i nomi noti via reflection. */
    private static void trySetRegolaPenalita(RegolePenalitaDAO rulesDAO, RegolePenalita rp) {
        if (rulesDAO == null) return;
        for (String name : new String[]{
                "save", "store", "salvaRegolaPenalita", "salvaRegolaPenalità", "set", "setRegolaPenalita"
        }) {
            try {
                Method m = rulesDAO.getClass().getMethod(name, RegolePenalita.class);
                m.invoke(rulesDAO, rp);
                return; // ok
            } catch (ReflectiveOperationException ignored) {
                // prova metodo successivo
            }
        }
    }
}