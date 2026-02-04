package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.StatoPagamento;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerGestionePagamento extends BaseDAOTest {

    private LogicControllerGestionePagamento controller;
    private PagamentoDAO pagamentoDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerGestionePagamento();

        pagamentoDAO = DAOFactory.getInstance().getPagamentoDAO();
        assertNotNull(pagamentoDAO, "DAOFactory.getPagamentoDAO() ha restituito null");

        tryClear(pagamentoDAO);
    }

    // =====================================================================================
    // 1) PAGAMENTO PRENOTAZIONE
    // =====================================================================================
    @Test
    @DisplayName("1) Prenotazione: pagamento OK → persistito con metodo/importo; stato coerente con enum ritorno")
    void testRichiediPagamentoPrenotazione_Ok() {
        DatiPagamentoBean dati = new DatiPagamentoBean();
        dati.setMetodo("paypal");                // test anche la normalizzazione case-insensitive
        dati.setImporto(25f);

        StatoPagamento esito = controller.richiediPagamentoPrenotazione(dati, 1001);
        assertNotNull(esito, "Enum stato non nullo");

        Pagamento saved = pagamentoDAO.findByPrenotazione(1001);
        assertNotNull(saved, "Pagamento deve essere salvato");
        assertEquals(MetodoPagamento.PAYPAL, saved.getMetodo());
        assertEquals(0, BigDecimal.valueOf(25f).compareTo(saved.getImportoFinale()));
        // lo stato persistito deve essere esattamente quello ritornato dal controller
        assertEquals(esito, saved.getStato());
        assertNotNull(saved.getDataPagamento(), "Data pagamento valorizzata");
    }

    @Test
    @DisplayName("2) Prenotazione: pagamento KO (importo=0) → persistito con importo=0; stato coerente con enum ritorno")
    void testRichiediPagamentoPrenotazione_Ko() {
        DatiPagamentoBean dati = new DatiPagamentoBean();
        dati.setMetodo(MetodoPagamento.SATISPAY.name());
        dati.setImporto(0f);

        StatoPagamento esito = controller.richiediPagamentoPrenotazione(dati, 1002);
        assertNotNull(esito);

        Pagamento saved = pagamentoDAO.findByPrenotazione(1002);
        assertNotNull(saved);
        assertEquals(MetodoPagamento.SATISPAY, saved.getMetodo());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getImportoFinale()));
        assertEquals(esito, saved.getStato());
    }

    @Test
@DisplayName("3) Prenotazione: esiste già un pagamento → update del record")
void testRichiediPagamentoPrenotazione_UpdateEsistente() {
    // seed pagamento esistente
    Pagamento p = new Pagamento();
    p.setIdPrenotazione(1003);
    p.setImportoFinale(BigDecimal.valueOf(10));
    p.setMetodo(MetodoPagamento.BONIFICO);
    p.setStato(StatoPagamento.OK);
    p.setDataPagamento(LocalDateTime.now().minusMinutes(10));
    pagamentoDAO.store(p);

    // snapshot del timestamp PRIMA dell’update
    LocalDateTime tsBefore = p.getDataPagamento();

    // nuova richiesta di pagamento (diverso importo/metodo)
    DatiPagamentoBean dati = new DatiPagamentoBean();
    dati.setMetodo(MetodoPagamento.PAYPAL.name());
    dati.setImporto(12f);


    StatoPagamento esito = controller.richiediPagamentoPrenotazione(dati, 1003);
    assertNotNull(esito);

    Pagamento after = pagamentoDAO.findByPrenotazione(1003);
    assertNotNull(after);
    assertEquals(MetodoPagamento.PAYPAL, after.getMetodo(), "Metodo aggiornato");
    assertEquals(0, BigDecimal.valueOf(12f).compareTo(after.getImportoFinale()), "Importo aggiornato");

    // verifica su snapshot (NON su p.getDataPagamento() post-mutazione)
    assertTrue(after.getDataPagamento().isAfter(tsBefore), "Timestamp aggiornato");
}


    // =====================================================================================
    // 2) RIMBORSO
    // =====================================================================================
    @Test
@DisplayName("4) Rimborso: pagamento presente → stato aggiornato e dataPagamento aggiornata")
void testEseguiRimborso_Presente() {
    // seed pagamento
    Pagamento p = new Pagamento();
    p.setIdPrenotazione(2001);
    p.setImportoFinale(BigDecimal.valueOf(40));
    p.setMetodo(MetodoPagamento.PAYPAL);
    p.setStato(StatoPagamento.OK);
    p.setDataPagamento(LocalDateTime.now().minusMinutes(5));
    pagamentoDAO.store(p);

    // snapshot PRIMA
    LocalDateTime tsBefore = p.getDataPagamento();

    controller.eseguiRimborso(2001, 30f);

    Pagamento after = pagamentoDAO.findByPrenotazione(2001);
    assertNotNull(after);
    assertNotNull(after.getStato(), "Stato aggiornato (es. RIMBORSATO se presente nell'enum)");

    // verifica su snapshot (NON su p post-aggiornamento)
    assertTrue(after.getDataPagamento().isAfter(tsBefore), "Data pagamento aggiornata");
}

    @Test
    @DisplayName("5) Rimborso: pagamento assente → nessuna eccezione, nessun record creato")
    void testEseguiRimborso_Assente() {
        controller.eseguiRimborso(2002, 10f);
        assertNull(pagamentoDAO.findByPrenotazione(2002), "Nessun pagamento deve esistere");
    }

    // =====================================================================================
    // 3) PENALITÀ
    // =====================================================================================
    @Test
    @DisplayName("6) Penalità: pagamento OK → success=true e record con idPrenotazione negativo")
    void testRichiediPagamentoPenalita_Ok() {
        DatiPagamentoBean dati = new DatiPagamentoBean();
        dati.setMetodo(MetodoPagamento.PAYPAL.name());
        dati.setImporto(12f);

        StatoPagamentoBean out = controller.richiediPagamentoPenalita(dati, 77);
        assertNotNull(out);
        assertTrue(out.isSuccesso());
        assertNotNull(out.getStato());
        assertNotNull(out.getIdTransazione());

        Pagamento saved = pagamentoDAO.findByPrenotazione(-77);
        assertNotNull(saved);
        assertEquals(MetodoPagamento.PAYPAL, saved.getMetodo());
        assertEquals(0, BigDecimal.valueOf(12f).compareTo(saved.getImportoFinale()));
        assertEquals(out.getStato(), saved.getStato().name(), "Stato persistito coerente con DTO");
    }

    @Test
    @DisplayName("7) Penalità: pagamento KO (importo=0) → success=false e record con importo=0")
    void testRichiediPagamentoPenalita_Ko() {
        DatiPagamentoBean dati = new DatiPagamentoBean();
        dati.setMetodo(MetodoPagamento.SATISPAY.name());
        dati.setImporto(0f);

        StatoPagamentoBean out = controller.richiediPagamentoPenalita(dati, 88);
        assertNotNull(out);
        assertFalse(out.isSuccesso());

        Pagamento saved = pagamentoDAO.findByPrenotazione(-88);
        assertNotNull(saved);
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getImportoFinale()));
        assertEquals(out.getStato(), saved.getStato().name());
    }

    // =====================================================================================
    // Helpers
    // =====================================================================================
    private static void tryClear(Object dao) {
        if (dao == null) return;
        try {
            Method m = dao.getClass().getMethod("clear");
            m.setAccessible(true);
            m.invoke(dao);
        } catch (ReflectiveOperationException ignored) { /* ignored: clear may not exist on all DAOs */ }
    }
}