package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerLog;
import com.ispw.view.interfaces.ViewLog;
import com.ispw.view.shared.LogViewUtils;

public class CLILogView extends GenericViewCLI implements ViewLog, NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: view CLI log, usa controller grafico.
    // A2) IO: parametri log e input console.

    private final CLIGraphicControllerLog controller;
    private final Scanner in = new Scanner(System.in);

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: verifica ruolo e render logs.

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

        System.out.println("\n=== LOG SISTEMA ===");
        if (logs.isEmpty()) {
            System.out.println("(nessun log disponibile)");
        } else {
            for (Object l : logs) {
                System.out.println(String.valueOf(l));
            }
        }

        CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
    }
}
