package com.ispw.dao.factory;


import com.ispw.model.enums.PersistencyProvider;
import com.ispw.dao.interfaces.GeneralUserDAO;

public abstract class DAOFactory {

    private static PersistencyProvider provider;
    private static DAOFactory instance;

    /** Imposta il provider UNA SOLA VOLTA nel bootstrap */
    public static void setPersistencyProvider(PersistencyProvider p) {
        if (provider != null) {
            throw new IllegalStateException("PersistencyProvider già impostato. Non puoi cambiarlo a runtime.");
        }
        provider = p;
    }

    /** Ritorna sempre la stessa istanza (Singleton) */
    public static DAOFactory getInstance() {
        if (provider == null) {
            throw new IllegalStateException("DAOFactory non configurata. Chiama setPersistencyProvider() prima.");
        }
        if (instance == null) {
            instance = switch (provider) {
                case IN_MEMORY   -> new MemoryDAOFactory();
                case FILE_SYSTEM -> new FileSystemDAOFactory();
                case DBMS        -> new DbmsDAOFactory();
            };
        }
        return instance;
    }

    /**
     * (Opzionale) Mantieni questo metodo solo se ti serve compatibilità,
     * ma NON crea nuove istanze: configura e poi ritorna il singleton.
     */
    @Deprecated
    public static DAOFactory getFactory(PersistencyProvider p) {
        setPersistencyProvider(p);
        return getInstance();
    }

    // ===== metodi astratti per ottenere i DAO concreti =====
    public abstract GeneralUserDAO getGeneralUserDAO();

    // Aggiungerai qui gli altri:
    // public abstract CampoDAO getCampoDAO();
    // public abstract PrenotazioneDAO getPrenotazioneDAO();
}

