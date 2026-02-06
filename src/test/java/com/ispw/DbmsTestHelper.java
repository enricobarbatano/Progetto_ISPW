package com.ispw;

import java.sql.Connection;
import java.sql.Statement;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.model.enums.PersistencyProvider;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: utilita' di test per eseguire scenari con persistenza DBMS.
 * Responsabilita': inizializzare la connessione, configurare la DAOFactory e
 * ripristinare lo stato in-memory al termine.
 *
 * SEZIONE LOGICA
 * Incapsula setup DBMS, invocazione del test e pulizia finale della factory.
 */
public final class DbmsTestHelper {

    public static final String DEFAULT_URL =
        "jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome";
    public static final String DEFAULT_USER = "ispw_user";
    public static final String DEFAULT_PASS = "ispw_user";

    private DbmsTestHelper() { }

    public static void runWithDbms(ThrowingRunnable setup, ThrowingRunnable test) throws Exception {
        String url = System.getProperty("db.url", DEFAULT_URL);
        String user = System.getProperty("db.user", DEFAULT_USER);
        String pass = System.getProperty("db.pass", DEFAULT_PASS);

        if (url == null || url.isBlank() || user == null || user.isBlank() || pass == null) {
            return;
        }

        try {
            DbmsConnectionFactory.getInstance();
        } catch (IllegalStateException ex) {
            DbmsConnectionFactory.init(url, user, pass);
        }

        if (setup != null) {
            setup.run();
        }

        resetDaoFactory();
        DAOFactory.initialize(PersistencyProvider.DBMS, null);

        try {
            test.run();
        } finally {
            resetDaoFactory();
            DAOFactory.initialize(PersistencyProvider.IN_MEMORY, null);
        }
    }

    public static void resetDaoFactory() throws Exception {
        var m = DAOFactory.class.getDeclaredMethod("resetForTests");
        m.setAccessible(true);
        m.invoke(null);
    }

    public static void executeStatements(ThrowingRunnable statements) throws Exception {
        statements.run();
    }

    public static void withStatement(ThrowingConsumer<Statement> consumer) throws Exception {
        try (Connection c = DbmsConnectionFactory.getInstance().getConnection();
             Statement st = c.createStatement()) {
            consumer.accept(st);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }
}
