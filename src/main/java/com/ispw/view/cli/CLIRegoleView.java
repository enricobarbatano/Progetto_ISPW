package com.ispw.view.cli;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerRegole;
import com.ispw.view.interfaces.ViewGestioneRegole;

/**
 * View CLI per gestione regole.
 */
public class CLIRegoleView extends GenericViewCLI implements ViewGestioneRegole, NavigableController {
    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicControllerRegole controller;

    private Integer selectedCampoId;
    private List<String> lastCampi;

    public CLIRegoleView(CLIGraphicControllerRegole controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
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

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);
        if (rawCampi instanceof List<?> campi) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) campi;
            lastCampi = list;
            System.out.println("\n=== CAMPI ===");
            int i = 1;
            for (Object c : list) {
                final int idx = i++;
                System.out.println(" [" + idx + "] " + c);
            }
        }
        Object rawId = lastParams.get(GraphicControllerUtils.KEY_ID_CAMPO);
        if (rawId instanceof Integer id) {
            selectedCampoId = id;
            System.out.println("Campo selezionato: " + id);
        }

        System.out.println("1) Lista campi");
        System.out.println("2) Seleziona campo");
        System.out.println("3) Aggiorna stato campo");
        System.out.println("4) Aggiorna tempistiche");
        System.out.println("5) Aggiorna penalità");
        System.out.println("0) Home");
        System.out.print("Scelta: ");
        String scelta = in.nextLine().trim();

        switch (scelta) {
            case "1" -> controller.richiediListaCampi();
            case "2" -> handleSelezionaCampo();
            case "3" -> handleAggiornaCampo();
            case "4" -> handleAggiornaTempistiche();
            case "5" -> handleAggiornaPenalita();
            case "0" -> controller.tornaAllaHome();
            default -> handleSceltaAlternativa(scelta);
        }
    }

    private void handleSceltaAlternativa(String scelta) {
        if (isNumeric(scelta) && lastCampi != null && !lastCampi.isEmpty()) {
            int n = Integer.parseInt(scelta);
            Integer id = resolveCampoIdFromChoice(n, lastCampi);
            if (id != null) {
                controller.selezionaCampo(id);
                return;
            }
        }
        System.out.println("Scelta non valida");
    }

    private void handleSelezionaCampo() {
        System.out.print("Id campo: ");
        int id = Integer.parseInt(in.nextLine());
        controller.selezionaCampo(id);
    }

    private void handleAggiornaCampo() {
        if (selectedCampoId == null && !lastParams.containsKey(GraphicControllerUtils.KEY_CAMPI)) {
            controller.richiediListaCampi();
            return;
        }

        Object rawCampi = lastParams.get(GraphicControllerUtils.KEY_CAMPI);
        if (rawCampi instanceof List<?> campi) {
            System.out.println("\n=== CAMPI ===");
            int i = 1;
            for (Object c : campi) {
                final int idx = i++;
                System.out.println(" [" + idx + "] " + c);
            }
        }

        int id;
        if (selectedCampoId != null) {
            id = selectedCampoId;
        } else {
            id = readIdCampo();
        }
        System.out.print("Attivo? (true/false): ");
        boolean attivo = Boolean.parseBoolean(in.nextLine());
        System.out.print("Manutenzione? (true/false): ");
        boolean manut = Boolean.parseBoolean(in.nextLine());

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put(GraphicControllerUtils.KEY_ID_CAMPO, id);
        payload.put(GraphicControllerUtils.KEY_ATTIVO, attivo);
        payload.put(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE, manut);
        controller.aggiornaStatoCampo(payload);
    }

    private void handleAggiornaTempistiche() {
        try {
            System.out.print("Durata slot (min): ");
            int durata = Integer.parseInt(in.nextLine());
            System.out.print("Ora apertura (HH:mm): ");
            LocalTime apertura = LocalTime.parse(in.nextLine().trim());
            System.out.print("Ora chiusura (HH:mm): ");
            LocalTime chiusura = LocalTime.parse(in.nextLine().trim());
            System.out.print("Preavviso minimo (min): ");
            int preavviso = Integer.parseInt(in.nextLine());

            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI, durata);
            payload.put(GraphicControllerUtils.KEY_ORA_APERTURA, apertura);
            payload.put(GraphicControllerUtils.KEY_ORA_CHIUSURA, chiusura);
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, preavviso);
            controller.aggiornaTempistiche(payload);
        } catch (RuntimeException ex) {
            System.err.println("[ERRORE] Dati tempistiche non validi");
        }
    }

    private void handleAggiornaPenalita() {
        try {
            System.out.print("Valore penalità: ");
            BigDecimal valore = new BigDecimal(in.nextLine().trim());
            System.out.print("Preavviso minimo (min): ");
            String rawPreavviso = in.nextLine().trim();
            int preavviso = rawPreavviso.isBlank() ? 0 : Integer.parseInt(rawPreavviso);

            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put(GraphicControllerUtils.KEY_VALORE_PENALITA, valore);
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, preavviso);
            controller.aggiornaPenalita(payload);
        } catch (RuntimeException ex) {
            System.err.println("[ERRORE] Dati penalità non validi");
        }
    }

    private int readIdCampo() {
        System.out.print("Id campo: ");
        return Integer.parseInt(in.nextLine());
    }

    private boolean isNumeric(String value) {
        return value != null && value.matches("\\d+");
    }

    private Integer resolveCampoIdFromChoice(int choice, List<String> campi) {
        if (choice <= 0) {
            return null;
        }
        if (choice <= campi.size()) {
            return parseIdFromCampo(campi.get(choice - 1));
        }
        return choice; // fallback: assume choice is an actual idCampo
    }

    private Integer parseIdFromCampo(String campo) {
        if (campo == null) return null;
        int hash = campo.indexOf('#');
        if (hash < 0) return null;
        int end = hash + 1;
        while (end < campo.length() && Character.isDigit(campo.charAt(end))) {
            end++;
        }
        if (end == hash + 1) return null;
        int value = 0;
        for (int i = hash + 1; i < end; i++) {
            int digit = Character.digit(campo.charAt(i), 10);
            if (digit < 0) {
                return null;
            }
            value = value * 10 + digit;
        }
        return value;
    }
}
