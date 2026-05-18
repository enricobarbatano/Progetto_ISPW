package com.ispw.view.cli;

import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerAccount;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.interfaces.ViewGestioneAccount;

public class CLIAccountView extends GenericViewCLI
        implements ViewGestioneAccount, NavigableController {

    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerAccount controller;

    public CLIAccountView(CLIGraphicControllerAccount controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_ACCOUNT;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        System.out.println("\n=== ACCOUNT ===");

        Object raw = lastParams.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);

        if (raw instanceof Map<?, ?> dati) {
            System.out.println("Utente: "
                    + dati.get(GraphicControllerUtils.KEY_NOME) + " "
                    + dati.get(GraphicControllerUtils.KEY_COGNOME));
        }

        System.out.println("1) Ricarica dati");
        System.out.println("2) Aggiorna account");
        System.out.println("3) Cambia password");
        System.out.println("0) Home");

        String scelta = in.nextLine();

        switch (scelta) {
            case "1" -> controller.loadAccount(sessione);
            case "2" -> handleAggiorna();
            case "3" -> handlePassword();
            case "0" -> controller.tornaAllaHome(sessione);
            default -> System.out.println("Scelta non valida");
        }
    }

    private void handleAggiorna() {

        Object raw = lastParams.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);

        if (!(raw instanceof Map<?, ?> dati)) {
            controller.loadAccount(sessione);
            return;
        }

        int idUtente = (int) dati.get(GraphicControllerUtils.KEY_ID_UTENTE);

        System.out.print("Nome: ");
        String nome = in.nextLine();

        System.out.print("Cognome: ");
        String cognome = in.nextLine();

        System.out.print("Email: ");
        String email = in.nextLine();

        controller.aggiornaDatiAccount(
                idUtente,
                nome,
                cognome,
                email,
                sessione
        );
    }

    private void handlePassword() {
        System.out.print("Vecchia password: ");
        String oldPwd = in.nextLine();

        System.out.print("Nuova password: ");
        String newPwd = in.nextLine();

        controller.cambiaPassword(oldPwd, newPwd, sessione);
    }
}
