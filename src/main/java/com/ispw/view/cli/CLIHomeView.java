package com.ispw.view.cli;

import java.util.Map;
import java.util.Scanner;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewHomeProfilo;

/**
 * View CLI per home/profilo.
 */
public class CLIHomeView extends GenericViewCLI implements ViewHomeProfilo, NavigableController {
    private final Scanner in = new Scanner(System.in);
    private final GraphicControllerNavigation navigator;

    public CLIHomeView(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_HOME;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        SessioneUtenteBean s = sessione;
        Ruolo ruolo = (s != null && s.getUtente() != null) ? s.getUtente().getRuolo() : null;

        System.out.println("\n=== HOME ===");
        if (ruolo != null) {
            System.out.println("Ruolo: " + ruolo);
        }

        System.out.println("1) Account");
        if (ruolo == Ruolo.UTENTE) {
            System.out.println("2) Prenotazione");
            System.out.println("3) Disdetta");
        } else if (ruolo == Ruolo.GESTORE) {
            System.out.println("2) Regole");
            System.out.println("3) Penalità");
            System.out.println("4) Log");
        }
        System.out.println("0) Logout");
        System.out.print("Scelta: ");
        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> goTo(GraphicControllerUtils.ROUTE_ACCOUNT);
            case "2" -> goTo(ruolo == Ruolo.GESTORE ? GraphicControllerUtils.ROUTE_REGOLE
                                                    : GraphicControllerUtils.ROUTE_PRENOTAZIONE);
            case "3" -> goTo(ruolo == Ruolo.GESTORE ? GraphicControllerUtils.ROUTE_PENALITA
                                                    : GraphicControllerUtils.ROUTE_DISDETTA);
            case "4" -> {
                if (ruolo == Ruolo.GESTORE) {
                    goTo(GraphicControllerUtils.ROUTE_LOGS);
                } else {
                    System.out.println("Scelta non valida");
                }
            }
            case "0" -> goTo(GraphicControllerUtils.ROUTE_LOGIN);
            default -> System.out.println("Scelta non valida");
        }
    }

    private void goTo(String route) {
        if (navigator == null) {
            return;
        }
        if (sessione != null) {
            navigator.goTo(route, Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
        } else {
            navigator.goTo(route);
        }
    }
}
