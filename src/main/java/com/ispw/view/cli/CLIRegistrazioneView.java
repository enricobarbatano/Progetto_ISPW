package com.ispw.view.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegistrazione;
import com.ispw.view.interfaces.ViewRegistrazione;

/**
 * View CLI per la registrazione.
 */
public class CLIRegistrazioneView extends GenericViewCLI implements ViewRegistrazione, NavigableController {
    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerRegistrazione controller;

    public CLIRegistrazioneView(CLIGraphicControllerRegistrazione controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGISTRAZIONE;
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

        sessione = null;

        System.out.println("\n=== REGISTRAZIONE ===");

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        Map<String, Object> form = new HashMap<>();
        System.out.print("Nome: ");
        form.put(GraphicControllerUtils.KEY_NOME, in.nextLine());
        System.out.print("Cognome: ");
        form.put(GraphicControllerUtils.KEY_COGNOME, in.nextLine());
        System.out.print("Email: ");
        form.put(GraphicControllerUtils.KEY_EMAIL, in.nextLine());
        System.out.print("Password: ");
        form.put(GraphicControllerUtils.KEY_PASSWORD, in.nextLine());

        controller.inviaDatiRegistrazione(form);
    }
}
