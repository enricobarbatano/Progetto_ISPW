package com.ispw.view.cli;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.cli.CLIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.interfaces.ViewGestioneRegole;

/**
 * View CLI per la gestione delle regole.
 *
 * RESPONSABILITÀ:
 * - mostrare lista campi;
 * - raccogliere input utente;
 * - chiamare il graphic controller con dati semplici.
 *
 * NON:
 * - crea bean;
 * - usa Map per passare input alla logica;
 * - chiama direttamente logic controller o DAO.
 */
public class CLIRegoleView extends GenericViewCLI
        implements ViewGestioneRegole, NavigableController {

    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerRegole controller;

    private Integer selectedCampoId;

    public CLIRegoleView(CLIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        if (!hasCampiPayload()) {
            controller.richiediListaCampi();
            return;
        }

        showCampi();
        showSelectedCampo();
        printMenu();

        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> controller.richiediListaCampi();
            case "2" -> handleSelezionaCampo();
            case "3" -> handleAggiornaCampo();
            case "4" -> handleAggiornaTempistiche();
            case "5" -> handleAggiornaPenalita();
            case "0" -> controller.tornaAllaHome();
            default -> System.out.println("Scelta non valida");
        }
    }

    private boolean hasCampiPayload() {
        return lastParams.get(GraphicControllerUtils.KEY_CAMPI) instanceof List<?>;
    }

    private void showCampi() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_CAMPI);

        if (!(raw instanceof List<?> campiObj)) {
            return;
        }

        List<String> campi = campiObj.stream()
                .map(Object::toString)
                .toList();

        System.out.println("\n=== CAMPI ===");

        if (campi.isEmpty()) {
            System.out.println("(nessun campo disponibile)");
            return;
        }

        int i = 1;
        for (String campo : campi) {
            System.out.println("[" + i++ + "] " + campo);
        }
    }

    private void showSelectedCampo() {
        Object rawId = lastParams.get(GraphicControllerUtils.KEY_ID_CAMPO);

        if (rawId instanceof Number n && n.intValue() > 0) {
            selectedCampoId = n.intValue();
        }

        if (selectedCampoId != null) {
            System.out.println("Campo selezionato: " + selectedCampoId);
        }
    }

    private void printMenu() {
        System.out.println("\n1) Lista campi");
        System.out.println("2) Seleziona campo");
        System.out.println("3) Aggiorna stato campo");
        System.out.println("4) Aggiorna tempistiche");
        System.out.println("5) Aggiorna penalita");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
    }

    private void handleSelezionaCampo() {
        Integer id = readPositiveInt("Id campo: ");

        if (id == null) {
            System.out.println("Id campo non valido");
            return;
        }

        selectedCampoId = id;
        controller.selezionaCampo(id);
    }

    private void handleAggiornaCampo() {
        Integer id = selectedCampoId != null ? selectedCampoId : readPositiveInt("Id campo: ");

        if (id == null) {
            System.out.println("Id campo non valido");
            return;
        }

        boolean attivo = readBoolean("Attivo? (true/false): ");
        boolean manutenzione = readBoolean("Manutenzione? (true/false): ");

        controller.aggiornaStatoCampo(id, attivo, manutenzione);
    }

    private void handleAggiornaTempistiche() {
        Integer durata = readPositiveInt("Durata slot (min): ");
        Integer preavviso = readNonNegativeInt("Preavviso minimo (min): ");

        if (durata == null || preavviso == null) {
            System.out.println("Durata o preavviso non validi");
            return;
        }

        try {
            System.out.print("Ora apertura (HH:mm): ");
            LocalTime apertura = LocalTime.parse(in.nextLine().trim());

            System.out.print("Ora chiusura (HH:mm): ");
            LocalTime chiusura = LocalTime.parse(in.nextLine().trim());

            controller.aggiornaTempistiche(
                    preavviso,
                    durata,
                    apertura,
                    chiusura
            );

        } catch (DateTimeParseException e) {
            System.out.println("Formato ora non valido. Usa HH:mm.");
        }
    }

    private void handleAggiornaPenalita() {
        try {
            System.out.print("Valore penalita: ");
            BigDecimal valore = new BigDecimal(in.nextLine().trim());

            if (valore.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Valore penalita non valido");
                return;
            }

            Integer preavviso = readNonNegativeInt("Preavviso minimo (min): ");

            if (preavviso == null) {
                System.out.println("Preavviso non valido");
                return;
            }

            controller.aggiornaPenalita(preavviso, valore);

        } catch (NumberFormatException e) {
            System.out.println("Valore penalita non valido");
        }
    }

    private Integer readPositiveInt(String prompt) {
        Integer value = readInteger(prompt);
        return value != null && value > 0 ? value : null;
    }

    private Integer readNonNegativeInt(String prompt) {
        Integer value = readInteger(prompt);
        return value != null && value >= 0 ? value : null;
    }

    private Integer readInteger(String prompt) {
        System.out.print(prompt);

        try {
            return Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean readBoolean(String prompt) {
        System.out.print(prompt);
        return Boolean.parseBoolean(in.nextLine().trim());
    }
}