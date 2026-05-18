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
 * ✅ NON crea Bean
 * ✅ NON usa Map
 * ✅ passa parametri primitivi
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

        System.out.println("\n=== REGISTRAZIONE ===");

        System.out.print("Nome: ");
        String nome = in.nextLine();

        System.out.print("Cognome: ");
        String cognome = in.nextLine();

        System.out.print("Email: ");
        String email = in.nextLine();

        System.out.print("Password: ");
        String password = in.nextLine();

        // ✅ FIX: uso enum Ruolo
        controller.inviaDatiRegistrazione(
                nome,
                cognome,
                email,
                password,
                Ruolo.UTENTE
        );
    }
}
