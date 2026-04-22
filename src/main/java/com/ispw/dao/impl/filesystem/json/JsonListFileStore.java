
package com.ispw.dao.impl.filesystem.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.exception.DaoException;

/**
 * Utility riusabile: persiste una LISTA di entità su un singolo file JSON.
 * Supporta LocalDate/LocalTime tramite JavaTimeModule.
 */
public class JsonListFileStore<E> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Path filePath;
    private final TypeReference<List<E>> typeRef;
    private final Comparator<E> stableOrder; // opzionale: ordine stabile su output JSON

    public JsonListFileStore(Path filePath, TypeReference<List<E>> typeRef) {
        this(filePath, typeRef, null);
    }

    public JsonListFileStore(Path filePath, TypeReference<List<E>> typeRef, Comparator<E> stableOrder) {
        this.filePath = filePath;
        this.typeRef = typeRef;
        this.stableOrder = stableOrder;
    }

    public List<E> readAll() {
        if (Files.notExists(filePath)) return new ArrayList<>();
        try {
            return MAPPER.readValue(filePath.toFile(), typeRef);
        } catch (IOException e) {
            throw new DaoException("Errore lettura JSON: " + filePath, e);
        }
    }

    public void writeAll(List<E> data) {
        List<E> toWrite = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        if (stableOrder != null) {
            toWrite.sort(stableOrder);
        }

        try {
            Files.createDirectories(filePath.getParent());

            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), toWrite);

            try {
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFail) {
                // fallback: alcuni FS non supportano ATOMIC_MOVE
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new DaoException("Errore scrittura JSON: " + filePath, e);
        }
    }

    /**
     * Helper: trasforma una lista in mappa usando un estrattore di id.
     * (Lo metto qui per comodità, così i DAO restano puliti.)
     */
    public <K> java.util.Map<K, E> toMap(List<E> list, Function<E, K> idExtractor) {
        java.util.Map<K, E> map = new java.util.concurrent.ConcurrentHashMap<>();
        if (list == null) return map;
        for (E e : list) {
            if (e == null) continue;
            K key = idExtractor.apply(e);
            if (key != null) map.put(key, e);
        }
        return map;
    }
}
