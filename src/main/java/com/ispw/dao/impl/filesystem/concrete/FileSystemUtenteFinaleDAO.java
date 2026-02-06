package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.UtenteFinale;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO FileSystem per UtenteFinale.
 * Responsabilita': persistere una mappa serializzata su file locale.
 *
 * SEZIONE LOGICA
 * Usa FileSystemDAO per (de)serializzazione e accesso alla cache.
 */
public class FileSystemUtenteFinaleDAO extends FileSystemDAO<Integer, UtenteFinale> implements UtenteFinaleDAO {

    private static final String FILE_NAME = "utente_finale.ser";

    public FileSystemUtenteFinaleDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(UtenteFinale entity) {
        return entity != null ? entity.getIdUtente() : 0;
    }

    @Override
    public void store(UtenteFinale entity) {
        Objects.requireNonNull(entity, "entity non può essere null");
        if (entity.getIdUtente() == 0) {
            final int next = this.cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }
        super.store(entity);
    }

    @Override
    public UtenteFinale findById(int idUtente) {
        return load(idUtente);
    }

    @Override
    public UtenteFinale findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;
        return this.cache.values().stream()
                .filter(u -> u != null && u.getEmail() != null
                        && u.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}