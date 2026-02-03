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

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerGestioneManutenzione extends BaseDAOTest {

    private LogicControllerGestioneManutenzione controller;
    private Logger logger;
    private TestLogHandler handler;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerGestioneManutenzione();

        // Catturo i log del controller (JUL)
        logger = Logger.getLogger(LogicControllerGestioneManutenzione.class.getName());
        logger.setUseParentHandlers(false);
        // pulizia handler esistenti
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        handler = new TestLogHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    @Test
    @DisplayName("1) inviaAlertManutentore: id non valido (<=0) → WARNING con messaggio e parametro")
    void testInviaAlert_IdNonValido() {
        controller.inviaAlertManutentore(0);

        LogRecord r = handler.last();
        assertNotNull(r, "Atteso un record di log");
        assertEquals(Level.WARNING, r.getLevel(), "Atteso livello WARNING");
        assertTrue(r.getMessage().contains("[MANUTENZIONE][WARN]"));
        // JUL parametrico: il valore è in parameters[0]
        assertNotNull(r.getParameters());
        assertEquals(0, r.getParameters()[0], "Parametro idCampo deve essere 0");
    }

    @Test
    @DisplayName("2) inviaAlertManutentore: id valido → INFO con contesto Campo#<id> e parametro")
    void testInviaAlert_IdValido() {
        controller.inviaAlertManutentore(42);

        LogRecord r = handler.last();
        assertNotNull(r);
        assertEquals(Level.INFO, r.getLevel(), "Atteso livello INFO");
        assertTrue(r.getMessage().contains("[MANUTENZIONE]"));
        assertNotNull(r.getParameters());
        assertEquals(42, r.getParameters()[0], "Parametro idCampo deve essere 42");
    }

    @Test
    @DisplayName("3) inviaAlertManutentore: sequenza WARN→INFO → ordine e contenuto coerenti")
    void testInviaAlert_Sequenza() {
        controller.inviaAlertManutentore(-5);   // warn
        controller.inviaAlertManutentore(7);    // info

        assertTrue(handler.size() >= 2, "Attesi almeno 2 record di log");

        LogRecord first = handler.get(handler.size() - 2);
        LogRecord last  = handler.last();

        assertEquals(Level.WARNING, first.getLevel());
        assertNotNull(first.getParameters());
        assertEquals(-5, first.getParameters()[0]);

        assertEquals(Level.INFO, last.getLevel());
        assertNotNull(last.getParameters());
        assertEquals(7, last.getParameters()[0]);
    }

    // Handler per catturare i log del controller (JUL)
    private static final class TestLogHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();
        @Override public void publish(LogRecord record) { records.add(record); }
        @Override public void flush() { }
        @Override public void close() throws SecurityException { records.clear(); }
        LogRecord last() { return records.isEmpty() ? null : records.get(records.size() - 1); }
        int size() { return records.size(); }
        LogRecord get(int idx) { return (idx >= 0 && idx < records.size()) ? records.get(idx) : null; }
    }
}