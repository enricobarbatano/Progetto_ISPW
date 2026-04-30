
package com.ispw.bootstrap.setup;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class SetupLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private SetupLoader() {}

    public static SetupData load(Path path) {
        try {
            return MAPPER.readValue(path.toFile(), SetupData.class);
        } catch (Exception e) {
            throw new RuntimeException("Errore lettura setup.json", e);
        }
    }
}
