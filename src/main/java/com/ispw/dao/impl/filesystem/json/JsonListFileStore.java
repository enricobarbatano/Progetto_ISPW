package com.ispw.dao.impl.filesystem.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ispw.dao.exception.DaoException;

/**
 * JsonListFileStore<E>
 * -------------------
 * Utility "semplice" per persistere una LISTA di oggetti E su un singolo file JSON.
 *
 * RESPONSABILITÀ:
 *  - readAll(): legge List<E> dal file JSON
 *  - writeAll(): scrive List<E> sul file JSON
 *
 * NON È responsabilità dello store:
 *  - generazione ID
 *  - query/filtri/join
 *  - regole di business
 *  - caching
 *
 * Nota:
 *  - Supporta LocalDate/LocalTime grazie a JavaTimeModule.
 *  - L’ordine stabile (Comparator) è opzionale: serve per output deterministico.
 */
public class JsonListFileStore<E> {

    /**
     * ObjectMapper condiviso-> è il componente jackson che effettua la trasformazione O.O<--> json:
     * - JavaTimeModule: supporto tipi java.time
     * - WRITE_DATES_AS_TIMESTAMPS disabilitato: date serializzate come stringhe (ISO) e non timestamp numerici.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** File JSON di destinazione (es. storageDir/campi.json) */
    private final Path filePath;

    /** TypeReference necessario a Jackson per deserializzare correttamente List<E> (type erasure dei generics). */
    private final TypeReference<List<E>> typeRef;

    /**
     * Comparator opzionale per ottenere output deterministico (stabile) nel file JSON.
     * Se null, viene mantenuto l'ordine dell'input.
     */
    private final Comparator<E> stableOrder;
    public JsonListFileStore(Path filePath, TypeReference<List<E>> typeRef) {
        this(filePath, typeRef, null);
    }

    public JsonListFileStore(Path filePath, TypeReference<List<E>> typeRef, Comparator<E> stableOrder) {
        this.filePath = filePath;
        this.typeRef = typeRef;
        this.stableOrder = stableOrder;
    }

    /**
     * controlla se il file esiste, se non esiste ritorna un eccezione
     * se esiste l'objectmapper llegge il json (filepath.tofile converte la stringa dentro filepath in un path)
     *  e lo converte in una lista di tipo typereference che è relativo ad ogni dao
     */
    public List<E> readAll() {
        if (Files.notExists(filePath)) return new ArrayList<>();
        try {
            return MAPPER.readValue(filePath.toFile(), typeRef);
        } catch (IOException e) {
            throw new DaoException("Errore lettura JSON: " + filePath, e);
        }
    }

    /**
     * Scrive tutti gli elementi sul file JSON (SCRITTURA SEMPLICE).
     *
     * Caratteristiche:
     * - crea la directory padre se manca
     * - copia difensiva dell'input (evita side-effect sul chiamante)
     * - applica stableOrder (se presente)
     * - scrive direttamente nel file finale (senza tmp + move atomica)
     *
     * Trade-off:
     * - semplice e diretto
     * - meno robusto in caso di crash durante la scrittura (possibile JSON parziale)
     */
    public void writeAll(List<E> data) {
        List<E> toWrite = (data == null) ? new ArrayList<>() : new ArrayList<>(data);

        if (stableOrder != null) {
            toWrite.sort(stableOrder);
        }

        try {
            // Assicura che la directory padre esista
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // SCRITTURA SEMPLICE: scrive direttamente sul file definitivo
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), toWrite);

        } catch (IOException e) {
            throw new DaoException("Errore scrittura JSON: " + filePath, e);
        }
    }

      /**
     * Helper: trasforma una lista in mappa usando un estrattore di id.
     * (Lo metto qui per comodità, così i DAO restano puliti.)
        Prendi una lista di oggetti E
        estrai da ogni oggetto una chiave K
        costruisci una Map<K, E>
      */
    
    
}