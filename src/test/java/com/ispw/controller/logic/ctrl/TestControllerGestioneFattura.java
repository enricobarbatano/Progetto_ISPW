package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.DatiFatturaBean;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerGestioneFattura extends BaseDAOTest {

    private LogicControllerGestioneFattura controller;
    private FatturaDAO fatturaDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerGestioneFattura();
        fatturaDAO = DAOFactory.getInstance().getFatturaDAO();
        assertNotNull(fatturaDAO, "DAOFactory.getFatturaDAO() ha restituito null");
        tryClear(fatturaDAO);
    }

    // =====================================================================================
    // 1) Fattura PRENOTAZIONE
    // =====================================================================================
    @Test
    @DisplayName("1) Prenotazione: dati validi → fattura emessa con link FATT-<id>-yyyyMMdd.pdf")
    void testFatturaPrenotazione_Ok() {
        int idPren = 123;
        LocalDate opDate = LocalDate.of(2026, 2, 3);
        String expectedSuffix = opDate.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd

        DatiFatturaBean dati = new DatiFatturaBean();
        dati.setCodiceFiscaleCliente("RSSMRA80A01H501Z");
        dati.setDataOperazione(opDate);

        Fattura f = controller.generaFatturaPrenotazione(dati, idPren);
        assertNotNull(f, "Fattura attesa");
        assertEquals(idPren, f.getIdPrenotazione());
        assertNotNull(f.getLinkPdf());
        assertEquals(f.getLinkPdf().equals("FATT-" + idPren + "-" + expectedSuffix + ".pdf"),
                   "Link PDF atteso FATT-" + idPren + "-" + expectedSuffix + ".pdf");
        assertEquals(opDate, f.getDataEmissione());
    }

    @Test
    @DisplayName("2) Prenotazione: dati NON validi (CF mancante) → null")
    void testFatturaPrenotazione_DatiNonValidi() {
        DatiFatturaBean dati = new DatiFatturaBean();
        dati.setCodiceFiscaleCliente(null);
        dati.setDataOperazione(LocalDate.now());

        Fattura f = controller.generaFatturaPrenotazione(dati, 456);
        assertNull(f, "Con dati non validi deve tornare null");
    }

    @Test
    @DisplayName("3) Prenotazione: id non valido (<=0) → null")
    void testFatturaPrenotazione_IdNonValido() {
        DatiFatturaBean dati = new DatiFatturaBean();
        dati.setCodiceFiscaleCliente("RSSMRA80A01H501Z");
        dati.setDataOperazione(LocalDate.now());

        Fattura f = controller.generaFatturaPrenotazione(dati, 0);
        assertNull(f, "Con id prenotazione non valido deve tornare null");
    }

    // =====================================================================================
    // 2) Fattura PENALITÀ
    // =====================================================================================
    @Test
    @DisplayName("4) Penalità: dati validi → fattura emessa con ref negativo e link PEN-<abs(id)>-yyyyMMdd.pdf")
    void testFatturaPenalita_Ok() {
        int idPen = 77;
        LocalDate opDate = LocalDate.of(2026, 2, 3);
        String expectedSuffix = opDate.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd

        DatiFatturaBean dati = new DatiFatturaBean();
        dati.setCodiceFiscaleCliente("RSSMRA80A01H501Z");
        dati.setDataOperazione(opDate);

        Fattura f = controller.generaFatturaPenalita(dati, idPen);
        assertNotNull(f);
        assertEquals(-idPen, f.getIdPrenotazione(), "Ref negativo atteso (-idPenalita)");
        assertEquals(f.getLinkPdf(), "PEN-" + idPen + "-" + expectedSuffix + ".pdf",
                   "Link PDF atteso PEN-" + idPen + "-" + expectedSuffix + ".pdf");
        assertEquals(opDate, f.getDataEmissione());
    }

    @Test
    @DisplayName("5) Penalità: dati NON validi → null")
    void testFatturaPenalita_DatiNonValidi() {
        DatiFatturaBean dati = new DatiFatturaBean(); // CF mancante
        Fattura f = controller.generaFatturaPenalita(dati, 88);
        assertNull(f, "Con dati non validi deve tornare null");
    }

    // =====================================================================================
    // 3) Nota di credito (RIMBORSO)
    // =====================================================================================
    @Test
    @DisplayName("6) Nota di credito: id valido → nessuna eccezione")
    void testNotaDiCredito_Ok() {
        assertDoesNotThrow(() -> controller.emettiNotaDiCredito(555));
    }

    @Test
    @DisplayName("7) Nota di credito: id NON valido → nessuna eccezione (log warning)")
    void testNotaDiCredito_IdNonValido() {
        assertDoesNotThrow(() -> controller.emettiNotaDiCredito(0));
    }

    // =====================================================================================
    // Helper
    // =====================================================================================
    private static void tryClear(Object dao) {
        if (dao == null) return;
        try {
            Method m = dao.getClass().getMethod("clear");
            m.setAccessible(true);
            m.invoke(dao);
        } catch (ReflectiveOperationException ignored) { }
    }
}