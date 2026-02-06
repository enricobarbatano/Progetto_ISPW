package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
import com.ispw.DbmsTestHelper;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPrenotazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPrenotazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPrenotazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoPagamento;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: test di integrazione del caso d'uso Prenotazione Campo.
 * Responsabilita': verificare disponibilita', creazione prenotazione e pagamento.
 *
 * SEZIONE LOGICA
 * Usa DAO in-memory per i test di base e DBMS per lo scenario end-to-end.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerPrenotazioneCampo extends BaseDAOTest {

    // ========= costanti (no magic strings) =========
    private static final String MSG_NESSUNO_SLOT  = "Dovrebbe restituire almeno uno slot";
    private static final String MSG_LISTA_VUOTA   = "Con param=null deve tornare lista vuota";
    private static final String MSG_RIEP_NON_NULL = "Riepilogo non dovrebbe essere null";
    private static final String MSG_ID_ASS        = "Id prenotazione dovrebbe essere assegnato (>0)";
    private static final String MSG_SLOT_NO       = "Se lo slot non è disponibile, il controller deve restituire null";
    private static final String MSG_NO_PREN       = "Non deve essere creata alcuna prenotazione";
    private static final String MSG_PAY_OK        = "Il pagamento dovrebbe risultare OK";
    private static final String MSG_PAY_KO        = "Pagamento deve risultare KO";

    private LogicControllerPrenotazioneCampo controller;

    private GeneralUserDAO userDAO;
    private CampoDAO campoDAO;
    private PrenotazioneDAO prenotazioneDAO;
    private PagamentoDAO pagamentoDAO;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        controller = new LogicControllerPrenotazioneCampo();

        userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        campoDAO = DAOFactory.getInstance().getCampoDAO();
        prenotazioneDAO = DAOFactory.getInstance().getPrenotazioneDAO();
        pagamentoDAO = DAOFactory.getInstance().getPagamentoDAO();

        // pulizia store tra i test, senza dipendere dai concreti
        tryClear(userDAO);
        tryClear(campoDAO);
        tryClear(prenotazioneDAO);
        tryClear(pagamentoDAO);
    }

    // =====================================================================================
    // 1) TROVA SLOT DISPONIBILI
    // =====================================================================================
    @Test
    @DisplayName("1) Trova slot disponibili: happy path (fake disponibilità)")
    void testTrovaSlotDisponibili_HappyPath() {
        ParametriVerificaBean pv = new ParametriVerificaBean();
        pv.setIdCampo(1);
        pv.setData(LocalDate.now().plusDays(1).toString()); // domani
        pv.setOraInizio("10:00");
        pv.setDurataMin(60);

        FakeDisp ok = FakeDisp.slotAderente(pv.getData(), pv.getOraInizio(), "11:00");

        List<DatiDisponibilitaBean> out = controller.trovaSlotDisponibili(pv, ok);

        assertNotNull(out);
        assertFalse(out.isEmpty(), MSG_NESSUNO_SLOT);
        DatiDisponibilitaBean s = out.get(0);
        assertEquals(pv.getData(), s.getData());
        assertEquals(pv.getOraInizio(), s.getOraInizio());
        assertEquals("11:00", s.getOraFine());
    }

    @Test
    @DisplayName("2) Trova slot disponibili: input nullo → lista vuota")
    void testTrovaSlotDisponibili_InputNull() {
        assertTrue(controller.trovaSlotDisponibili(null, new FakeDisp()).isEmpty(), MSG_LISTA_VUOTA);
    }

    // =====================================================================================
    // 2) NUOVA PRENOTAZIONE
    // =====================================================================================
    @Test
    @DisplayName("3) Nuova prenotazione: happy path → crea DA_PAGARE, blocca slot, ritorna riepilogo")
    void testNuovaPrenotazione_HappyPath() {
        // utente e campo
        UtenteFinale u = seedUser("mario.rossi@example.org");
        Campo c = seedCampo(1, 30f); // 30 €/h

        DatiInputPrenotazioneBean in = new DatiInputPrenotazioneBean();
        in.setIdCampo(c.getIdCampo());
        String data = LocalDate.now().plusDays(1).toString();
        in.setData(data);
        in.setOraInizio("10:00");
        in.setOraFine("11:00");

        SessioneUtenteBean sessione = sessionOf(u);

        // disponibilità coerente con la richiesta
        FakeDisp ok = FakeDisp.slotAderente(data, "10:00", "11:00");

        RiepilogoPrenotazioneBean riepilogo = controller.nuovaPrenotazione(in, sessione, ok);

        // riepilogo
        assertNotNull(riepilogo, MSG_RIEP_NON_NULL);
        assertTrue(riepilogo.getIdPrenotazione() > 0, MSG_ID_ASS);
        assertNotNull(riepilogo.getUtente());
        assertEquals(u.getEmail(), riepilogo.getUtente().getEmail());
        assertEquals(30f, riepilogo.getImportoTotale(), 0.001, "Importo pro-rata 1h * 30€/h");

        // stato e dati persistiti
        Prenotazione salvata = prenotazioneDAO.load(riepilogo.getIdPrenotazione());
        assertNotNull(salvata);
        assertEquals(StatoPrenotazione.DA_PAGARE, salvata.getStato());
        assertEquals("10:00", salvata.getOraInizio().toString());
        assertEquals("11:00", salvata.getOraFine().toString());
        assertEquals(c.getIdCampo(), salvata.getIdCampo());
        assertEquals(u.getIdUtente(), salvata.getIdUtente());
    }

    @Test
    @DisplayName("4) Nuova prenotazione: slot non disponibile → null (no-op)")
    void testNuovaPrenotazione_SlotNonDisponibile() {
        UtenteFinale u = seedUser("anna@example.org");
        seedCampo(2, 25f);

        DatiInputPrenotazioneBean in = new DatiInputPrenotazioneBean();
        in.setIdCampo(2);
        String data = LocalDate.now().plusDays(2).toString();
        in.setData(data);
        in.setOraInizio("09:00");
        in.setOraFine("10:00");

        SessioneUtenteBean sessione = sessionOf(u);

        // nessuno slot aderente
        FakeDisp vuota = new FakeDisp();

        RiepilogoPrenotazioneBean riepilogo = controller.nuovaPrenotazione(in, sessione, vuota);

        assertNull(riepilogo, MSG_SLOT_NO);
        assertTrue(prenotazioneDAO.findByUtente(u.getIdUtente()).isEmpty(), MSG_NO_PREN);
    }

    // =====================================================================================
    // 3) COMPLETA PRENOTAZIONE
    // =====================================================================================
    @Test
    @DisplayName("5) Completa prenotazione: pagamento OK → CONFERMATA, fattura e notifica inviate, esito success")
    void testCompletaPrenotazione_PagamentoOK() {
        UtenteFinale u = seedUser("ok@example.org");
        Campo c = seedCampo(3, 20f);

        Prenotazione p = new Prenotazione();
        p.setIdUtente(u.getIdUtente());
        p.setIdCampo(c.getIdCampo());
        p.setData(LocalDate.now().plusDays(3));
        p.setOraInizio(java.time.LocalTime.parse("18:00"));
        p.setOraFine(java.time.LocalTime.parse("19:00"));
        p.setStato(StatoPrenotazione.DA_PAGARE);
        prenotazioneDAO.store(p);

        DatiPagamentoBean pay = new DatiPagamentoBean();
        pay.setMetodo(MetodoPagamento.PAYPAL.name());
        pay.setImporto(20f);

        FakePagamento okPay   = FakePagamento.success(true, pagamentoDAO);
        FakeFattura trackFat  = new FakeFattura();
        FakeNotifica trackNot = new FakeNotifica();

        SessioneUtenteBean sessione = sessionOf(u);

        StatoPagamentoBean esito = controller.completaPrenotazione(pay, sessione, okPay, trackFat, trackNot);

        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), MSG_PAY_OK);
        assertNotNull(esito.getStato());

        Prenotazione after = prenotazioneDAO.load(p.getIdPrenotazione());
        assertEquals(StatoPrenotazione.CONFERMATA, after.getStato());

        assertEquals(1, trackFat.invocations);
        assertEquals(p.getIdPrenotazione(), trackFat.lastIdPren);
        assertEquals(1, trackNot.invConferma);
        assertNotNull(trackNot.lastUtente);
        assertEquals(u.getEmail(), trackNot.lastUtente.getEmail());

        Pagamento saved = pagamentoDAO.findByPrenotazione(p.getIdPrenotazione());
        assertNotNull(saved);
        assertEquals(StatoPagamento.OK, saved.getStato());
    }

    @Test
    @DisplayName("6) Completa prenotazione: pagamento KO → resta DA_PAGARE, nessuna fattura/notifica")
    void testCompletaPrenotazione_PagamentoKO() {
        UtenteFinale u = seedUser("ko@example.org");
        Campo c = seedCampo(4, 22f);

        Prenotazione p = new Prenotazione();
        p.setIdUtente(u.getIdUtente());
        p.setIdCampo(c.getIdCampo());
        p.setData(LocalDate.now().plusDays(1));
        p.setOraInizio(java.time.LocalTime.parse("09:00"));
        p.setOraFine(java.time.LocalTime.parse("10:00"));
        p.setStato(StatoPrenotazione.DA_PAGARE);
        prenotazioneDAO.store(p);

        DatiPagamentoBean pay = new DatiPagamentoBean();
        pay.setMetodo(MetodoPagamento.SATISPAY.name());
        pay.setImporto(0f);

        FakePagamento koPay   = FakePagamento.success(false, pagamentoDAO);
        FakeFattura trackFat  = new FakeFattura();
        FakeNotifica trackNot = new FakeNotifica();

        StatoPagamentoBean esito = controller.completaPrenotazione(pay, sessionOf(u), koPay, trackFat, trackNot);

        assertNotNull(esito);
        assertFalse(esito.isSuccesso(), MSG_PAY_KO);

        Prenotazione after = prenotazioneDAO.load(p.getIdPrenotazione());
        assertEquals(StatoPrenotazione.DA_PAGARE, after.getStato());

        assertEquals(0, trackFat.invocations);
        assertEquals(0, trackNot.invConferma);
    }

    @Test
    @DisplayName("7) Completa prenotazione: pagamento random → stato coerente con esito")
    void testCompletaPrenotazione_PagamentoRandom() {
        UtenteFinale u = seedUser("rnd@example.org");
        Campo c = seedCampo(5, 25f);

        Prenotazione p = new Prenotazione();
        p.setIdUtente(u.getIdUtente());
        p.setIdCampo(c.getIdCampo());
        p.setData(LocalDate.now().plusDays(2));
        p.setOraInizio(java.time.LocalTime.parse("12:00"));
        p.setOraFine(java.time.LocalTime.parse("13:00"));
        p.setStato(StatoPrenotazione.DA_PAGARE);
        prenotazioneDAO.store(p);

        DatiPagamentoBean pay = new DatiPagamentoBean();
        pay.setMetodo(MetodoPagamento.PAYPAL.name());
        pay.setImporto(25f);

        FakePagamentoRandom rndPay = new FakePagamentoRandom(new Random(42), pagamentoDAO);
        FakeFattura trackFat  = new FakeFattura();
        FakeNotifica trackNot = new FakeNotifica();

        StatoPagamentoBean esito = controller.completaPrenotazione(pay, sessionOf(u), rndPay, trackFat, trackNot);

        assertNotNull(esito);

        Prenotazione after = prenotazioneDAO.load(p.getIdPrenotazione());
        if (rndPay.lastSuccess) {
            assertTrue(esito.isSuccesso(), MSG_PAY_OK);
            assertEquals(StatoPrenotazione.CONFERMATA, after.getStato());
            assertEquals(1, trackFat.invocations);
            assertEquals(1, trackNot.invConferma);
        } else {
            assertFalse(esito.isSuccesso(), MSG_PAY_KO);
            assertEquals(StatoPrenotazione.DA_PAGARE, after.getStato());
            assertEquals(0, trackFat.invocations);
            assertEquals(0, trackNot.invConferma);
        }
    }
    
