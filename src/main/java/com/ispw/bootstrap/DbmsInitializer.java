package com.ispw.bootstrap;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;

final class DbmsInitializer {

    static boolean isInitialized() {
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT 1 FROM app_metadata WHERE k='initialized'");
             var rs = ps.executeQuery()) {

            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    static void markInitialized() {
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO app_metadata (k, v) VALUES ('initialized','true')")) {

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Errore marker DBMS", e);
        }
    }
}
