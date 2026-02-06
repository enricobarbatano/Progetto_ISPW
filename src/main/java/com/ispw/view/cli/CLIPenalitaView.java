package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerPenalita;
import com.ispw.view.interfaces.ViewGestionePenalita;

/**
 * View CLI per applicazione penalità.
 */
public class CLIPenalitaView extends GenericViewCLI implements ViewGestionePenalita, NavigableController {
    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerPenalita controller;

    private Integer lastId;

    public CLIPenalitaView(CLIGraphicControllerPenalita controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        Object rawUtenti = lastParams.get(GraphicControllerUtils.KEY_UTENTI);
        if (rawUtenti instanceof List<?> list) {
            System.out.println("\n=== UTENTI ===");
            for (Object u : list) {
                System.out.println(String.valueOf(u));
            }
            System.out.print("Id utente da selezionare (0 = annulla): ");
            String raw = in.nextLine().trim();
            if (!raw.isBlank()) {
                int id = Integer.parseInt(raw);
                if (id > 0) {
                    lastId = id;
                }
            }
        }

        System.out.println("1) Lista utenti");
        System.out.println("2) Applica penalità");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> controller.richiediListaUtenti();
            case "2" -> handleApplicaPenalita();
            case "0" -> controller.tornaAllaHome();
            default -> System.out.println("Scelta non valida");
        }
    }

    private void handleApplicaPenalita() {
        int id = readIdUtente();
        System.out.print("Importo: ");
        float importo = Float.parseFloat(in.nextLine());
        System.out.print("Motivazione: ");
        String motivazione = in.nextLine();
        controller.applicaPenalita(id, importo, motivazione);
    }

    private int readIdUtente() {
        if (lastId != null) {
            System.out.print("Id utente (invio per confermare " + lastId + "): ");
            String raw = in.nextLine().trim();
            if (raw.isBlank()) {
                return lastId;
            }
        } else {
            System.out.print("Id utente: ");
            String raw = in.nextLine().trim();
            if (!raw.isBlank()) {
                return Integer.parseInt(raw);
            }
        }
        System.out.print("Id utente: ");
        return Integer.parseInt(in.nextLine());
    }
}
