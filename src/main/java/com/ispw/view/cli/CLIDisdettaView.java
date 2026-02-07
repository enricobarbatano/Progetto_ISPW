package com.ispw.view.cli;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.NavigableController;
import com.ispw.controller.graphic.cli.CLIGraphicControllerDisdetta;
import com.ispw.view.cli.console.ConsoleDisdettaAnteprimaView;
import com.ispw.view.cli.console.ConsoleDisdettaElencoView;
import com.ispw.view.cli.console.ConsoleDisdettaEsitoView;
import com.ispw.view.interfaces.ViewDisdettaPrenotazione;

public class CLIDisdettaView extends GenericViewCLI implements ViewDisdettaPrenotazione, NavigableController {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: view CLI disdetta, usa controller grafico e console view.
    // A2) IO: input console e params di disdetta.

    private final CLIGraphicControllerDisdetta controller;
    private final ConsoleDisdettaElencoView elencoView = new ConsoleDisdettaElencoView();
    private final ConsoleDisdettaAnteprimaView anteprimaView = new ConsoleDisdettaAnteprimaView();
    private final ConsoleDisdettaEsitoView esitoView = new ConsoleDisdettaEsitoView();
    private final Scanner in = new Scanner(System.in);

    private Integer selectedId;

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: routing in base ai payload.
    // L2) handleElenco/handleAnteprima/handleSuccess: gestione step.

    public CLIDisdettaView(CLIGraphicControllerDisdetta controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        CliViewUtils.printMessages(getLastError(), getLastSuccess());

        if (handleSuccess()) {
            return;
        }
        if (handleAnteprima()) {
            return;
        }
        if (handleElenco()) {
            return;
        }

        controller.richiediPrenotazioniCancellabili(sessione);
    }

    private boolean handleElenco() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_PRENOTAZIONI);
        if (!(raw instanceof List<?> elenco)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        List<String> lista = (List<String>) elenco;
        elencoView.show(lista);
        System.out.print("Id prenotazione da disdire (0 = annulla): ");
        int id = Integer.parseInt(in.nextLine());
        if (id <= 0) {
            return true;
        }
        selectedId = id;
        controller.richiediAnteprimaDisdetta(id, sessione);
        return true;
    }

    private boolean handleAnteprima() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_ANTEPRIMA);
        if (!(raw instanceof Map<?, ?> anteprima)) {
            return false;
        }
        Object possibile = anteprima.get(GraphicControllerUtils.KEY_POSSIBILE);
        Object penale = anteprima.get(GraphicControllerUtils.KEY_PENALE);
        boolean poss = possibile instanceof Boolean b && b;
        float pen = penale instanceof Number n ? n.floatValue() : 0f;
        anteprimaView.show(poss, pen);

        System.out.print("Confermare disdetta? [s/N]: ");
        String ans = in.nextLine().trim();
        if ("s".equalsIgnoreCase(ans) && selectedId != null) {
            controller.confermaDisdetta(selectedId, sessione);
        }
        return true;
    }

    private boolean handleSuccess() {
        Object raw = lastParams.get(GraphicControllerUtils.KEY_SUCCESSO);
        if (raw == null) {
            return false;
        }
        esitoView.showMessage(String.valueOf(raw));
        CliViewUtils.askReturnHome(in, controller::tornaAllaHome);
        return true;
    }
}
