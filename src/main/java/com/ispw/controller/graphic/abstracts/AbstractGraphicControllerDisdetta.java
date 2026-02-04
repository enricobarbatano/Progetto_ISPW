package com.ispw.controller.graphic.abstracts;

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

public abstract class AbstractGraphicControllerDisdetta implements GraphicControllerDisdetta {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerDisdetta(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void richiediPrenotazioniCancellabili(SessioneUtenteBean sessione) {
        if (isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_UTENTE_MANCANTE)) {
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
        if (isIdNonValido(idPrenotazione, GraphicControllerUtils.MSG_ID_PRENOTAZIONE_NON_VALIDO)) {
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_DISDETTA,
                Map.of(GraphicControllerUtils.KEY_ID_PRENOTAZIONE, idPrenotazione));
        }
    }

    @Override
    public void richiediAnteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (isIdNonValido(idPrenotazione, GraphicControllerUtils.MSG_ID_PRENOTAZIONE_NON_VALIDO)
                || isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_UTENTE_MANCANTE)) {
            return;
        }

        try {
            LogicControllerDisdettaPrenotazione logicController = new LogicControllerDisdettaPrenotazione();
            EsitoDisdettaBean esito = logicController.anteprimaDisdetta(idPrenotazione, sessione);

            if (esito == null || !esito.isPossibile()) {
                notifyDisdettaError(GraphicControllerUtils.MSG_DISDETTA_NON_CONSENTITA);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_POSSIBILE, esito.isPossibile());
            payload.put(GraphicControllerUtils.KEY_PENALE, esito.getPenale());

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_DISDETTA,
                    Map.of(GraphicControllerUtils.KEY_ANTEPRIMA, payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore anteprima disdetta", e);
        }
    }

    @Override
    public void confermaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (isIdNonValido(idPrenotazione, GraphicControllerUtils.MSG_ID_PRENOTAZIONE_NON_VALIDO)
                || isSessioneNonValida(sessione, GraphicControllerUtils.MSG_SESSIONE_UTENTE_MANCANTE)) {
            return;
        }

        try {
            LogicControllerDisdettaPrenotazione logicController = new LogicControllerDisdettaPrenotazione();
            EsitoOperazioneBean esito = logicController.eseguiAnnullamento(idPrenotazione, sessione);

            if (esito == null || !esito.isSuccesso()) {
                notifyDisdettaError(esito != null ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_DISDETTA_NON_RIUSCITA);
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
        goToHome();
    }

    private void notifyDisdettaError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_DISDETTA,
            GraphicControllerUtils.PREFIX_DISDETTA, message);
    }

    private boolean isSessioneNonValida(SessioneUtenteBean sessione, String message) {
        if (sessione == null || sessione.getUtente() == null) {
            notifyDisdettaError(message);
            return true;
        }
        return false;
    }

    private boolean isIdNonValido(int idPrenotazione, String message) {
        if (idPrenotazione <= 0) {
            notifyDisdettaError(message);
            return true;
        }
        return false;
    }
}