@Test
@DisplayName("8) Flusso completo: slot disponibile → prenotazione, pagamento OK, conferma")
void testFlussoCompleto_SlotDisponibile_PagamentoOK() {
    // Arrange: utente e campo
    UtenteFinale u = seedUser("flow@example.org");
    
    
    Campo c = seedCampo(10, 40f); // 40 €/h

    // Slot disponibile domani 15:00-16:00
    String data = LocalDate.now().plusDays(1).toString();
    String oraInizio = "15:00";
    String oraFine   = "16:00";

    // 1) La view chiede gli slot → il secondario risponde con uno slot aderente
    ParametriVerificaBean pv = new ParametriVerificaBean();
    pv.setIdCampo(c.getIdCampo());
    pv.setData(data);
    pv.setOraInizio(oraInizio);
    pv.setDurataMin(60);

    FakeDisp disp = FakeDisp.slotAderente(data, oraInizio, oraFine);
    List<DatiDisponibilitaBean> slots = controller.trovaSlotDisponibili(pv, disp);
    assertFalse(slots.isEmpty(), "Slot atteso");

    // 2) L’utente procede: crea prenotazione DA_PAGARE
    DatiInputPrenotazioneBean in = new DatiInputPrenotazioneBean();
    in.setIdCampo(c.getIdCampo());
    in.setData(data);
    in.setOraInizio(oraInizio);
    in.setOraFine(oraFine);

    SessioneUtenteBean sessione = sessionOf(u);
    RiepilogoPrenotazioneBean riepilogo = controller.nuovaPrenotazione(in, sessione, disp);

    assertNotNull(riepilogo, "Riepilogo atteso");
    assertTrue(riepilogo.getIdPrenotazione() > 0, "ID prenotazione assegnato");
    assertEquals(40f, riepilogo.getImportoTotale(), 0.001, "Pro-rata 1h * 40€/h");

    Prenotazione p = prenotazioneDAO.load(riepilogo.getIdPrenotazione());
    assertNotNull(p, "Prenotazione salvata");
    assertEquals(StatoPrenotazione.DA_PAGARE, p.getStato());

    // 3) Completa: pagamento OK → CONFERMATA + fattura + notifica
    DatiPagamentoBean pay = new DatiPagamentoBean();
    pay.setMetodo(MetodoPagamento.PAYPAL.name());
    pay.setImporto(40f);

    FakePagamento okPay   = FakePagamento.success(true, pagamentoDAO);
    FakeFattura trackFat  = new FakeFattura();
    FakeNotifica trackNot = new FakeNotifica();

    StatoPagamentoBean esito = controller.completaPrenotazione(pay, sessione, okPay, trackFat, trackNot);

    // Assert esito e transizioni
    assertNotNull(esito, "Esito non nullo");
    assertTrue(esito.isSuccesso(), "Pagamento OK");
    assertNotNull(esito.getStato(), "Stato pagamento valorizzato");

    Prenotazione after = prenotazioneDAO.load(p.getIdPrenotazione());
    assertEquals(StatoPrenotazione.CONFERMATA, after.getStato(), "Stato aggiornato a CONFERMATA");

    // Fattura e notifica inviate esattamente una volta
    assertEquals(1, trackFat.invocations, "Fattura generata");
    assertEquals(p.getIdPrenotazione(), trackFat.lastIdPren, "ID prenotazione fattura");
    assertEquals(1, trackNot.invConferma, "Notifica di conferma inviata");
    assertNotNull(trackNot.lastUtente);
    assertEquals(u.getEmail(), trackNot.lastUtente.getEmail());

    // Pagamento persistito dal fake su PagamentoDAO
    Pagamento saved = pagamentoDAO.findByPrenotazione(p.getIdPrenotazione());
    assertNotNull(saved, "Pagamento salvato");
    assertEquals(StatoPagamento.OK, saved.getStato(), "Stato pagamento OK");
}

    @Test
    @DisplayName("DBMS) Completa prenotazione: pagamento persistito")
    void testCompletaPrenotazioneDbms() throws Exception {
        DbmsTestHelper.runWithDbms(
            TestControllerPrenotazioneCampo::createTablesIfMissingDbms,
            () -> {
                GeneralUserDAO dbUserDAO = DAOFactory.getInstance().getGeneralUserDAO();
                CampoDAO dbCampoDAO = DAOFactory.getInstance().getCampoDAO();
                PrenotazioneDAO dbPrenotazioneDAO = DAOFactory.getInstance().getPrenotazioneDAO();
                PagamentoDAO dbPagamentoDAO = DAOFactory.getInstance().getPagamentoDAO();

                UtenteFinale u = seedDbUser(dbUserDAO);
                Campo c = seedDbCampo(dbCampoDAO, 7001);

                Prenotazione p = new Prenotazione();
                p.setIdUtente(u.getIdUtente());
                p.setIdCampo(c.getIdCampo());
                p.setData(LocalDate.now().plusDays(2));
                p.setOraInizio(java.time.LocalTime.parse("10:00"));
                p.setOraFine(java.time.LocalTime.parse("11:00"));
                p.setStato(StatoPrenotazione.DA_PAGARE);
                dbPrenotazioneDAO.store(p);

                Prenotazione persisted = dbPrenotazioneDAO.findByUtente(u.getIdUtente()).stream()
                    .findFirst()
                    .orElse(null);
                assertNotNull(persisted, "Prenotazione deve essere persistita");

                DatiPagamentoBean pay = new DatiPagamentoBean();
                pay.setMetodo(MetodoPagamento.PAYPAL.name());
                pay.setImporto(20f);

                FakeFattura trackFat = new FakeFattura();
                FakeNotifica trackNot = new FakeNotifica();

                LogicControllerPrenotazioneCampo dbController = new LogicControllerPrenotazioneCampo();
                StatoPagamentoBean esito = dbController.completaPrenotazione(
                    pay, sessionOf(u), new LogicControllerGestionePagamento(), trackFat, trackNot);

                assertNotNull(esito);

                Pagamento saved = dbPagamentoDAO.findByPrenotazione(persisted.getIdPrenotazione());
                assertNotNull(saved, "Pagamento deve essere persistito nel DB");
            }
        );
    }


    // =====================================================================================
    // Helpers (seed, sessione, clear) + Fakes
    // =====================================================================================

    private UtenteFinale seedUser(String email) {
        UtenteFinale u = new UtenteFinale();
        u.setNome("Test");
        u.setEmail(email);
        u.setPassword("pwd");
        u.setRuolo(Ruolo.UTENTE);
        u.setStatoAccount(com.ispw.model.enums.StatoAccount.ATTIVO);
        userDAO.store(u);
        return u;
    }

    private Campo seedCampo(int idCampo, float costoOrario) {
        Campo c = new Campo();
        c.setIdCampo(idCampo);
        c.setCostoOrario(costoOrario);
        campoDAO.store(c);
        return c;
    }

    private SessioneUtenteBean sessionOf(GeneralUser u) {
        SessioneUtenteBean s = new SessioneUtenteBean();
        s.setUtente(new UtenteBean(u.getNome(), null, u.getEmail(), u.getRuolo()));
        s.setIdSessione(java.util.UUID.randomUUID().toString());
        return s;
    }

    private static UtenteFinale seedDbUser(GeneralUserDAO dao) {
        UtenteFinale u = new UtenteFinale();
        u.setNome("DB");
        u.setEmail("prenotazione.db+" + UUID.randomUUID() + "@example.org");
        u.setPassword("pwd");
        u.setRuolo(Ruolo.UTENTE);
        u.setStatoAccount(com.ispw.model.enums.StatoAccount.ATTIVO);
        dao.store(u);
        return u;
    }

    private static Campo seedDbCampo(CampoDAO dao, int idCampo) {
        Campo c = new Campo();
        c.setIdCampo(idCampo);
        c.setCostoOrario(20f);
        c.setAttivo(true);
        c.setFlagManutenzione(false);
        dao.store(c);
        return c;
    }

    private static void createTablesIfMissingDbms() throws Exception {
        DbmsTestHelper.withStatement(st -> {
            st.execute("""
                CREATE TABLE IF NOT EXISTS general_user (
                  id_utente INT AUTO_INCREMENT PRIMARY KEY,
                  nome VARCHAR(255),
                  email VARCHAR(255),
                  password VARCHAR(255),
                  stato_account VARCHAR(40),
                  ruolo VARCHAR(40)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS campi (
                  id_campo INT PRIMARY KEY,
                  nome VARCHAR(255),
                  tipo_sport VARCHAR(255),
                  costo_orario FLOAT,
                  is_attivo BOOLEAN,
                  flag_manutenzione BOOLEAN
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS prenotazioni (
                  id_prenotazione INT AUTO_INCREMENT PRIMARY KEY,
                  id_utente INT NOT NULL,
                  id_campo INT NOT NULL,
                  data DATE,
                  ora_inizio TIME,
                  ora_fine TIME,
                  stato VARCHAR(40),
                  notifica_richiesta BOOLEAN
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS pagamenti (
                  id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
                  id_prenotazione INT NOT NULL,
                  importo_finale DECIMAL(10,2),
                  metodo VARCHAR(40),
                  stato VARCHAR(40),
                  data_pagamento TIMESTAMP NULL
                )
            """);
        });
    }

    /** Invoca clear() se esiste (evita dipendenze dai concreti In-Memory). */
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

    /** Fake disponibilità: default lista vuota; factory per uno slot aderente. */
    private static final class FakeDisp implements GestioneDisponibilitaPrenotazione {
        private final List<DatiDisponibilitaBean> slots = new ArrayList<>();

        static FakeDisp slotAderente(String data, String oraInizio, String oraFine) {
            FakeDisp f = new FakeDisp();
            DatiDisponibilitaBean b = new DatiDisponibilitaBean();
            b.setData(data);
            b.setOraInizio(oraInizio);
            b.setOraFine(oraFine);
            f.slots.add(b);
            return f;
        }

        @Override
        public List<DatiDisponibilitaBean> verificaDisponibilita(ParametriVerificaBean param) {
            return new ArrayList<>(slots);
        }
    }

    /**
     * Fake Pagamento:
     * - OK/FALLITO su enum.
     * - Salva il pagamento sul DAO per simulare il secondario reale.
     */
    private static final class FakePagamento implements GestionePagamentoPrenotazione {
        private final boolean success;
        private final PagamentoDAO pagamentoDAO;

        private FakePagamento(boolean success, PagamentoDAO pagamentoDAO) {
            this.success = success;
            this.pagamentoDAO = pagamentoDAO;
        }

        static FakePagamento success(boolean success, PagamentoDAO pagamentoDAO) {
            return new FakePagamento(success, pagamentoDAO);
        }

        @Override
        public StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione) {
            StatoPagamento stato = success ? StatoPagamento.OK : StatoPagamento.FALLITO;

            Pagamento p = pagamentoDAO.findByPrenotazione(idPrenotazione);
            if (p == null) p = new Pagamento();
            p.setIdPrenotazione(idPrenotazione);
            p.setMetodo(dati.getMetodo() != null
                    ? com.ispw.model.enums.MetodoPagamento.valueOf(dati.getMetodo())
                    : MetodoPagamento.PAYPAL);
            p.setImportoFinale(java.math.BigDecimal.valueOf(Math.max(0f, dati.getImporto())));
            p.setDataPagamento(java.time.LocalDateTime.now());
            p.setStato(stato);
            pagamentoDAO.store(p);

            return stato;
        }
    }

    /**
     * Fake Pagamento randomico:
     * - esito random (seed fissato nel test per ripetibilità)
     * - salva il pagamento sul DAO
     */
    private static final class FakePagamentoRandom implements GestionePagamentoPrenotazione {
        private final Random random;
        private final PagamentoDAO pagamentoDAO;
        boolean lastSuccess;

        private FakePagamentoRandom(Random random, PagamentoDAO pagamentoDAO) {
            this.random = random;
            this.pagamentoDAO = pagamentoDAO;
        }

        @Override
        public StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione) {
            lastSuccess = random.nextBoolean();
            StatoPagamento stato = lastSuccess ? StatoPagamento.OK : StatoPagamento.FALLITO;

            Pagamento p = pagamentoDAO.findByPrenotazione(idPrenotazione);
            if (p == null) p = new Pagamento();
            p.setIdPrenotazione(idPrenotazione);
            p.setMetodo(dati.getMetodo() != null
                    ? com.ispw.model.enums.MetodoPagamento.valueOf(dati.getMetodo())
                    : MetodoPagamento.PAYPAL);
            p.setImportoFinale(java.math.BigDecimal.valueOf(Math.max(0f, dati.getImporto())));
            p.setDataPagamento(java.time.LocalDateTime.now());
            p.setStato(stato);
            pagamentoDAO.store(p);

            return stato;
        }
    }

    /** Fake Fattura: solo tracing delle invocazioni. */
    private static final class FakeFattura implements GestioneFatturaPrenotazione {
        int invocations;
        int lastIdPren;

        @Override
        public com.ispw.model.entity.Fattura generaFatturaPrenotazione(DatiFatturaBean dati, int idPrenotazione) {
            invocations++;
            lastIdPren = idPrenotazione;
            return null;
        }
    }

    /** Fake Notifica: traccia l'invio della conferma prenotazione. */
    private static final class FakeNotifica implements GestioneNotificaPrenotazione {
        int invConferma;
        UtenteBean lastUtente;

        @Override
        public void inviaConfermaPrenotazione(UtenteBean utente, String dettaglio) {
            invConferma++;
            lastUtente = utente;
        }

        @Override
        public void impostaPromemoria(int idPrenotazione, int minutiAnticipo) {
            // no-op
        }
    }
}

