package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;    // abstract
import java.util.Locale;  // concreto da istanziare
import java.util.Objects;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.UtenteFinale;

/**
 * DAO FileSystem per GeneralUser.
 * SonarCloud-friendly:
 * - costante per nome file, controlli input, no System.out;
 * - id autoincrement se 0.
 */
public final class FileSystemGeneralUserDAO extends FileSystemDAO<Integer, GeneralUser> implements GeneralUserDAO {

    private static final String FILE_NAME = "general_user.ser";

    public FileSystemGeneralUserDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(GeneralUser entity) {
        return entity.getIdUtente();
    }

    @Override
    public void store(GeneralUser entity) {
        Objects.requireNonNull(entity, "entity non può essere null");
        if (entity.getIdUtente() == 0) {
            int next = this.cache.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdUtente(next);
        }
        super.store(entity);
    }

    @Override
    public GeneralUser create(Integer id) {
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(id != null ? id : 0);
        return u;
    }

    @Override
    public GeneralUser findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;
        return this.cache.values().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    public GeneralUser findById(int idUtente) {
        return load(idUtente);
    }

    @Override
    public List<GeneralUser> findAll() {
        return new ArrayList<>(this.cache.values());
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
