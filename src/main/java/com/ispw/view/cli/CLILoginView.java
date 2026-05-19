package com.ispw.view.cli;

import java.util.Map;

import com.ispw.controller.graphic.cli.CLIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.cli.console.ConsoleLoginView;
import com.ispw.view.interfaces.ViewLogin;

/**
 * View CLI per il login.
 *
 * RESPONSABILITÀ:
 * - mostrare il menu di login;
 * - raccogliere email e password;
 * - delegare il login al graphic controller;
 * - delegare il passaggio alla registrazione;
 * - delegare la chiusura dell'applicazione.
 *
 * NON:
 * - crea bean;
 * - chiama direttamente il logic controller;
 * - accede a DAO o persistenza;
 * - gestisce logica applicativa.
 */
public class CLILoginView extends GenericViewCLI
        implements ViewLogin, NavigableController {

    private final CLIGraphicLoginController controller;
    private final ConsoleLoginView console;

    public CLILoginView(CLIGraphicLoginController controller) {
        this(controller, new ConsoleLoginView());
    }

    public CLILoginView(CLIGraphicLoginController controller,
                        ConsoleLoginView console) {
        this.controller = controller;
        this.console = console;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        // Il login rappresenta l'ingresso non autenticato.
        sessione = null;

        console.render();
        console.showError(getLastError());

        String choice = console.readChoice();

        switch (choice) {
            case "1" -> handleLogin();
            case "2" -> controller.vaiARegistrazione();
            case "0" -> handleExit();
            default -> handleInvalidChoice();
        }
    }

    /**
     * Legge le credenziali da console e le passa al graphic controller.
     */
    private void handleLogin() {
        String email = console.readEmail();
        String password = console.readPassword();

        controller.effettuaLogin(email, password);
    }

    /**
     * Chiude l'applicazione.
     */
    private void handleExit() {
        console.showInfo("Chiusura applicazione...");
        controller.esci();
    }

    /**
     * Gestisce una scelta non valida.
     *
     * Non chiama goToLogin(), perché quel metodo è protected.
     * Usa logout(), che è pubblico e riporta alla route login.
     */
    private void handleInvalidChoice() {
        console.showError("Scelta non valida");
        controller.logout();
    }
}