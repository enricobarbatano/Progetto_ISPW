package com.ispw.view.cli;

import java.util.Map;

import com.ispw.controller.graphic.cli.CLIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.cli.console.ConsoleLoginView;
import com.ispw.view.interfaces.ViewLogin;

/**
 * View CLI login.
 *
 * RESPONSABILITÀ:
 * - raccoglie email/password
 * - chiama controller con dati semplici
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

        sessione = null;

        console.render();
        console.showError(getLastError());

        String choice = console.readChoice();

        if ("2".equals(choice)) {
            controller.vaiARegistrazione();
            return;
        }

        String email = console.readEmail();
        String password = console.readPassword();

        // ✅ chiamata corretta
        controller.effettuaLogin(email, password);
    }
}
