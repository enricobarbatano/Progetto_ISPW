package com.ispw.view.cli;

import java.util.Map;
import java.util.Scanner;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.model.enums.Ruolo;
import com.ispw.view.interfaces.ViewHomeProfilo;

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

        final SessioneUtenteBean s = sessione;
        final Ruolo ruolo = (s != null && s.getUtente() != null) ? s.getUtente().getRuolo() : null;

        System.out.println("\n=== HOME ===");
        if (ruolo != null) {
            System.out.println("Ruolo: " + ruolo);
        } else {
            System.out.println("(ruolo non disponibile)");
        }

        // Menu comune
        System.out.println("1) Account");

        // Menu per ruolo
        if (ruolo == Ruolo.UTENTE) {
            System.out.println("2) Prenotazione");
            System.out.println("3) Disdetta (richiesta)");
        } else if (ruolo == Ruolo.GESTORE) {
            System.out.println("2) Regole");
            System.out.println("3) Penalita");
            System.out.println("4) Log");
            System.out.println("5) Richieste disdetta"); // ✅ nuovo UC complesso (step gestore)
        }

        System.out.println("0) Logout");
        System.out.print("Scelta: ");
        final String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> goTo(GraphicControllerUtils.ROUTE_ACCOUNT);

            case "2" -> {
                if (ruolo == Ruolo.GESTORE) {
                    goTo(GraphicControllerUtils.ROUTE_REGOLE);
                } else if (ruolo == Ruolo.UTENTE) {
                    goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE);
                } else {
                    sceltaNonValida();
                }
            }

            case "3" -> {
                if (ruolo == Ruolo.GESTORE) {
                    goTo(GraphicControllerUtils.ROUTE_PENALITA);
                } else if (ruolo == Ruolo.UTENTE) {
                    goTo(GraphicControllerUtils.ROUTE_DISDETTA);
                } else {
                    sceltaNonValida();
                }
            }

            case "4" -> {
                if (ruolo == Ruolo.GESTORE) {
                    goTo(GraphicControllerUtils.ROUTE_LOGS);
                } else {
                    sceltaNonValida();
                }
            }

            case "5" -> {
                if (ruolo == Ruolo.GESTORE) {
                    goTo(GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA);
                } else {
                    sceltaNonValida();
                }
            }

            case "0" -> goTo(GraphicControllerUtils.ROUTE_LOGIN);

            default -> sceltaNonValida();
        }
    }

    private void sceltaNonValida() {
        System.out.println("Scelta non valida");
    }

    private void goTo(String route) {
        if (navigator == null) return;

        if (sessione != null) {
            navigator.goTo(route, Map.of(GraphicControllerUtils.KEY_SESSIONE, sessione));
        } else {
            navigator.goTo(route);
        }
    }
}