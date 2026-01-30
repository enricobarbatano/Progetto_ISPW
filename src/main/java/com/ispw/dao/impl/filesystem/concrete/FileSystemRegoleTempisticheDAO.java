package com.ispw.dao.impl.filesystem.concrete;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

public class FileSystemRegoleTempisticheDAO implements RegoleTempisticheDAO {

    private final Path file;

    public FileSystemRegoleTempisticheDAO(Path storageDir) {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.file = storageDir.resolve("regole_tempistiche.ser");
    }

    @Override
    public RegoleTempistiche get() {
        if (!Files.exists(file)) return null;
        try (InputStream is = Files.newInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            return (RegoleTempistiche) ois.readObject();
        } catch (EOFException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Errore lettura regole_tempistiche", e);
        }
    }

    @Override
    public void save(RegoleTempistiche regole) {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream os = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(regole);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura regole_tempistiche", e);
        }
    }
}
