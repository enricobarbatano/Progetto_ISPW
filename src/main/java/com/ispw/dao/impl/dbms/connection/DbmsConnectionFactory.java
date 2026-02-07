package com.ispw.dao.impl.dbms.connection;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbmsConnectionFactory implements ConnectionFactory {

    private static DbmsConnectionFactory instance;

    private final String url;
    private final String user;
    private final String password;

    private DbmsConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static void init(String url, String user, String password) {
        if (instance != null) {
            throw new IllegalStateException("DbmsConnectionFactory gia inizializzata.");
        }
        instance = new DbmsConnectionFactory(url, user, password);
    }

    public static DbmsConnectionFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DbmsConnectionFactory non inizializzata (chiama init nel bootstrap).");
        }
        return instance;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

