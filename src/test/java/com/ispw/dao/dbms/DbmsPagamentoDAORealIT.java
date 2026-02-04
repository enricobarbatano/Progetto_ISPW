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
import static org.junit.jupiter.api.Assumptions.assumeTrue;
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
 * IT reale su DBMS (MySQL/MariaDB).
 * - Si attiva solo se passi -Ddb.url, -Ddb.user, -Ddb.pass
 * - Crea (se necessario) la tabella pagamenti
 * - Esegue CRUD e finder
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DbmsPagamentoDAORealIT {

    private String url="jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome";
    private String user= "ispw_user";
    private String pass= "ispw_user";

    private PagamentoDAO dao;

    @BeforeAll
    void initIfPropsPresent() throws java.sql.SQLException {
       

        // Skip se le property non sono valorizzate
        assumeTrue(nonBlank(url) && nonBlank(user) && pass != null,
                "Proprietà db non presenti: usa -Ddb.url -Ddb.user -Ddb.pass per abilitare il test reale");

        // Inizializza la ConnectionFactory del progetto
        try {
            DbmsConnectionFactory.getInstance();
        } catch (IllegalStateException ex) {
            DbmsConnectionFactory.init(url, user, pass);
        }

        // Crea tabella se non esiste (DDL semplice; adatta ai tuoi indici/constraint reali)
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             Statement st = c.createStatement()) {

            // MySQL/MariaDB: AUTO_INCREMENT
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
        }

        dao = new DbmsPagamentoDAO(DbmsConnectionFactory.getInstance());
    }

    @Test @Order(1)
    @DisplayName("Real Insert+Load su MySQL")
    void testInsertAndLoadReal() {
        // Se il test è stato skippato per le property mancanti, non arriviamo qui.
        assumeTrue(dao != null, "DAO non inizializzato (test skippato)");

        Pagamento p = new Pagamento();
        p.setIdPrenotazione(42_000);
        p.setImportoFinale(new BigDecimal("19.90"));
        p.setMetodo(null);
        p.setStato(null);
        p.setDataPagamento(LocalDateTime.now());

        dao.store(p);
        assertTrue(p.getIdPagamento() > 0, "ID auto-generato dal DB deve essere > 0");

        Pagamento loaded = dao.load(p.getIdPagamento());
        assertNotNull(loaded);
        assertEquals(42_000, loaded.getIdPrenotazione());
        assertEquals(new BigDecimal("19.90"), loaded.getImportoFinale());
    }

    @Test @Order(2)
    @DisplayName("Real Update+FindByPrenotazione su MySQL")
    void testUpdateAndFindByPrenotazioneReal() {
        assumeTrue(dao != null, "DAO non inizializzato (test skippato)");

        Pagamento first = dao.findByPrenotazione(42_000);
        assertNotNull(first);

        first.setImportoFinale(new BigDecimal("24.99"));
        dao.store(first);

        Pagamento reloaded = dao.load(first.getIdPagamento());
        assertNotNull(reloaded);
        assertEquals(new BigDecimal("24.99"), reloaded.getImportoFinale());

        Pagamento byPren = dao.findByPrenotazione(42_000);
        assertNotNull(byPren);
        assertEquals(first.getIdPagamento(), byPren.getIdPagamento());
    }

    @Test @Order(3)
    @DisplayName("Real Exists+Delete su MySQL")
    void testExistsAndDeleteReal() {
        assumeTrue(dao != null, "DAO non inizializzato (test skippato)");

        Pagamento target = dao.findByPrenotazione(42_000);
        assertNotNull(target);

        assertTrue(dao.exists(target.getIdPagamento()));
        dao.delete(target.getIdPagamento());
        assertFalse(dao.exists(target.getIdPagamento()));
        assertNull(dao.load(target.getIdPagamento()));
        assertNull(dao.findByPrenotazione(42_000));
    }

    private static boolean nonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}