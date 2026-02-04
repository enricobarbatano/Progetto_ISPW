package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Prenotazione;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerGestoreDisponibilita extends BaseDAOTest {

    private LogicControllerGestoreDisponibilita controller;
    private CampoDAO campoDAO;
    private PrenotazioneDAO prenotazioneDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerGestoreDisponibilita();
        campoDAO = DAOFactory.getInstance().getCampoDAO();
        prenotazioneDAO = DAOFactory.getInstance().getPrenotazioneDAO();

        assertNotNull(campoDAO, "DAOFactory.getCampoDAO() ha restituito null");
        assertNotNull(prenotazioneDAO, "DAOFactory.getPrenotazioneDAO() ha restituito null");

        tryClear(campoDAO);
        tryClear(prenotazioneDAO);
    }

    // =====================================================================================
    // 1) liberaSlot: sblocca lo slot della prenotazione
    // =====================================================================================
    @Test
    @DisplayName("1) Libera slot: sblocca lo slot della prenotazione indicata")
    void testLiberaSlot() {
        Campo c = seedCampo(1, 30f, true, false);
        LocalDate d = LocalDate.now().plusDays(1);
        LocalTime i = LocalTime.parse("09:00");
        LocalTime f = LocalTime.parse("10:00");

        // Blocco slot e salvo il campo
        c.bloccoSlot(Date.valueOf(d), Time.valueOf(i), Time.valueOf(f));
        campoDAO.store(c);
        assertFalse(c.isDisponibile(Date.valueOf(d), Time.valueOf(i), Time.valueOf(f)), "Slot deve risultare bloccato (pre-condizione)");

        // Creo prenotazione collegata allo slot
        Prenotazione p = new Prenotazione();
        p.setIdCampo(c.getIdCampo());
        p.setIdUtente(1);
        p.setData(d);
        p.setOraInizio(i);
        p.setOraFine(f);
        prenotazioneDAO.store(p);

        // Eseguo
        controller.liberaSlot(p.getIdPrenotazione());

        // Verifica
        Campo after = campoDAO.findById(1);
        assertNotNull(after);
        assertTrue(after.isDisponibile(Date.valueOf(d), Time.valueOf(i), Time.valueOf(f)), "Slot deve risultare sbloccato (post-condizione)");
    }

    // =====================================================================================
    // 2) rimuoviDisponibilità: disattiva il campo
    // =====================================================================================
    @Test
    @DisplayName("2) Rimuovi disponibilità: disattiva il campo")
    void testRimuoviDisponibilita() {
        Campo c = seedCampo(2, 25f, true, false);
        assertTrue(controller.rimuoviDisponibilita(c.getIdCampo()), "Operazione deve tornare TRUE");
        Campo after = campoDAO.findById(2);
        assertNotNull(after);
        assertFalse(after.isAttivo(), "Campo deve risultare disattivo");
    }

    // =====================================================================================
    // 3) attivaDisponibilità: attiva il campo (ritorna lista vuota)
    // =====================================================================================
    @Test
    @DisplayName("3) Attiva disponibilità: attiva il campo e ritorna lista vuota")
    void testAttivaDisponibilita() {
        Campo c = seedCampo(3, 25f, false, false);
        List<DatiDisponibilitaBean> out = controller.attivaDisponibilita(c.getIdCampo());
        assertNotNull(out);
        assertTrue(out.isEmpty(), "La firma prevede lista vuota come risultato");
        Campo after = campoDAO.findById(3);
        assertNotNull(after);
        assertTrue(after.isAttivo(), "Campo deve risultare attivo");
    }

    // =====================================================================================
    // 4) verificaDisponibilita: slot coerenti (attivo, non in manutenzione)
    // =====================================================================================
    @Test
    @DisplayName("4) Verifica disponibilità: ritorna slot solo per campi attivi e non in manutenzione")
    void testVerificaDisponibilita() {
         //Campo #4: ATTIVO, NON in manutenzione → disponibile
        seedCampo(4, 20f, true, false);
        // Campo #5: attivo ma in manutenzione → NON deve comparire
        seedCampo(5, 50f, true, true);

        LocalDate d = LocalDate.now().plusDays(1);
        LocalTime i = LocalTime.parse("10:00");

        ParametriVerificaBean pv = new ParametriVerificaBean();
        pv.setIdCampo(4);                  // con l'implementazione attuale, id>=0 → findAll()
        pv.setData(d.toString());
        pv.setOraInizio(i.toString());
        pv.setDurataMin(60);               // l'implementazione forza comunque 60 minuti

        List<DatiDisponibilitaBean> out = controller.verificaDisponibilita(pv);
        assertNotNull(out);
        assertFalse(out.isEmpty(), "Deve tornare almeno uno slot");
        // ci aspettiamo solo lo slot del campo #4
        assertEquals(1, out.size(), "Solo il campo attivo e non in manutenzione deve passare il filtro");

        DatiDisponibilitaBean b = out.get(0);
        assertEquals(d.toString(), b.getData());
        assertEquals(i.toString(), b.getOraInizio());
        assertEquals("11:00", b.getOraFine(), "Ora fine = ora inizio + 60 min");
        assertEquals(20f, b.getCosto(), 0.001f, "Costo pro-rata 60' * 20€/h");
    }

    // =====================================================================================
    // 5) verificaDisponibilita(null): NPE (come da Objects.requireNonNull)
    // =====================================================================================
    @Test
    @DisplayName("5) Verifica disponibilità: param=null → NullPointerException")
    void testVerificaDisponibilita_ParamNull() {
        assertThrows(NullPointerException.class, () -> controller.verificaDisponibilita(null));
    }

    // =====================================================================================
    // Helpers
    // =====================================================================================
    private Campo seedCampo(int id, float costoOrario, boolean attivo, boolean manutenzione) {
        Campo c = new Campo();
        c.setIdCampo(id);
        c.setCostoOrario(costoOrario);
        c.updateStatoOperativo(attivo, manutenzione);
        campoDAO.store(c);
        return c;
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
