package com.ispw.controller.graphic.cli;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerDisdetta;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerDisdettaPrenotazione;

/**
 * Adapter CLI per la disdetta prenotazione.
 */
public class CLIGraphicControllerDisdetta implements GraphicControllerDisdetta {
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    /**
     * Richiede prenotazioni cancellabili per l'utente.
     * View deve fornire UtenteBean dalla sessioneCorrente.
     */
    @Override
    public void richiediPrenotazioniCancellabili(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Sessione utente mancante");
            return;
        }

        try {
            LogicControllerDisdettaPrenotazione logicController = new LogicControllerDisdettaPrenotazione();
            List<RiepilogoPrenotazioneBean> prenotazioni =
                    logicController.ottieniPrenotazioniCancellabili(sessione.getUtente());

            List<String> elenco = prenotazioni.stream()
                    .map(RiepilogoPrenotazioneBean::toString)
                    .toList();

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_DISDETTA,
                    Map.of(GraphicControllerUtils.KEY_PRENOTAZIONI, elenco));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore richiesta prenotazioni cancellabili", e);
        }
    }

    @Override
    public void selezionaPrenotazione(int idPrenotazione) {
        if (idPrenotazione <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Id prenotazione non valido");
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_DISDETTA,
                Map.of(GraphicControllerUtils.KEY_ID_PRENOTAZIONE, idPrenotazione));
        }
    }

    /**
     * Richiede anteprima disdetta con calcolo rimborso.
     */
    @Override
    public void richiediAnteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (idPrenotazione <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Id prenotazione non valido");
            return;
        }
        if (sessione == null || sessione.getUtente() == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Sessione utente mancante");
            return;
        }

        try {
            LogicControllerDisdettaPrenotazione logicController = new LogicControllerDisdettaPrenotazione();
            EsitoDisdettaBean esito = logicController.anteprimaDisdetta(idPrenotazione, sessione);

            if (esito == null || !esito.isPossibile()) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Disdetta non consentita");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("possibile", esito.isPossibile());
            payload.put("penale", esito.getPenale());

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_DISDETTA, Map.of("anteprima", payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore anteprima disdetta", e);
        }
    }

    /**
     * Conferma disdetta prenotazione.
     */
    @Override
    public void confermaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (idPrenotazione <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Id prenotazione non valido");
            return;
        }
        if (sessione == null || sessione.getUtente() == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA, "Sessione utente mancante");
            return;
        }

        try {
            LogicControllerDisdettaPrenotazione logicController = new LogicControllerDisdettaPrenotazione();
            EsitoOperazioneBean esito = logicController.eseguiAnnullamento(idPrenotazione, sessione);

            if (esito == null || !esito.isSuccesso()) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
                    GraphicControllerUtils.PREFIX_DISDETTA,
                    esito != null ? esito.getMessaggio() : "Disdetta non riuscita");
                return;
            }

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_DISDETTA,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore conferma disdetta", e);
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

}
