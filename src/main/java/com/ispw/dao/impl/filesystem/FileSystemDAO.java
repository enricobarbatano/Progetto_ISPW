package com.ispw.dao.impl.filesystem;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.dao.interfaces.DAO;

public abstract class FileSystemDAO<I, E> implements DAO<I, E> {

    protected final Map<I, E> cache = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Path filePath;
    private final FileCodec<I, E> codec;

    private static final Logger LOGGER = Logger.getLogger(FileSystemDAO.class.getName());

    /**
     * @param storageDir directory base (es: "storage/")
     * @param fileName nome file per l'entità (es: "campo.dat")
     * @param codec strategia di lettura/scrittura (binaria, json, ecc.)
     */
    protected FileSystemDAO(Path storageDir, String fileName, FileCodec<I, E> codec) {
        this.codec = codec;
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new com.ispw.dao.exception.DaoException("Impossibile creare directory storage: " + storageDir, e);
        }
        this.filePath = storageDir.resolve(fileName);
        loadFromDisk();
    }

    /** Come ricavo la chiave dall'entità */
    protected abstract I getId(E entity);

    // -------------------------
    // Ciclo FS: load / flush
    // -------------------------

    protected void loadFromDisk() {
        lock.writeLock().lock();
        try {
            if (Files.notExists(filePath)) {
                cache.clear();
                return;
            }
            Map<I, E> data = codec.read(filePath).orElseGet(ConcurrentHashMap::new);
            cache.clear();
            cache.putAll(data);
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void flushToDisk() {
        lock.writeLock().lock();
        try {
            // scrittura atomica: file.tmp -> replace file reale
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            codec.write(tmp, cache);

            Files.move(tmp, filePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException e) {
            throw new com.ispw.dao.exception.DaoException("Errore scrittura su file: " + filePath, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -------------------------
    // Implementazione DAO base
    // -------------------------

    @Override
    public E load(I id) {
        lock.readLock().lock();
        try {
            return cache.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void store(E entity) {
        lock.writeLock().lock();
        try {
            cache.put(getId(entity), entity);
            flushToDisk();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(I id) {
        lock.writeLock().lock();
        try {
            cache.remove(id);
            flushToDisk();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(I id) {
        lock.readLock().lock();
        try {
            return cache.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public E create(I id) {
        // in FS di solito non si “crea” senza dati, ma puoi override nei concreti
        return null;
    }

    // ---------------------------------------------------------
    // Codec: per scegliere formato senza cambiare FileSystemDAO
    // ---------------------------------------------------------

    @FunctionalInterface
    protected interface FileCodec<K, V> {
        /**
         * Legge una mappa da file. Può lanciare DaoException in caso di errore di lettura critico.
         * @param file percorso del file
         * @return Optional contenente i dati letti, oppure Optional.empty() se il file non esiste
         * @throws com.ispw.dao.exception.DaoException se si verifica un errore di deserializzazione
         */
        Optional<Map<K, V>> read(Path file) throws com.ispw.dao.exception.DaoException;
        default void write(Path file, Map<K, V> data) throws IOException {
            throw new UnsupportedOperationException("write non implementato");
        }
    }

    /**
     * Codec binario basato su Java Serialization.
     * Richiede che la Map e i contenuti siano Serializable.
     */
    public static class JavaBinaryMapCodec<K, V> implements FileCodec<K, V> {

        @Override
        public Optional<Map<K, V>> read(Path file) {
            try (InputStream is = Files.newInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(is)) {

                @SuppressWarnings("unchecked")
                Map<K, V> data = (Map<K, V>) ois.readObject();
                return Optional.ofNullable(data);

            } catch (NoSuchFileException e) {
                LOGGER.log(Level.FINE, "File non trovato, trattando come vuoto: {0}", new Object[]{file});
                return Optional.empty();
            } catch (EOFException e) {
                LOGGER.log(Level.FINE, "EOF o file corrotto, trattando come vuoto: {0}", new Object[]{file});
                return Optional.empty(); // file vuoto/corrotto -> lo tratti come empty
            } catch (IOException | ClassNotFoundException e) {
                throw new com.ispw.dao.exception.DaoException("Errore lettura file binario: " + file, e);
            }
        }

        @Override
        public void write(Path file, Map<K, V> data) throws IOException {
            try (OutputStream os = Files.newOutputStream(file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
                 ObjectOutputStream oos = new ObjectOutputStream(os)) {

                oos.writeObject(data);
            }
        }
    }
}
