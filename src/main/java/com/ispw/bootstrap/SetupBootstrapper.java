package com.ispw.bootstrap;

import java.nio.file.Path;
import java.util.logging.Logger;

import com.ispw.bootstrap.setup.SetupData;
import com.ispw.bootstrap.setup.SetupLoader;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.enums.PersistencyProvider;

/**
 * Bootstrapper dei dati iniziali dell'applicazione.
 *
 * Responsabilità:
 * - controllare se il provider è già inizializzato;
 * - leggere setup/setup.json quando serve;
 * - salvare i dati iniziali tramite i DAO ottenuti dalla DAOFactory;
 * - marcare il provider come inizializzato.
 *
 * NON contiene:
 * - logica di business;
 * - logica SQL;
 * - logica FileSystem diretta sui JSON dei DAO.
 */
public final class SetupBootstrapper {

    /**
     * Logger usato al posto di System.out.
     */
    private static final Logger LOGGER = Logger.getLogger(SetupBootstrapper.class.getName());

    /**
     * Percorso standard del file di setup iniziale.
     */
    private static final Path SETUP_FILE = Path.of("setup", "setup.json");

    /**
     * Costruttore privato.
     *
     * La classe contiene solo metodi statici di bootstrap,
     * quindi non deve essere istanziata.
     */
    private SetupBootstrapper() {
        // Utility class: nessuna istanza necessaria.
    }

    /**
     * Avvia il bootstrap solo se necessario.
     *
     * DBMS:
     * - controlla marker su tabella app_metadata.
     *
     * FILE_SYSTEM:
     * - controlla marker .initialized nella root FS.
     *
     * IN_MEMORY:
     * - non richiede bootstrap qui, perché il seed viene letto dai BaseDAO.
     */
    public static void bootstrapIfNeeded(
            PersistencyProvider provider,
            Path fsRoot
    ) {
        switch (provider) {
            case DBMS -> bootstrapDbms();
            case FILE_SYSTEM -> bootstrapFs(fsRoot);
            case IN_MEMORY -> {
                // Nessuna azione: i DAO in-memory seeded leggono direttamente dalla seed root.
            }
        }
    }

    // ================= DBMS =================

    /**
     * Esegue il bootstrap dei dati iniziali su DBMS.
     *
     * Se il DB è già inizializzato, il metodo termina subito.
     * Altrimenti legge setup.json, salva i dati tramite DAOFactory
     * e poi marca il DB come inizializzato.
     */
    private static void bootstrapDbms() {
        if (DbmsInitializer.isInitialized()) {
            return;
        }

        SetupData data = SetupLoader.load(SETUP_FILE);
        DAOFactory daoFactory = DAOFactory.getInstance();

        data.getGeneralUsers().forEach(daoFactory.getGeneralUserDAO()::store);
        data.getUtentiFinali().forEach(daoFactory.getUtenteFinaleDAO()::store);
        data.getGestori().forEach(daoFactory.getGestoreDAO()::store);
        data.getCampi().forEach(daoFactory.getCampoDAO()::store);

        daoFactory.getRegolePenalitaDAO().save(data.getRegolePenalita());
        daoFactory.getRegoleTempisticheDAO().save(data.getRegoleTempistiche());

        DbmsInitializer.markInitialized();
        LOGGER.info("[BOOTSTRAP] DBMS inizializzato da setup.json");
    }

    // ================= FILE SYSTEM =================

    /**
     * Esegue il bootstrap dei dati iniziali su FileSystem.
     *
     * Se il FileSystem è già inizializzato, il metodo termina subito.
     * Altrimenti legge setup.json, salva i dati tramite DAOFactory
     * e poi crea il marker .initialized.
     */
    private static void bootstrapFs(Path fsRoot) {
        if (FileSystemInitializer.isInitialized(fsRoot)) {
            return;
        }

        SetupData data = SetupLoader.load(SETUP_FILE);
        DAOFactory daoFactory = DAOFactory.getInstance();

        data.getGeneralUsers().forEach(daoFactory.getGeneralUserDAO()::store);
        data.getUtentiFinali().forEach(daoFactory.getUtenteFinaleDAO()::store);
        data.getGestori().forEach(daoFactory.getGestoreDAO()::store);
        data.getCampi().forEach(daoFactory.getCampoDAO()::store);

        daoFactory.getRegolePenalitaDAO().save(data.getRegolePenalita());
        daoFactory.getRegoleTempisticheDAO().save(data.getRegoleTempistiche());

        FileSystemInitializer.markInitialized(fsRoot);
        LOGGER.info("[BOOTSTRAP] FILE_SYSTEM inizializzato da setup.json");
    }
}