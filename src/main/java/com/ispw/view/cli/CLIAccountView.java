package com.ispw.view.cli;

import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerAccount;
import com.ispw.view.interfaces.ViewGestioneAccount;

/**
 * View CLI per gestione account.
 */
public class CLIAccountView extends GenericViewCLI implements ViewGestioneAccount, NavigableController {
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

        System.out.println("\n=== ACCOUNT ===");

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        Object raw = lastParams.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);
        if (raw instanceof Map<?, ?> dati) {
            Object idUtente = dati.get(GraphicControllerUtils.KEY_ID_UTENTE);
            Object nome = dati.get(GraphicControllerUtils.KEY_NOME);
            Object cognome = dati.get(GraphicControllerUtils.KEY_COGNOME);
            Object email = dati.get(GraphicControllerUtils.KEY_EMAIL);
            final String msg = String.format("Utente: %s %s (%s) [id=%s]", nome, cognome, email, idUtente);
            System.out.println(msg);
        }

        System.out.println("1) Ricarica dati account");
        System.out.println("2) Aggiorna dati account");
        System.out.println("3) Cambia password");
        System.out.println("4) Logout");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> controller.loadAccount(sessione);
            case "2" -> handleAggiorna();
            case "3" -> handleCambiaPassword();
            case "4" -> controller.logout();
            case "0" -> controller.tornaAllaHome(sessione);
            default -> System.out.println("Scelta non valida");
        }
    }

    private void handleAggiorna() {
        if (sessione == null) {
            System.err.println("[ERRORE] Sessione non valida");
            return;
        }

        Object raw = lastParams.get(GraphicControllerUtils.KEY_DATI_ACCOUNT);
        if (!(raw instanceof Map<?, ?> dati) || dati.get(GraphicControllerUtils.KEY_ID_UTENTE) == null) {
            controller.loadAccount(sessione);
            return;
        }

        int idUtente = (int) dati.get(GraphicControllerUtils.KEY_ID_UTENTE);

        System.out.print("Nome (vuoto = non modificare): ");
        String nome = in.nextLine();
        System.out.print("Cognome (vuoto = non modificare): ");
        String cognome = in.nextLine();
        System.out.print("Email (vuoto = non modificare): ");
        String email = in.nextLine();

        Map<String, Object> update = new java.util.HashMap<>();
        update.put(GraphicControllerUtils.KEY_ID_UTENTE, idUtente);
        update.put(GraphicControllerUtils.KEY_SESSIONE, sessione);
        if (!nome.isBlank()) update.put(GraphicControllerUtils.KEY_NOME, nome);
        if (!cognome.isBlank()) update.put(GraphicControllerUtils.KEY_COGNOME, cognome);
        if (!email.isBlank()) update.put(GraphicControllerUtils.KEY_EMAIL, email);

        controller.aggiornaDatiAccount(update);
    }

    private void handleCambiaPassword() {
        if (sessione == null) {
            System.err.println("[ERRORE] Sessione non valida");
            return;
        }
        System.out.print("Vecchia password: ");
        String oldPwd = in.nextLine();
        System.out.print("Nuova password: ");
        String newPwd = in.nextLine();
        if (newPwd == null || newPwd.trim().length() < 6) {
            System.err.println("[ERRORE] Password non valida (min 6 caratteri)");
            return;
        }
        controller.cambiaPassword(oldPwd, newPwd, sessione);
    }
}
