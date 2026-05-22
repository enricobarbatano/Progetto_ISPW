package com.ispw.view.cli;

import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerRegistrazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewRegistrazione;

/**
 * View CLI registrazione.
 *
 * NON crea Bean.
 * NON gestisce eccezioni.
 * NON chiama direttamente il logic controller.
 * Legge solo i dati grezzi dalla console e li passa al graphic controller.
 */
public class CLIRegistrazioneView extends GenericViewCLI
        implements ViewRegistrazione, NavigableController {

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
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        showRouteError(params);

        System.out.println("\n=== REGISTRAZIONE ===");

        System.out.print("Nome: ");
        String nome = in.nextLine();

        System.out.print("Cognome: ");
        String cognome = in.nextLine();

        System.out.print("Email: ");
        String email = in.nextLine();

        System.out.print("Password: ");
        String password = in.nextLine();

        controller.inviaDatiRegistrazione(
                nome,
                cognome,
                email,
                password,
                Ruolo.UTENTE
        );
    }

    private void showRouteError(Map<String, Object> params) {
        if (params == null) {
            return;
        }

        Object error = params.get(GraphicControllerUtils.KEY_ERROR);
        if (error != null) {
            System.out.println("[ERRORE] " + error);
        }
    }
}