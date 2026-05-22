package com.ispw.bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.factory.FrontendControllerFactory;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.model.enums.PersistencyProvider;

public final class AppBootstrapper {

    private static final Logger LOGGER = Logger.getLogger(AppBootstrapper.class.getName());

    private AppBootstrapper() {
        // Utility class: non deve essere istanziata.
    }

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {

        // 1) Leggi configurazione
        AppConfigurator configurator = new AppConfigurator();
        AppConfig config = configurator.askUserConfiguration();

        // 2) Config DBMS (solo se DBMS)
        if (config.persistency() == PersistencyProvider.DBMS) {
            DbmsConnectionFactory.init(
                    "jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome",
                    "ispw_user",
                    "ispw_user"
            );

            // Ping DB per verificare connessione
            try (var c = DbmsConnectionFactory.getInstance().getConnection();
                 var ps = c.prepareStatement("SELECT 1");
                 var rs = ps.executeQuery()) {

                if (rs.next()) {
                    System.out.println("DB OK");
                }

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e, () -> "Errore connessione DB");
                return;
            }
        }

        // 2b) Config FILE_SYSTEM (solo se FILE_SYSTEM)
        Path fsRoot = null;
        if (config.persistency() == PersistencyProvider.FILE_SYSTEM) {
            Path root = Paths.get("C:\\Users\\User\\ISPW_Project\\Progetto_ISPW\\filesystem");
            try {
                Files.createDirectories(root);
            } catch (java.io.IOException | SecurityException e) {
                LOGGER.log(
                        Level.SEVERE,
                        e,
                        () -> "Impossibile creare directory root per FILE_SYSTEM: " + root
                );
                return;
            }
            fsRoot = root;
            System.out.println("FILE_SYSTEM root impostata su: " + root.toAbsolutePath());
        }

        // 2c) Config IN_MEMORY seed root (solo se IN_MEMORY)
        Path seedRoot = null;
        if (config.persistency() == PersistencyProvider.IN_MEMORY) {
            seedRoot = Paths.get("C:\\Users\\User\\ISPW_Project\\Progetto_ISPW\\seed");
            if (!Files.exists(seedRoot)) {
                Path finalSeedRoot = seedRoot;
                LOGGER.severe(() -> "Cartella seed non trovata: " + finalSeedRoot.toAbsolutePath());
                return;
            }
            System.out.println("IN_MEMORY seed root impostata su: " + seedRoot.toAbsolutePath());
        }

        // 3) Configura persistenza
        Path rootForProvider = switch (config.persistency()) {
            case FILE_SYSTEM -> fsRoot;
            case IN_MEMORY -> seedRoot;
            case DBMS -> null;
        };

        DAOFactory.initialize(config.persistency(), rootForProvider);

        SetupBootstrapper.bootstrapIfNeeded(
                config.persistency(),
                rootForProvider
        );

        System.out.println("Persistency provider: " + config.persistency());

        // 4) Configura frontend
        FrontendControllerFactory.setFrontendProvider(config.frontend());
        System.out.println("Frontend provider: " + config.frontend());

        // 5) Avvio UI
        System.out.println("Avvio applicazione...");
        FrontendControllerFactory.getInstance().startApplication();
    }
}