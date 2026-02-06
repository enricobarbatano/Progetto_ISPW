package com.ispw;


import org.junit.jupiter.api.BeforeAll;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.enums.PersistencyProvider;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: base comune per i test che richiedono un DAOFactory configurato.
 * Responsabilita': inizializzare la persistenza in-memory una sola volta.
 *
 * SEZIONE LOGICA
 * Attiva il provider in-memory e ignora il caso di factory gia' inizializzata.
 */
public abstract class BaseDAOTest {

    @BeforeAll
    public static void bootstrapDaoFactory() {
        try {
            DAOFactory.initialize(PersistencyProvider.IN_MEMORY, null);
        } catch (IllegalStateException ignored) { /* già configurata */ }
    }
}
