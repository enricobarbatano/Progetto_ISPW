package com.ispw.bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;

final class FileSystemInitializer {

    static boolean isInitialized(Path root) {
        return Files.exists(root.resolve(".initialized"));
    }

    static void markInitialized(Path root) {
        try {
            Files.createFile(root.resolve(".initialized"));
        } catch (Exception e) {
            throw new RuntimeException("Errore marker FILE_SYSTEM", e);
        }
    }
}
