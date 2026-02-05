package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerLog;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewLog;

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
    public void onShow() {
        onShow(Map.of());
    }

    @Override
    public void onHide() {
        // no-op
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        Ruolo ruolo = (sessione != null && sessione.getUtente() != null) ? sessione.getUtente().getRuolo() : null;
        if (ruolo != Ruolo.GESTORE) {
            System.out.println("Accesso ai log riservato al gestore.");
            controller.tornaAllaHome();
            return;
        }

        Object raw = lastParams.get(GraphicControllerUtils.KEY_LOGS);
        if (!(raw instanceof List<?> logs)) {
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
