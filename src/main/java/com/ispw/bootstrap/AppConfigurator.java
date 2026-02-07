package com.ispw.bootstrap;

import com.ispw.model.enums.AppMode;
import com.ispw.model.enums.FrontendProvider;
import com.ispw.model.enums.PersistencyProvider;

public final class AppConfigurator {

    private final ConsoleMenu menu = new ConsoleMenu();

    public AppConfig askUserConfiguration() {
        // 1) UI
        int uiChoice = menu.askOption("Seleziona Interfaccia", "CLI", "GUI");
        FrontendProvider frontend = (uiChoice == 1) ? FrontendProvider.CLI : FrontendProvider.GUI;

        // 2) Mode
        int modeChoice = menu.askOption("Seleziona Modalita ",
                "DEMO (in-memory, no persistenza)", "STANDARD (con persistenza)");
        AppMode mode = (modeChoice == 1) ? AppMode.DEMO : AppMode.STANDARD;

        // 3) Persistenza
        PersistencyProvider persistency;
        if (mode == AppMode.DEMO) {
            // DEMO => MEMORY forzato
            persistency = PersistencyProvider.IN_MEMORY;
        } else {
            int pChoice = menu.askOption("Seleziona Persistenza", "FILE_SYSTEM", "DBMS");
            persistency = (pChoice == 1) ? PersistencyProvider.FILE_SYSTEM : PersistencyProvider.DBMS;
        }

        return new AppConfig(frontend, mode, persistency);
    }
}
