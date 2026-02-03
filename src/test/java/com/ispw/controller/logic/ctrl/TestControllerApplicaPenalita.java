package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.Fattura;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

/**
 * Test JUnit 5 per il caso d'uso "Applica Penalità".
 * Assunzione: a runtime i DAO creati sono In-Memory (vedi BaseDAOTest).
 *
 * Copre:
 *  1) Metodo base: validazione, importo da bean, log.
 *  2) KO: utente assente, dati non validi, importo non valido.
 *  3) Importo da regole (RegolePenalitaDAO).
 *  4) Overload con orchestrazione (notifica/pagamento/fattura) mantenendo DIP.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerApplicaPenalita extends BaseDAOTest {

    private LogicControllerApplicaPenalita controller;

    private GeneralUserDAO     userDAO;
    private LogDAO             logDAO;
    private RegolePenalitaDAO  rulesDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerApplicaPenalita();

        userDAO  = DAOFactory.getInstance().getGeneralUserDAO();
        logDAO   = DAOFactory.getInstance().getLogDAO();
        rulesDAO = DAOFactory.getInstance().getRegolePenalitaDAO();

        // Pulizia store in-memory (se i concreti espongono clear())
        tryClear(userDAO);
        tryClear(logDAO);
        tryClear(rulesDAO);
    }

    // ------------------------------------------------------------------------------------
    // 1) Metodo base: happy path → esito OK, log scritto, nessuna orchestrazione esterna
    // ------------------------------------------------------------------------------------
    @Test
    @DisplayName("1) Penalità base: happy path → OK, log scritto (no notifica/pagamento/fattura)")
    void testApplicaSanzione_Base_HappyPath() {
        // Arrange: utente esistente (ID esplicito per coerenza provider)
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

        // Act
        EsitoOperazioneBean esito = controller.applicaSanzione(dati);

        // Assert esito
        assertNotNull(esito);
        assertTrue(esito.isSuccess(), "L'applicazione della penalità deve andare a buon fine");
        assertNotNull(esito.getMessaggio());

        // Assert log presente
        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty(), "Deve essere presente un log");
        SystemLog last = logs.get(0);
        assertNotNull(last.getTimestamp());
        assertTrue(last.getDescrizione() != null && !last.getDescrizione().isBlank(),
                "La descrizione del log non deve essere vuota");
    }

    // -------------------------------------------------------
    // 2) KO: utente inesistente → esito KO, nessun log scritto
    // -------------------------------------------------------
    @Test
    @DisplayName("2) Penalità: utente inesistente → esito KO, nessun log")
    void testApplicaSanzione_UtenteAssente() {
        // Arrange
        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(9999); // non esiste
        dati.setMotivazione("Test");
        dati.setDataDecorrenza(LocalDate.of(2026, 1, 10));
        dati.setImporto(new BigDecimal("10"));

        // Act
        EsitoOperazioneBean esito = controller.applicaSanzione(dati);

        // Assert
        assertNotNull(esito);
        assertFalse(esito.isSuccess(), "Deve fallire per utente non esistente");

        // Nessun log
        List<SystemLog> logs = logDAO.findLast(1);
        assertTrue(logs.isEmpty(), "Non devono essere presenti log");
    }

    // ---------------------------------------------------------------
    // 3) KO: dati non validi → id <= 0 o motivazione blank → esito KO
    // ---------------------------------------------------------------
    @Test
    @DisplayName("3) Penalità: dati non validi (id<=0 o motivazione vuota) → KO")
    void testApplicaSanzione_DatiNonValidi() {
        // Case A: idUtente <= 0
        DatiPenalitaBean dati1 = new DatiPenalitaBean();
        dati1.setIdUtente(0);
        dati1.setMotivazione("x");
        EsitoOperazioneBean esito1 = controller.applicaSanzione(dati1);
        assertFalse(esito1.isSuccess());

        // Case B: motivazione mancante
        DatiPenalitaBean dati2 = new DatiPenalitaBean();
        dati2.setIdUtente(1);
        dati2.setMotivazione("   ");
        EsitoOperazioneBean esito2 = controller.applicaSanzione(dati2);
        assertFalse(esito2.isSuccess());
    }

    // -------------------------------------------------------------------
    // 4) Importo da regole: se importo non presente, usa RegolePenalitaDAO
    // -------------------------------------------------------------------
    @Test
    @DisplayName("4) Penalità: importo assente → usa RegolePenalitaDAO (happy path)")
    void testApplicaSanzione_ImportoDaRegole() {
        // Arrange: utente
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(2);
        u.setNome("Anna");
        u.setEmail("anna@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        // Setup regola: valorePenalita > 0
        RegolePenalita rp = new RegolePenalita();
        rp.setValorePenalita(new BigDecimal("12.50"));
        trySetRegolaPenalita(rulesDAO, rp); // prova a salvare via reflection su concreti

        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("Ritardo check-in");
        dati.setDataDecorrenza(LocalDate.of(2026, 1, 12));
        dati.setImporto(null); // forza uso regole

        // Act
        EsitoOperazioneBean esito = controller.applicaSanzione(dati);

        // Assert
        assertNotNull(esito);
        assertTrue(esito.isSuccess(), "La penalità deve andare a buon fine usando l'importo da regole");
        // log presente
        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------
    // 5) Overload con orchestrazione: notifica + pagamento + fattura → tutte invocate (DIP by-parameter)
    //    - DatiPagamentoBean.importo == 0 => impostato all'importo penalità
    //    - DatiPagamentoBean.metodo blank => default "PAYPAL"
    // ---------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("5) Overload orchestrazione → notifica/pagamento/fattura invocati; default importo/metodo")
    void testApplicaSanzione_Overload_Orchestrazione() {
        // Arrange: utente
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(3);
        u.setNome("Luca");
        u.setEmail("luca@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        // Penalità con data e importo fissati (per ID deterministico replicabile)
        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("Danni al campo");
        dati.setDataDecorrenza(LocalDate.of(2026, 2, 1));
        BigDecimal importoPen = new BigDecimal("30.00");
        dati.setImporto(importoPen);

        // DTO pagamento: importo=0 e metodo blank → il controller deve impostarli
        DatiPagamentoBean pay = new DatiPagamentoBean();
        pay.setImporto(0f);
        pay.setMetodo("  "); // blank

        // DTO fattura (data null per testare default a oggi)
        DatiFatturaBean fatt = new DatiFatturaBean();
        fatt.setDataOperazione(null);

        // Fakes secondari
        FakePagamentoPenalita fakePay = new FakePagamentoPenalita();
        FakeFatturaPenalita   fakeFatt = new FakeFatturaPenalita();
        FakeNotificaPenalita  fakeNoti = new FakeNotificaPenalita();

        // Calcolo ID atteso come nel controller (deterministico)
        int expectedId = computeIdPenalitaDeterministico(
                u.getIdUtente(), dati.getMotivazione(), dati.getDataDecorrenza(), importoPen);

        // Act
        EsitoOperazioneBean esito = controller.applicaSanzione(
                dati, pay, fatt, fakePay, fakeFatt, fakeNoti);

        // Assert esito
        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        // Assert invocazioni
        assertEquals(1, fakeNoti.invocations, "Notifica penalità deve essere inviata una volta");
        assertEquals(String.valueOf(u.getIdUtente()), fakeNoti.lastIdUtente);

        assertEquals(1, fakePay.invocations, "Pagamento penalità deve essere richiesto una volta");
        assertEquals(expectedId, fakePay.lastIdPenalita, "ID penalità passato al pagamento non combacia");
        // Il controller deve aver impostato importo/metodo di pay
        assertEquals(importoPen.floatValue(), pay.getImporto(), 0.0001f);
        assertEquals("PAYPAL", pay.getMetodo());

        assertEquals(1, fakeFatt.invocations, "Fattura penalità deve essere generata una volta");
        assertEquals(expectedId, fakeFatt.lastIdPenalita);

        // Assert log presente
        List<SystemLog> logs = logDAO.findByUtente(u.getIdUtente());
        assertFalse(logs.isEmpty());
    }

    // ---------------------------------------------------------
    // 6) KO: importo non valido (né bean né regole) → esito KO
    // ---------------------------------------------------------
    @Test
    @DisplayName("6) Penalità: importo non valido (bean<=0 e regole assenti/<=0) → KO")
    void testApplicaSanzione_ImportoNonValido() {
        // Arrange: utente esistente
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(4);
        u.setNome("Sara");
        u.setEmail("sara@example.org");
        u.setPassword("pwd");
        u.setStatoAccount(StatoAccount.ATTIVO);
        u.setRuolo(Ruolo.UTENTE);
        userDAO.store(u);

        // Forza regola con valorePenalita <= 0 (blocca la "via di fuga" verso OK)
        RegolePenalita rp = new RegolePenalita();
        rp.setValorePenalita(BigDecimal.ZERO); // <= 0 => non valida
        trySetRegolaPenalita(rulesDAO, rp);

        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(u.getIdUtente());
        dati.setMotivazione("Test importo non valido");
        dati.setDataDecorrenza(LocalDate.of(2026, 2, 1));
        dati.setImporto(new BigDecimal("0")); // <= 0

        // Act
        EsitoOperazioneBean esito = controller.applicaSanzione(dati);

        // Assert
        assertNotNull(esito);
        assertFalse(esito.isSuccess(), "Deve fallire per importo non valido (bean<=0 e regole<=0)");
    }

    // =========================================================
    // Fake per i collaboratori secondari (DIP by-parameter)
    // =========================================================
    private static final class FakePagamentoPenalita implements GestionePagamentoPenalita {
        int invocations;
        DatiPagamentoBean lastDati;
        int lastIdPenalita;

        @Override
        public StatoPagamentoBean richiediPagamentoPenalità(DatiPagamentoBean dati, int idPenalità) {
            this.invocations++;
            this.lastDati = dati;
            this.lastIdPenalita = idPenalità;
            return new StatoPagamentoBean(); // il controller non usa il ritorno
        }
    }

    private static final class FakeFatturaPenalita implements GestioneFatturaPenalita {
        int invocations;
        DatiFatturaBean lastDati;
        int lastIdPenalita;

        @Override
        public Fattura generaFatturaPenalita(DatiFatturaBean dati, int idPenalità) {
            this.invocations++;
            this.lastDati = dati;
            this.lastIdPenalita = idPenalità;
            return new Fattura(); // il controller non usa il ritorno
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
                m.setAccessible(true);
                m.invoke(dao);
                return;
            } catch (ReflectiveOperationException ignored) {
                // tenta nome successivo
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
                m.setAccessible(true);
                m.invoke(rulesDAO, rp);
                return; // ok
            } catch (ReflectiveOperationException ignored) {
                // prova metodo successivo
            }
        }
    }

    /** Replica il computeIdPenalitaDeterministico del controller per asserire l'ID passato ai fake. */
    private static int computeIdPenalitaDeterministico(int idUtente, String motivazione,
                                                       LocalDate dataDecorrenza, BigDecimal importo) {
        int h = Objects.hash(
                idUtente,
                motivazione != null ? motivazione.trim() : "",
                dataDecorrenza != null ? dataDecorrenza : LocalDate.now(),
                importo != null ? importo : BigDecimal.ZERO
        );
        return Math.abs(h == Integer.MIN_VALUE ? h + 1 : h);
    }
}