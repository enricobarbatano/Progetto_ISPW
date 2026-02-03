package com.ispw.controller.logic.ctrl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.UtenteBean;
import com.ispw.model.enums.Ruolo;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerGestioneNotifica extends BaseDAOTest {

    private LogicControllerGestioneNotifica controller;
    private Logger logger;
    private TestLogHandler handler;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerGestioneNotifica();

        // catturo i log del controller
        logger = Logger.getLogger(LogicControllerGestioneNotifica.class.getName());
        logger.setUseParentHandlers(false);
        // rimuovo eventuali handler residui
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        handler = new TestLogHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    @Test
    @DisplayName("1) Conferma cancellazione: utente valido → INFO con contesto corretto")
    void testConfermaCancellazione_UtenteValido() {
        UtenteBean u = new UtenteBean("Mario", "Rossi", "mario.rossi@example.org", Ruolo.UTENTE);

        controller.inviaConfermaCancellazione(u, "Dettaglio cancellazione");

        LogRecord r = handler.last();
        assertNotNull(r, "Atteso un log");
        assertEquals(Level.INFO, r.getLevel());
        assertTrue(r.getMessage().contains("Conferma cancellazione"));
        assertTrue(r.getMessage().contains("[NOTIFICA]"));
    }

    @Test
    @DisplayName("2) Conferma cancellazione: utente null → WARNING con 'utente=null'")
    void testConfermaCancellazione_UtenteNull() {
        controller.inviaConfermaCancellazione(null, "Dettaglio cancellazione");

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.WARNING, r.getLevel());
        assertTrue(r.getMessage().contains("[NOTIFICA][WARN]"));
        assertTrue(r.getMessage().contains("utente=null"));
    }

    @Test
    @DisplayName("3) Conferma registrazione: utente valido → INFO")
    void testConfermaRegistrazione() {
        UtenteBean u = new UtenteBean("Giulia", "Verdi", "giulia.verdi@example.org", Ruolo.UTENTE);

        controller.inviaConfermaRegistrazione(u);

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.INFO, r.getLevel());
        assertTrue(r.getMessage().contains("Conferma registrazione"));
    }

    @Test
    @DisplayName("4) Aggiornamento account: utente null → WARNING")
    void testAggiornamentoAccount_UtenteNull() {
        controller.inviaConfermaAggiornamentoAccount(null);

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.WARNING, r.getLevel());
        assertTrue(r.getMessage().contains("[NOTIFICA][WARN]"));
        assertTrue(r.getMessage().contains("utente=null"));
    }

    @Test
    @DisplayName("5) Penalità: idUtente vuoto → INFO con 'idUtente=VUOTO'")
    void testNotificaPenalita_IdVuoto() {
        controller.inviaNotificaPenalita("  ");

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.INFO, r.getLevel());
        assertTrue(r.getMessage().contains("Notifica penalità"));
        assertTrue(r.getMessage().contains("idUtente=VUOTO"));
    }

    @Test
    @DisplayName("6) Conferma prenotazione: utente valido → INFO con contesto")
    void testConfermaPrenotazione_UtenteValido() {
        UtenteBean u = new UtenteBean("Luca", "Bianchi", "luca.bianchi@example.org", Ruolo.UTENTE);

        controller.inviaConfermaPrenotazione(u, "Dettagli prenotazione");

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.INFO, r.getLevel());
        assertTrue(r.getMessage().contains("Conferma prenotazione"));
    }

    @Test
    @DisplayName("7) Imposta promemoria: parametri validi → INFO [PROMEMORIA] con parametri")
    void testImpostaPromemoria_Valido() {
        controller.impostaPromemoria(123, 30);

        LogRecord r = handler.last();
        assertNotNull(r);
        // messaggio con placeholder + parametri (JUL parametrico)
        assertEquals(Level.INFO, r.getLevel());
        assertTrue(r.getMessage().contains("[PROMEMORIA]"));
        assertNotNull(r.getParameters(), "Parametri JUL presenti");
        assertEquals(123, r.getParameters()[0]);
        assertEquals(30,  r.getParameters()[1]);
    }

    @Test
    @DisplayName("8) Imposta promemoria: idPrenotazione non valido → WARNING")
    void testImpostaPromemoria_IdNonValido() {
        controller.impostaPromemoria(0, 30);

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.WARNING, r.getLevel());
        assertTrue(r.getMessage().contains("[NOTIFICA][WARN]"));
        assertTrue(r.getMessage().contains("idPrenotazione non valido"));
    }

    @Test
    @DisplayName("9) Imposta promemoria: minuti non validi → WARNING")
    void testImpostaPromemoria_MinutiNonValidi() {
        controller.impostaPromemoria(123, 0);

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.WARNING, r.getLevel());
        assertTrue(r.getMessage().contains("[NOTIFICA][WARN]"));
        assertTrue(r.getMessage().contains("minutiAnticipo non valido"));
    }

    @Test
    @DisplayName("10) Broadcast aggiornamento regole → INFO")
    void testBroadcastRegole() {
        controller.inviaNotificaAggiornamentoRegole();

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.INFO, r.getLevel());
        assertTrue(r.getMessage().contains("Aggiornamento regole"));
        assertTrue(r.getMessage().contains("BROADCAST"));
    }

    // =====================================================================================
    // Handler per catturare i log
    // =====================================================================================
    private static final class TestLogHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();
        @Override public void publish(LogRecord record) { records.add(record); }
        @Override public void flush() { 
            
 /**
     * Nota: metodo intenzionalmente vuoto.
     * Questo handler memorizza i LogRecord in memoria (lista) e non scrive su stream/sink esterni;
     * non c'è quindi alcun buffer sottostante da svuotare. Lasciarlo no-op evita effetti collaterali
     * nei test e rende il comportamento prevedibile.
     */

        }
        @Override public void close() throws SecurityException { records.clear(); }
        LogRecord last() { return records.isEmpty() ? null : records.get(records.size() - 1); }
        int size() { return records.size(); }
        LogRecord get(int idx) { return (idx >= 0 && idx < records.size()) ? records.get(idx) : null; }
    }
}