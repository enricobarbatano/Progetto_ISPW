package com.ispw.bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.factory.FrontendControllerFactory;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.model.enums.PersistencyProvider;

public final class AppBootstrapper {

    @SuppressWarnings("java:S1312") // Logger con nome della classe: scelta progettuale condivisa
    private static final Logger LOGGER = Logger.getLogger(AppBootstrapper.class.getName());

    public static void main(String[] args) {

        // 1) Leggi configurazione
        AppConfigurator configurator = new AppConfigurator();
        AppConfig config = configurator.askUserConfiguration();

        // 2) Config DBMS (solo se DBMS)
        if (config.persistency() == PersistencyProvider.DBMS) {
            DbmsConnectionFactory.init(
                // NOTA: niente HTML entities nell’URL
                "jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome",
                "ispw_user",
                "ispw_user"
            );

            // (Opzionale) ping DB per verificare connessione
            try (var c  = DbmsConnectionFactory.getInstance().getConnection();
                 var ps = c.prepareStatement("SELECT 1");
                 var rs = ps.executeQuery()) {

                if (rs.next()) {
                    // Log “on-demand” (nessun placeholder, nessun rischio)
                    LOGGER.info(() -> "DB OK: " );
                }

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Errore connessione DB", e);
                return;
            }
        }

        // 2b) Config FILE_SYSTEM (solo se FILE_SYSTEM)
        if (config.persistency() == PersistencyProvider.FILE_SYSTEM) {
            Path root = Paths.get("C:\\Users\\User\\OneDrive\\Desktop\\Progetti_Uni\\Progetto_ISPW\\FileSystem");
            try {
                Files.createDirectories(root);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Impossibile creare directory root per FILE_SYSTEM: " + root, e);
                return;
            }
            DAOFactory.setFileSystemRoot(root);

            LOGGER.log(Level.INFO, "FILE_SYSTEM root impostata su: {0}", root.toAbsolutePath());
        }

        // 3) Configura persistenza (DAOFactory guidata dal provider)
        DAOFactory.setPersistencyProvider(config.persistency());
        // Messaggio parametrico: MessageFormat
        LOGGER.log(Level.INFO, "Persistency provider: {0}", config.persistency());

        // 4) Configura frontend
        FrontendControllerFactory.setFrontendProvider(config.frontend());
        LOGGER.log(Level.INFO, "Frontend provider: {0}", config.frontend());

        // 5) Avvio UI (nessun parametro -> ok Supplier o stringa diretta)
        LOGGER.info("Avvio applicazione...");
        FrontendControllerFactory.getInstance().startApplication();
    }
}