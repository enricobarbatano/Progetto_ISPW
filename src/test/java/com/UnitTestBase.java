package com;


import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.enums.PersistencyProvider;

public abstract class UnitTestBase {

    @BeforeAll
    @SuppressWarnings("unused")
    static void initDaoFactoryInMemory() {
        // Reset della DAOFactory (è package-private, quindi la chiamiamo via reflection)
        resetDaoFactory();

        // IN_MEMORY non richiede root Path
        DAOFactory.initialize(PersistencyProvider.IN_MEMORY, (Path) null);
    }

    private static void resetDaoFactory() {
        try {
            Method m = DAOFactory.class.getDeclaredMethod("resetForTests");
            m.setAccessible(true);
            m.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Impossibile resettare DAOFactory per i test", e);
        }
    }
}
