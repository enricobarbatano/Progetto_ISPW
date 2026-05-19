package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.interfaces.ViewLog;
import com.ispw.view.shared.LogViewUtils;

/**
 * View CLI per la visualizzazione dei log.
 *
 * RESPONSABILITÀ:
 * - verificare autorizzazione gestore;
 * - mostrare log ricevuti dal graphic controller;
 * - delegare caricamento log e ritorno home.
 *
 * NON:
 * - accede a DAO;
 * - chiama logic controller;
 * - crea bean.
 */
public class CLILogView extends GenericViewCLI implements ViewLog, NavigableController {

    private final CLIGraphicControllerLog controller;
    private final Scanner in = new Scanner(System.in);

    public CLILogView(CLIGraphicControllerLog controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        if (!LogViewUtils.isGestore(sessione)) {
            System.out.println("Accesso ai log riservato al gestore.");
            controller.tornaAllaHome();
            return;
        }

        List<String> logs = LogViewUtils.readLogs(lastParams);

        if (logs == null) {
            controller.richiediLog(20);
            return;
        }

        renderLogs(logs);
        CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
    }

    /**
     * Stampa i log ricevuti.
     */
    private void renderLogs(List<String> logs) {
        System.out.println("\n=== LOG SISTEMA ===");

        if (logs.isEmpty()) {
            System.out.println("(nessun log disponibile)");
            return;
        }

        for (String log : logs) {
            System.out.println(" - " + log);
        }
    }
}