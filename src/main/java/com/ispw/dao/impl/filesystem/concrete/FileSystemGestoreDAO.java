package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.Permesso;

/**
 * SEZIONE ARCHITETTURALE
 * Ruolo: DAO FileSystem per Gestore.
 * Responsabilita': persistere una mappa serializzata su file locale.
 *
 * SEZIONE LOGICA
 * Usa FileSystemDAO per (de)serializzazione e accesso alla cache.
 */
public class FileSystemGestoreDAO extends FileSystemDAO<Integer, Gestore> implements GestoreDAO {

    private static final String FILE_NAME = "gestore.ser";

    public FileSystemGestoreDAO(Path storageDir) {
        super(storageDir, FILE_NAME, new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Gestore entity) {
        return entity != null ? entity.getIdUtente() : 0; // chiave = idUtente
    }

    @Override
    public Gestore findById(int idGestore) {
        return load(idGestore);
    }

    @Override
    public Gestore findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;
        return this.cache.values().stream()
                .filter(g -> g != null && g.getEmail() != null
                          && g.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<Permesso> getPermessi(int idGestore) {
        final Gestore g = load(idGestore);
        if (g == null || g.getPermessi() == null) return Collections.emptySet();
        // ritorno un Set immutabile costruito dalla List
        return Set.copyOf(g.getPermessi());
    }

    @Override
    public boolean hasPermesso(int idGestore, Permesso permesso) {
        final Gestore g = load(idGestore);
        return g != null && g.getPermessi() != null && g.getPermessi().contains(permesso);
    }

    @Override
    public void assegnaPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");
        final Gestore g = load(idGestore);
        if (g == null) return;
        var list = g.getPermessi(); // è una List<Permesso>
        if (list != null && !list.contains(permesso) && list.add(permesso)) {
            store(g); // persisti su file
        }
    }

    @Override
    public void rimuoviPermesso(int idGestore, Permesso permesso) {
        Objects.requireNonNull(permesso, "permesso non può essere null");
        final Gestore g = load(idGestore);
        if (g == null) return;
        var list = g.getPermessi();
        if (list != null && list.remove(permesso)) {
            store(g); // persisti su file
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
