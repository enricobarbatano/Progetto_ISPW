package com.ispw.view.cli;

import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicLoginController;
import com.ispw.view.cli.console.ConsoleLoginView;
import com.ispw.view.interfaces.ViewLogin;

/**
 * View CLI per la schermata di login.
 */
public class CLILoginView extends GenericViewCLI implements ViewLogin, NavigableController {

    private final CLIGraphicLoginController controller;
    private final ConsoleLoginView console;

    public CLILoginView(CLIGraphicLoginController controller) {
        this(controller, new ConsoleLoginView());
    }

    public CLILoginView(CLIGraphicLoginController controller, ConsoleLoginView console) {
        this.controller = controller;
        this.console = console;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
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

        String err = getLastError();
        console.render();

        if (err != null && !err.isBlank()) {
            console.showError(err);
        }

        String choice = console.readChoice();
        if ("2".equals(choice)) {
            controller.vaiARegistrazione();
            return;
        }

        String email;
        if (choice != null && choice.contains("@")) {
            email = choice;
        } else {
            email = console.readEmail();
        }
        String password = console.readPassword();
        controller.effettuaLoginRaw(email, password);
    }
}
