package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaDisdetta;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaRimborso;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaDisdetta;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoRimborso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoPrenotazione;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerDisdettaPrenotazione extends BaseDAOTest {

    private LogicControllerDisdettaPrenotazione controller;

    private PrenotazioneDAO prenotazioneDAO;
    private PagamentoDAO pagamentoDAO;
    private GeneralUserDAO userDAO;
    private LogDAO logDAO;
    private RegoleTempisticheDAO tempDAO;
    private RegolePenalitaDAO penDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerDisdettaPrenotazione();

        final var f = DAOFactory.getInstance();
        prenotazioneDAO = f.getPrenotazioneDAO();
        pagamentoDAO    = f.getPagamentoDAO();
        userDAO         = f.getGeneralUserDAO();
        logDAO          = f.getLogDAO();
        tempDAO         = f.getRegoleTempisticheDAO();
        penDAO          = f.getRegolePenalitaDAO();

        // Fail-fast se la factory non è cablata
        assertAll(
            () -> assertNotNull(prenotazioneDAO, "DAOFactory.getPrenotazioneDAO() ha restituito null"),
            () -> assertNotNull(pagamentoDAO,    "DAOFactory.getPagamentoDAO() ha restituito null"),
            () -> assertNotNull(userDAO,         "DAOFactory.getGeneralUserDAO() ha restituito null"),
            () -> assertNotNull(logDAO,          "DAOFactory.getLogDAO() ha restituito null"),
            () -> assertNotNull(tempDAO,         "DAOFactory.getRegoleTempisticheDAO() ha restituito null"),
            () -> assertNotNull(penDAO,          "DAOFactory.getRegolePenalitaDAO() ha restituito null")
        );

        // Pulizia store tra i test (se i concreti espongono clear())
        tryClear(prenotazioneDAO);
        tryClear(pagamentoDAO);
        tryClear(userDAO);
        tryClear(logDAO);

        // Regole predefinite (overridable nei singoli test)
        penDAO.save(new RegolePenalita(BigDecimal.valueOf(10), 120)); // penale=10€, preavviso=120'
        tempDAO.save(new RegoleTempistiche(60, LocalTime.of(8,0), LocalTime.of(23,0), 120));
    }

    @Test
    @DisplayName("1) Anteprima: preavviso sufficiente → disdetta possibile, penale=0")
    void testAnteprimaDisdetta_SenzaPenale() {
        UtenteFinale u = seedUser("anteprima.ok@example.org");
        Prenotazione p = seedPrenotazione(u.getIdUtente(), LocalDate.now().plusDays(2), "18:00", "19:00", StatoPrenotazione.CONFERMATA);

        SessioneUtenteBean s = sessionOf(u);
        EsitoDisdettaBean preview = controller.anteprimaDisdetta(p.getIdPrenotazione(), s);

        assertNotNull(preview);
        assertTrue(preview.isPossibile());
        assertEquals(0f, preview.getPenale(), 0.001f);
    }

    @Test
    @DisplayName("2) Annullamento: CONFERMATA con penale → ANNULLATA, rimborso=pagato-penale, NC emessa, slot liberato, notifica inviata")
    void testEseguiAnnullamento_ConfermataConPenale() {
        UtenteFinale u = seedUser("disdetta.ok@example.org");
        Prenotazione p = seedPrenotazione(u.getIdUtente(), LocalDate.now().plusDays(1), "10:00", "11:00", StatoPrenotazione.CONFERMATA);

        // pagamento effettivo = 40€
        seedPagamento(p.getIdPrenotazione(), BigDecimal.valueOf(40));

        // Forzo penale applicata: preavviso richiesto 2000' (> 24h) così scatta la penale di 10€
        tempDAO.save(new RegoleTempistiche(60, LocalTime.of(8,0), LocalTime.of(23,0), 2000));
        penDAO.save(new RegolePenalita(BigDecimal.valueOf(10), 2000));

        FakeRimborso pay = new FakeRimborso();
        FakeNotaCredito fatt = new FakeNotaCredito();
        FakeNotifica noti = new FakeNotifica();
        FakeDisp disp = new FakeDisp();

        EsitoOperazioneBean esito = controller.eseguiAnnullamento(
                p.getIdPrenotazione(), sessionOf(u), pay, fatt, noti, disp);

        assertNotNull(esito);
        assertTrue(esito.isSuccesso(), "Operazione attesa OK");

        Prenotazione after = prenotazioneDAO.load(p.getIdPrenotazione());
        assertEquals(StatoPrenotazione.ANNULLATA, after.getStato(), "Stato aggiornato ad ANNULLATA");

        // rimborso = 40 - 10 = 30
        assertEquals(1, pay.calls);
        assertEquals(p.getIdPrenotazione(), pay.lastIdPren);
        assertEquals(30f, pay.lastImporto, 0.001f);

        assertEquals(1, fatt.calls);
        assertEquals(p.getIdPrenotazione(), fatt.lastIdPren);

        assertEquals(1, noti.calls);
        assertNotNull(noti.lastUtente);
        assertTrue(noti.lastDettaglio != null && !noti.lastDettaglio.isBlank(), "Dettaglio notifica valorizzato");

        assertEquals(1, disp.calls);
        assertEquals(p.getIdPrenotazione(), disp.lastIdPren);
    }

    @Test
    @DisplayName("3) Annullamento: DA_PAGARE → ANNULLATA, nessun rimborso/NC, slot liberato")
    void testEseguiAnnullamento_DaPagare_NoRimborso() {
        UtenteFinale u = seedUser("disdetta.nopay@example.org");
        Prenotazione p = seedPrenotazione(u.getIdUtente(), LocalDate.now().plusDays(1), "09:00", "10:00", StatoPrenotazione.DA_PAGARE);

        FakeRimborso pay = new FakeRimborso();
        FakeNotaCredito fatt = new FakeNotaCredito();
        FakeNotifica noti = new FakeNotifica();
        FakeDisp disp = new FakeDisp();

        EsitoOperazioneBean esito = controller.eseguiAnnullamento(
                p.getIdPrenotazione(), sessionOf(u), pay, fatt, noti, disp);

        assertNotNull(esito);
        assertTrue(esito.isSuccesso());

        Prenotazione after = prenotazioneDAO.load(p.getIdPrenotazione());
        assertEquals(StatoPrenotazione.ANNULLATA, after.getStato());

        assertEquals(0, pay.calls, "Nessun rimborso");
        assertEquals(0, fatt.calls, "Nessuna nota di credito");

        assertEquals(1, disp.calls);
        assertEquals(1, noti.calls);
    }

    // =========================
    // Fakes secondari
    // =========================
    private static final class FakeRimborso implements GestionePagamentoRimborso {
        int calls; int lastIdPren; float lastImporto;
        @Override public void eseguiRimborso(int idPrenotazione, float importo) {
            calls++; lastIdPren = idPrenotazione; lastImporto = importo;
        }
    }
    private static final class FakeNotaCredito implements GestioneFatturaRimborso {
        int calls; int lastIdPren;
        @Override public void emettiNotaDiCredito(int idPrenotazione) { calls++; lastIdPren = idPrenotazione; }
    }
    private static final class FakeNotifica implements GestioneNotificaDisdetta {
        int calls; UtenteBean lastUtente; String lastDettaglio;
        @Override
        public void inviaConfermaCancellazione(UtenteBean utente, String dettaglio) {
            calls++; lastUtente = utente; lastDettaglio = dettaglio;
        }
    }
    private static final class FakeDisp implements GestioneDisponibilitaDisdetta {
        int calls; int lastIdPren;
        @Override public void liberaSlot(int idPrenotazione) { calls++; lastIdPren = idPrenotazione; }
    }

    // =========================
    // Helpers seed/clear
    // =========================
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

    private Prenotazione seedPrenotazione(int idUtente, LocalDate data, String inizio, String fine, StatoPrenotazione stato) {
        Prenotazione p = new Prenotazione();
        p.setIdUtente(idUtente);
        p.setIdCampo(1);
        p.setData(data);
        p.setOraInizio(LocalTime.parse(inizio));
        p.setOraFine(LocalTime.parse(fine));
        p.setStato(stato);
        prenotazioneDAO.store(p);
        return p;
    }

    private void seedPagamento(int idPrenotazione, BigDecimal importo) {
        Pagamento p = pagamentoDAO.findByPrenotazione(idPrenotazione);
        if (p == null) p = new Pagamento();
        p.setIdPrenotazione(idPrenotazione);
        p.setImportoFinale(importo);
        pagamentoDAO.store(p);
    }

    private SessioneUtenteBean sessionOf(GeneralUser u) {
        SessioneUtenteBean s = new SessioneUtenteBean();
        s.setUtente(new UtenteBean(u.getNome(), null, u.getEmail(), u.getRuolo()));
        s.setIdSessione(java.util.UUID.randomUUID().toString());
        return s;
    }

    private static void tryClear(Object dao) {
        if (dao == null) return;
        try {
            Method m = dao.getClass().getMethod("clear");
            m.setAccessible(true);
            m.invoke(dao);
        } catch (ReflectiveOperationException ignored) { /* ignored: clear may not exist on all DAOs */ }
    }
}