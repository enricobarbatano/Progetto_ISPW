
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
// Nota: questa classe è un helper generico per gestire la persistenza di liste di oggetti su file JSON,
// con supporto per tipi complessi come LocalDate.
// I DAO specifici (es. PrenotazioneDAOFileSystem) possono utilizzare questa classe per gestire la persistenza dei dati in modo semplice e robusto,
public class JsonListFileStore<E> {
    // L'ObjectMapper è un'istanza statica condivisa, configurata con il modulo JavaTimeModule per supportare LocalDate/LocalTime,
    // e con la disabilitazione di WRITE_DATES_AS_TIMESTAMPS per serializzare le date in formato leggibile anziché come timestamp.
    private static final ObjectMapper MAPPER = new ObjectMapper()
    
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Il filePath è il percorso del file JSON dove viene salvata la lista di entità. Viene impostato tramite il costruttore.
    // Il typeRef è un TypeReference di Jackson che specifica il tipo della lista (
    // List<E>) da leggere/scrivere, necessario per la deserializzazione corretta.
    // Il stableOrder è un Comparator opzionale che, se fornito, viene usato per ordinare la lista prima di scriverla su file,
    // garantendo un ordine stabile nel file JSON (utile per il versionamento e la leggibilità).
    private final Path filePath;
    private final TypeReference<List<E>> typeRef;
    private final Comparator<E> stableOrder; // opzionale: ordine stabile su output JSON


    // Il costruttore accetta il filePath, il typeRef e opzionalmente un Comparator per l'ordine stabile.
    //  Se non viene fornito un Comparator, la lista viene scritta nell'ordine originale.
    public JsonListFileStore(Path filePath, TypeReference<List<E>> typeRef) {
        this(filePath, typeRef, null);
    }
    // Il costruttore completo accetta tutti e tre i parametri, permettendo di specificare un Comparator per l'ordine stabile se desiderato.
    public JsonListFileStore(Path filePath, TypeReference<List<E>> typeRef, Comparator<E> stableOrder) {
        this.filePath = filePath;
        this.typeRef = typeRef;
        this.stableOrder = stableOrder;
    }

    // Il metodo readAll legge la lista di entità dal file JSON. Se il file non esiste, restituisce una lista vuota.
    // Se c'è un errore di I/O durante la lettura, lancia una DaoException con un messaggio dettagliato.
    public List<E> readAll() {
        if (Files.notExists(filePath)) return new ArrayList<>();
        try {
            return MAPPER.readValue(filePath.toFile(), typeRef);
        } catch (IOException e) {
            throw new DaoException("Errore lettura JSON: " + filePath, e);
        }
    }
    // Il metodo writeAll scrive la lista di entità sul file JSON. Se viene fornito un Comparator per l'ordine stabile, ordina la lista prima di scriverla.
    // Utilizza un file temporaneo e una mossa atomica per garantire che il file JSON non venga corrotto in caso di errori durante la scrittura.
    public void writeAll(List<E> data) {
        List<E> toWrite = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        if (stableOrder != null) {
            toWrite.sort(stableOrder);
        }

        try {
            Files.createDirectories(filePath.getParent());

            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), toWrite);
            // atomic write: tmp -> move
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
    // Il metodo toMap è un helper che trasforma una lista di entità in una mappa, 
    // utilizzando una funzione di estrazione dell'id per determinare le chiavi della mappa.
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
