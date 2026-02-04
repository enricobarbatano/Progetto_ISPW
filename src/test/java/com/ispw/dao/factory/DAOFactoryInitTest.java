package com.ispw.dao.factory;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.ispw.model.enums.PersistencyProvider;

class DAOFactoryInitTest {

    @AfterEach
    @SuppressWarnings("unused")
    void tearDown() {
        // clean up state between tests
        DAOFactory.resetForTests();
    }

    @Test
    void initialize_inMemory_shouldCreateMemoryFactory() {
        DAOFactory.initialize(PersistencyProvider.IN_MEMORY, null);
        DAOFactory instance = DAOFactory.getInstance();
        assertTrue(instance instanceof MemoryDAOFactory);
    }

    @Test
    void initialize_fileSystem_shouldCreateFileSystemFactory() throws Exception {
        Path tmp = Files.createTempDirectory("pspw-fs-test");
        try {
            DAOFactory.initialize(PersistencyProvider.FILE_SYSTEM, tmp);
            DAOFactory instance = DAOFactory.getInstance();
            assertTrue(instance instanceof FileSystemDAOFactory);
        } finally {
            // leave cleanup to tearDown/resetForTests
            // but remove temp dir
            try {
                Files.deleteIfExists(tmp);
            } catch (java.io.IOException ignored) {
                // ignored: temp dir cleanup not critical for test
            }
        }
    }

    @Test
    void initialize_dbms_shouldCreateDbmsFactory() {
        // initialize a lightweight in-memory DB connection factory for tests
        try {
            com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory.init("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        } catch (IllegalStateException ignored) { /* already initialized by another test */ }
        DAOFactory.initialize(PersistencyProvider.DBMS, null);
        DAOFactory instance = DAOFactory.getInstance();
        assertTrue(instance instanceof DbmsDAOFactory);
    }

    @Test
    void initialize_missingRootForFileSystem_shouldThrow() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DAOFactory.initialize(PersistencyProvider.FILE_SYSTEM, null));
        assertTrue(ex.getMessage() == null || !ex.getMessage().isBlank());
    }

    @Test
    void initialize_twice_shouldThrowIllegalState() {
        DAOFactory.initialize(PersistencyProvider.IN_MEMORY, null);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> DAOFactory.initialize(PersistencyProvider.IN_MEMORY, null));
        assertTrue(ex.getMessage() == null || !ex.getMessage().isBlank());
    }
}
