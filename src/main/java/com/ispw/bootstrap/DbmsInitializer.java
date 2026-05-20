package com.ispw.bootstrap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;

/**
 * Utility di bootstrap per il DBMS.
 *
 * Responsabilità:
 * - controllare se il database è già stato inizializzato;
 * - scrivere il marker di inizializzazione nella tabella app_metadata.
 *
 * NON contiene:
 * - logica DAO;
 * - logica di business;
 * - creazione delle tabelle.
 */
final class DbmsInitializer {

    /**
     * Costruttore privato.
     *
     * La classe contiene solo metodi statici di utilità,
     * quindi non deve essere istanziata.
     */
    private DbmsInitializer() {
        // Utility class: nessuna istanza necessaria.
    }

    /**
     * Verifica se il marker "initialized" è già presente nel DB.
     *
     * Se la query trova almeno una riga, il DB risulta già inizializzato.
     * Se la query fallisce, viene restituito false per permettere il bootstrap.
     */
    static boolean isInitialized() {
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM app_metadata WHERE k='initialized'");
             var rs = ps.executeQuery()) {

            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Inserisce il marker di inizializzazione nel DB.
     *
     * Questo metodo viene chiamato dopo il setup iniziale,
     * così ai successivi avvii il sistema sa che il DB è già pronto.
     */
    static void markInitialized() {
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO app_metadata (k, v) VALUES ('initialized','true')")) {

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Errore marker DBMS", e);
        }
    }
}
