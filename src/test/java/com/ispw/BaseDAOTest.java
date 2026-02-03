package com.ispw;


import org.junit.jupiter.api.BeforeAll;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.enums.PersistencyProvider;

public abstract class BaseDAOTest {

    @BeforeAll
    public static void bootstrapDaoFactory() {
        try {
            DAOFactory.setPersistencyProvider(PersistencyProvider.IN_MEMORY);
        } catch (IllegalStateException ignored) { /* già configurata */ }
    }
}
