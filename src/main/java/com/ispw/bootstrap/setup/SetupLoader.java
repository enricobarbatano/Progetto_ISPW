package com.ispw.bootstrap.setup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class per il caricamento del file setup.json.
 *
 * Responsabilità:
 * - leggere il file di setup iniziale;
 * - deserializzare il JSON in un oggetto SetupData;
 * - configurare Jackson per supportare eventuali tipi java.time.
 *
 * NON contiene:
 * - logica DAO;
 * - logica di business;
 * - scrittura su DB o FileSystem.
 */
public final class SetupLoader {

    /**
     * ObjectMapper condiviso per leggere setup.json.
     *
     * JavaTimeModule abilita il supporto ai tipi java.time.
     * WRITE_DATES_AS_TIMESTAMPS disabilitato mantiene date/tempi
     * in formato testuale leggibile.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Costruttore privato.
     *
     * La classe espone solo metodi statici,
     * quindi non deve essere istanziata.
     */
    private SetupLoader() {
        // Utility class: nessuna istanza necessaria.
    }

    /**
     * Legge il file setup.json e lo converte in SetupData.
     *
     * @param path percorso del file setup.json
     * @return dati di setup deserializzati
     *
     * In caso di errore di lettura o parsing JSON viene lanciata
     * una UncheckedIOException, perché l'errore nasce da I/O su file.
     */
    public static SetupData load(Path path) {
        try {
            return MAPPER.readValue(path.toFile(), SetupData.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Errore lettura setup.json", e);
        }
    }
}