package com.ispw.bootstrap;

import java.nio.file.Path;

import com.ispw.bootstrap.setup.SetupData;
import com.ispw.bootstrap.setup.SetupLoader;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.enums.PersistencyProvider;

public final class SetupBootstrapper {

    private static final Path SETUP_FILE = Path.of("setup", "setup.json");

    private SetupBootstrapper() {}

    public static void bootstrapIfNeeded(
            PersistencyProvider provider,
            Path fsRoot
    ) {
        switch (provider) {
            case DBMS -> bootstrapDbms();
            case FILE_SYSTEM -> bootstrapFs(fsRoot);
            case IN_MEMORY -> {
                // nulla ora (fase 2)
            }
        }
    }

    /* ================= DBMS ================= */

    private static void bootstrapDbms() {
        if (DbmsInitializer.isInitialized()) return;

        SetupData data = SetupLoader.load(SETUP_FILE);
        DAOFactory df = DAOFactory.getInstance();

        data.generalUsers.forEach(df.getGeneralUserDAO()::store);
        data.utentiFinali.forEach(df.getUtenteFinaleDAO()::store);
        data.gestori.forEach(df.getGestoreDAO()::store);
        data.campi.forEach(df.getCampoDAO()::store);

        df.getRegolePenalitaDAO().save(data.regolePenalita);
        df.getRegoleTempisticheDAO().save(data.regoleTempistiche);

        DbmsInitializer.markInitialized();
        System.out.println("[BOOTSTRAP] DBMS inizializzato da setup.json");
    }

    /* ================= FILE SYSTEM ================= */

    private static void bootstrapFs(Path fsRoot) {
        if (FileSystemInitializer.isInitialized(fsRoot)) return;

        SetupData data = SetupLoader.load(SETUP_FILE);
        DAOFactory df = DAOFactory.getInstance();

        data.generalUsers.forEach(df.getGeneralUserDAO()::store);
        data.utentiFinali.forEach(df.getUtenteFinaleDAO()::store);
        data.gestori.forEach(df.getGestoreDAO()::store);
        data.campi.forEach(df.getCampoDAO()::store);

        df.getRegolePenalitaDAO().save(data.regolePenalita);
        df.getRegoleTempisticheDAO().save(data.regoleTempistiche);

        FileSystemInitializer.markInitialized(fsRoot);
        System.out.println("[BOOTSTRAP] FILE_SYSTEM inizializzato da setup.json");
    }
}
