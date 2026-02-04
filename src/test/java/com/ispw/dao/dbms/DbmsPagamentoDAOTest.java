package com.ispw.dao.dbms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.dao.impl.dbms.concrete.DbmsPagamentoDAO;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

/**
 * Test per DbmsPagamentoDAO con H2 in-memory.
 * Copre: insert/update/delete/load/exists + findByPrenotazione.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DbmsPagamentoDAOTest {

    private PagamentoDAO dao;

    @BeforeAll
    void initDb() throws java.sql.SQLException {
        // 1) Init H2 in-memory (una sola volta)
        try {
            DbmsConnectionFactory.getInstance();
        } catch (IllegalStateException ex) {
            DbmsConnectionFactory.init("jdbc:h2:mem:ispw;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        }

        // 2) DDL minimale per la tabella pagamenti
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             Statement st = c.createStatement()) {

            st.execute("DROP TABLE IF EXISTS pagamenti");

            st.execute("""
                CREATE TABLE pagamenti (
                  id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
                  id_prenotazione INT NOT NULL,
                  importo_finale DECIMAL(10,2),
                  metodo VARCHAR(40),
                  stato VARCHAR(40),
                  data_pagamento TIMESTAMP
                )
            """);
        }

        // 3) SUT
        dao = new DbmsPagamentoDAO(DbmsConnectionFactory.getInstance());
    }

    @Test
    @Order(1)
    @DisplayName("Insert: salva un pagamento e lo ricarica con load")
    void testInsertAndLoad() {
        Pagamento p = new Pagamento();
        // id_pagamento non impostato: sarà generato dal DB
        p.setIdPrenotazione(900);
        p.setImportoFinale(new BigDecimal("25.50"));
        p.setMetodo(null); // evitiamo dipendenza dagli enum
        p.setStato(null);
        p.setDataPagamento(LocalDateTime.now());

        dao.store(p);

        assertTrue(p.getIdPagamento() > 0, "ID deve essere assegnato (>0) dopo lo store (auto-increment)");
        Pagamento loaded = dao.load(p.getIdPagamento());
        assertNotNull(loaded);
        assertEquals(p.getIdPrenotazione(), loaded.getIdPrenotazione());
        assertEquals(new BigDecimal("25.50"), loaded.getImportoFinale());
    }

    @Test
    @Order(2)
    @DisplayName("Update: modifica importo e verifica con load/findByPrenotazione")
    void testUpdateAndFindByPrenotazione() {
        Pagamento any = dao.findByPrenotazione(900);
        assertNotNull(any, "Deve esistere un pagamento per la prenotazione 900");

        // Update importo
        any.setImportoFinale(new BigDecimal("29.99"));
        dao.store(any);

        Pagamento reloaded = dao.load(any.getIdPagamento());
        assertNotNull(reloaded);
        assertEquals(new BigDecimal("29.99"), reloaded.getImportoFinale());

        // Finder specifico
        Pagamento byPren = dao.findByPrenotazione(900);
        assertNotNull(byPren);
        assertEquals(any.getIdPagamento(), byPren.getIdPagamento(),
                "findByPrenotazione deve restituire il pagamento aggiornato");
    }

    @Test
    @Order(3)
    @DisplayName("exists/delete: verifica esistenza, elimina e controlla che non esista più")
    void testExistsAndDelete() {
        Pagamento target = dao.findByPrenotazione(900);
        assertNotNull(target);

        assertTrue(dao.exists(target.getIdPagamento()));
        dao.delete(target.getIdPagamento());
        assertFalse(dao.exists(target.getIdPagamento()));
        assertNull(dao.load(target.getIdPagamento()));
        assertNull(dao.findByPrenotazione(900), "Dopo la delete non devo trovare la prenotazione 900");
    }

    @Test
    @Order(4)
    @DisplayName("findByPrenotazione: nessun risultato → null")
    void testFindByPrenotazioneEmpty() {
        assertNull(dao.findByPrenotazione(12345));
    }
}