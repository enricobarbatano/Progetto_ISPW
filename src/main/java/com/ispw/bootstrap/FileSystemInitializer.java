package com.ispw.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ispw.dao.exception.DaoException;

/**
 * Utility di bootstrap per il provider FileSystem.
 *
 * Responsabilità:
 * - verificare se la directory FileSystem è già stata inizializzata;
 * - creare un file marker ".initialized" dopo il setup.
 *
 * NON contiene:
 * - logica DAO;
 * - logica di business;
 * - gestione JSON.
 */
final class FileSystemInitializer {

    /**
     * Costruttore privato.
     *
     * La classe contiene solo metodi statici di utilità,
     * quindi non deve essere istanziata.
     */
    private FileSystemInitializer() {
        // Utility class: nessuna istanza necessaria.
    }

    /**
     * Controlla se il file marker ".initialized" esiste.
     *
     * Se esiste, il FileSystem è già stato preparato.
     */
    static boolean isInitialized(Path root) {
        return Files.exists(root.resolve(".initialized"));
    }

    /**
     * Crea il file marker ".initialized".
     *
     * Questo file indica che il setup iniziale del FileSystem
     * è già stato eseguito.
     */
    static void markInitialized(Path root) {
        try {
            Files.createFile(root.resolve(".initialized"));
        } catch (IOException e) {
            throw new DaoException("Errore marker FILE_SYSTEM", e);
        }
    }
}