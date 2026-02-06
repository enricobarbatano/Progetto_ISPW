package com.ispw.view.cli;

import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegistrazione;
import com.ispw.view.interfaces.ViewRegistrazione;
import com.ispw.view.shared.RegistrazioneViewUtils;

public class CLIRegistrazioneView extends GenericViewCLI implements ViewRegistrazione, NavigableController {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: view CLI registrazione, usa controller grafico.
    // A2) IO: input console e Map di registrazione.
    // ========================
    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerRegistrazione controller;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: raccolta dati e invio al controller.
    // ========================

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

        sessione = null;

        System.out.println("\n=== REGISTRAZIONE ===");

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        System.out.print("Nome: ");
        String nome = in.nextLine();
        System.out.print("Cognome: ");
        String cognome = in.nextLine();
        System.out.print("Email: ");
        String email = in.nextLine();
        System.out.print("Password: ");
        String password = in.nextLine();

        controller.inviaDatiRegistrazione(
            RegistrazioneViewUtils.buildForm(nome, cognome, email, password)
        );
    }
}
